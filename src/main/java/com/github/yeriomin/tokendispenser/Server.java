package com.github.yeriomin.tokendispenser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static spark.Spark.*;

public class Server {

    static public final Logger LOG = LoggerFactory.getLogger(Server.class.getName());

    static private final String CONFIG_FILE = "/config.properties";

    static final String PROPERTY_SPARK_HOST = "spark-host";
    static final String PROPERTY_SPARK_PORT = "spark-port";
    static final String PROPERTY_STORAGE = "storage";
    static final String PROPERTY_STORAGE_PLAINTEXT_PATH = "storage-plaintext-path";
    static final String PROPERTY_MONGODB_HOST = "mongodb-host";
    static final String PROPERTY_MONGODB_PORT = "mongodb-port";
    static final String PROPERTY_MONGODB_USERNAME = "mongodb-username";
    static final String PROPERTY_MONGODB_PASSWORD = "mongodb-password";
    static final String PROPERTY_MONGODB_DB = "mongodb-databaseNameStorage";
    static final String PROPERTY_MONGODB_COLLECTION = "mongodb-collectionName";
    static final String PROPERTY_EMAIL_RETRIEVAL = "enable-email-retrieval";
    static final String PROPERTY_RATE_LIMITING = "rate-limiting";
    static final String PROPERTY_RATE_LIMITING_MAX_REQUESTS = "rate-limiting-max-requests";
    static final String PROPERTY_RATE_LIMITING_CONTROL_PERIOD = "rate-limiting-control-period";
    static final String PROPERTY_RATE_LIMITING_EXPOSE_STATS_ENDPOINT = "rate-limiting-expose-stats-endpoint";

    static public final String STORAGE_MONGODB = "mongodb";
    static public final String STORAGE_PLAINTEXT = "plaintext";

    static PasswordsDbInterface passwords;
    static Map<Long, List<Long>> ips = new HashMap<>();
    static Map<Long, Integer> rateLimitHits = new HashMap<>();
    static Map<Integer, Integer> tokenRetrievalResults = new HashMap<>();
    static int rateLimitControlPeriod = 5 * 60 * 1000;
    static int rateLimitRequests = 10;

    static long getIp(Request request) {
        String requestIp = request.headers("X-Forwarded-For");
        return ipToLong((null == requestIp || requestIp.isEmpty()) ? request.ip() : requestIp);
    }

    private static long ipToLong(String address) {
        String[] split = address.split("\\.");
        if (split.length < 4) {
            return 0;
        }
        long result = 0;
        for (int i = 0; i < split.length; i++) {
            int power = 3 - i;
            result += (Integer.parseInt(split[i])%256 * Math.pow(256,power));
        }
        return result;
    }

    static String longToIp(long ip) {
        return ((ip >> 24 ) & 0xFF) + "." + ((ip >> 16 ) & 0xFF) + "." + ((ip >> 8 ) & 0xFF) + "." + (ip & 0xFF);
    }

    static void recordRequest(Request request) {
        long ip = getIp(request);
        if (!ips.containsKey(ip)) {
            ips.put(ip, new ArrayList<>());
        }
        ips.get(ip).add(System.currentTimeMillis());
    }

    static void recordResult(int responseCode) {
        if (!tokenRetrievalResults.containsKey(responseCode)) {
            tokenRetrievalResults.put(responseCode, 0);
        }
        tokenRetrievalResults.put(responseCode, tokenRetrievalResults.get(responseCode) + 1);
    }

    static boolean isSpam(Request request) {
        long ip = getIp(request);
        if (ips.containsKey(ip)) {
            int recentRequestCount = 0;
            for (Long timestamp: ips.get(ip)) {
                if (timestamp > System.currentTimeMillis() - rateLimitControlPeriod) {
                    recentRequestCount++;
                }
                if (recentRequestCount > rateLimitRequests) {
                    if (!rateLimitHits.containsKey(ip)) {
                        rateLimitHits.put(ip, 0);
                    }
                    rateLimitHits.put(ip, rateLimitHits.get(ip) + 1);
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Properties config = getConfig();
        String host = config.getProperty(PROPERTY_SPARK_HOST, "0.0.0.0");
        int port = Integer.parseInt(config.getProperty(PROPERTY_SPARK_PORT, "8080"));
        String hostDiy = System.getenv("OPENSHIFT_DIY_IP");
        if (null != hostDiy && !hostDiy.isEmpty()) {
            host = hostDiy;
            port = Integer.parseInt(System.getenv("OPENSHIFT_DIY_PORT"));
        }
        try {
            port = Integer.parseInt(System.getenv("PORT"));
        } catch (NumberFormatException e) {
            // Apparently, environment is not heroku
        }
        ipAddress(host);
        port(port);
        notFound("Not found");
        before((req, res) -> {
            LOG.info(req.requestMethod() + " " + req.url());
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Request-Method", "GET");
        });
        after((req, res) -> res.type("text/plain"));
        Server.passwords = PasswordsDbFactory.get(config);
        get("/token/email/:email", (req, res) -> new TokenResource().handle(req, res));
        get("/token-ac2dm/email/:email", (req, res) -> new TokenAc2dmResource().handle(req, res));
        if (config.getProperty(PROPERTY_EMAIL_RETRIEVAL, "false").equals("true")) {
            LOG.info("Exposing /email endpoint");
            get("/email", (req, res) -> new EmailResource().handle(req, res));
        }
        if (config.getProperty(PROPERTY_RATE_LIMITING, "false").equals("true")) {
            LOG.info("Enabling rate limiting");
            rateLimitControlPeriod = Integer.parseInt(config.getProperty(PROPERTY_RATE_LIMITING_CONTROL_PERIOD, "300000"));
            rateLimitRequests = Integer.parseInt(config.getProperty(PROPERTY_RATE_LIMITING_MAX_REQUESTS, "20"));
            if (config.getProperty(PROPERTY_RATE_LIMITING_EXPOSE_STATS_ENDPOINT, "false").equals("true")) {
                LOG.info("Exposing /stats endpoint");
                get("/stats", (req, res) -> new StatsResource().get(req, res));
                delete("/stats", (req, res) -> new StatsResource().delete(req, res));
            }
        }
    }

    static Properties getConfig() {
        Properties properties = new Properties();
        try (InputStream input = PasswordsDbMongo.class.getResourceAsStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String host = System.getenv("OPENSHIFT_MONGODB_DB_HOST");
        if (null != host && !host.isEmpty()) {
            properties.put(PROPERTY_MONGODB_HOST, host);
            properties.put(PROPERTY_MONGODB_PORT, System.getenv("OPENSHIFT_MONGODB_DB_PORT"));
            properties.put(PROPERTY_MONGODB_USERNAME, System.getenv("OPENSHIFT_MONGODB_DB_USERNAME"));
            properties.put(PROPERTY_MONGODB_PASSWORD, System.getenv("OPENSHIFT_MONGODB_DB_PASSWORD"));
            properties.put(PROPERTY_MONGODB_DB, System.getenv("OPENSHIFT_APP_NAME"));
        }
        return properties;
    }
}
