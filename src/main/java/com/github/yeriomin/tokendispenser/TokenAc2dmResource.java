package com.github.yeriomin.tokendispenser;

import com.github.yeriomin.playstoreapi.GooglePlayAPI;
import com.github.yeriomin.playstoreapi.GooglePlayException;
import com.github.yeriomin.playstoreapi.PropertiesDeviceInfoProvider;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import spark.Request;
import spark.Response;

import static com.github.yeriomin.tokendispenser.Server.PROPERTY_RATE_LIMITING;
import static spark.Spark.halt;

public class TokenAc2dmResource {

    public String handle(Request request, Response response) {
        if (Server.getConfig().getProperty(PROPERTY_RATE_LIMITING, "false").equals("true")) {
            if (Server.stats.isSpam(request)) {
                Server.LOG.error(Server.longToIp(Server.getIp(request)) + " makes too many requests");
                response.header("Retry-After", Integer.toString(Server.stats.getRateLimitControlPeriod() / 1000));
                Server.stats.recordResult(429);
                halt(429, "Try again later");
            }
        }
        String email = request.params("email");
        String password = Server.passwords.get(email);
        if (null == password || password.isEmpty()) {
            Server.LOG.error(email + " not found");
            Server.stats.recordResult(404);
            halt(404, "No password for this email");
        }
        int code = 500;
        String message;
        try {
            String token = getToken(email, password);
            Server.LOG.warn("Success");
            Server.stats.recordResult(200);
            return token;
        } catch (GooglePlayException e) {
            if (e.getCode() >= 400) {
                code = e.getCode();
            }
            message = e.getMessage();
            Server.LOG.warn(e.getClass().getName() + ": " + message);
            Server.stats.recordResult(code);
            halt(code, "Google responded with: " + message);
        } catch (IOException e) {
            message = e.getMessage();
            Server.LOG.error(e.getClass().getName() + ": " + message);
            Server.stats.recordResult(code);
            halt(code, message);
        }
        Server.stats.recordResult(code);
        return "";
    }

    GooglePlayAPI getApi() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getSystemResourceAsStream("device-gemini.properties"));
        } catch (IOException e) {
            halt(500, "device-gemini.properties not found");
        }

        PropertiesDeviceInfoProvider deviceInfoProvider = new PropertiesDeviceInfoProvider();
        deviceInfoProvider.setProperties(properties);
        deviceInfoProvider.setLocaleString(Locale.ENGLISH.toString());

        GooglePlayAPI api = new GooglePlayAPI();
        api.setClient(new OkHttpClientAdapter());
        api.setDeviceInfoProvider(deviceInfoProvider);
        api.setLocale(Locale.US);
        return api;
    }

    protected String getToken(String email, String password) throws IOException {
        return getApi().generateAC2DMToken(email, password);
    }

}
