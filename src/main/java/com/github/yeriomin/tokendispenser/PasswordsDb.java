package com.github.yeriomin.tokendispenser;

import com.mongodb.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Properties;

class PasswordsDb {

    static private final String CONFIG_FILE = "/config.properties";

    static private final String FIELD_EMAIL = "email";
    static private final String FIELD_PASSWORD = "password";

    static private final String PROPERTY_HOST = "mongodb-host";
    static private final String PROPERTY_PORT = "mongodb-port";
    static private final String PROPERTY_USERNAME = "mongodb-username";
    static private final String PROPERTY_PASSWORD = "mongodb-password";
    static private final String PROPERTY_DB_STORAGE = "mongodb-databaseNameStorage";
    static private final String PROPERTY_COLLECTION = "mongodb-collectionName";

    private DBCollection collection;

    PasswordsDb() {
        Properties config = getConfig();
        String host = config.getProperty(PROPERTY_HOST, "");
        int port = Integer.parseInt(config.getProperty(PROPERTY_PORT, "0"));
        String username = config.getProperty(PROPERTY_USERNAME, "");
        String password = config.getProperty(PROPERTY_PASSWORD, "");
        String databaseNameStorage = config.getProperty(PROPERTY_DB_STORAGE, "");
        String collectionName = config.getProperty(PROPERTY_COLLECTION, "");
        Mongo mongo;
        try {
            mongo = new Mongo(host, port);
        } catch (UnknownHostException e) {
            System.out.println("UnknownHostException: " + e.getMessage());
            return;
        }
        DB mongoDb = mongo.getDB(databaseNameStorage);
        if (!mongoDb.authenticate(username, password.toCharArray())) {
            System.out.println("Failed to authenticate against db: " + databaseNameStorage);
            return;
        }
        collection = mongoDb.getCollection(collectionName);
    }

    String get(String email) {
        BasicDBObject query = new BasicDBObject(FIELD_EMAIL, email);
        DBObject object = collection.findOne(query);
        String password = null;
        if (null != object) {
            password = (String) object.get(FIELD_PASSWORD);
        }
        return password;
    }

    void put(String email, String password) {
        DBObject object = new BasicDBObject();
        object.put(FIELD_EMAIL, email);
        object.put(FIELD_PASSWORD, password);
        collection.insert(object);
    }

    Properties getConfig() {
        Properties properties = new Properties();
        try (InputStream input = PasswordsDb.class.getResourceAsStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String host = System.getenv("OPENSHIFT_MONGODB_DB_HOST");
        if (null != host && !host.isEmpty()) {
            properties.put(PROPERTY_HOST, host);
            properties.put(PROPERTY_PORT, System.getenv("OPENSHIFT_MONGODB_DB_PORT"));
            properties.put(PROPERTY_USERNAME, System.getenv("OPENSHIFT_MONGODB_DB_USERNAME"));
            properties.put(PROPERTY_PASSWORD, System.getenv("OPENSHIFT_MONGODB_DB_PASSWORD"));
            properties.put(PROPERTY_DB_STORAGE, System.getenv("OPENSHIFT_APP_NAME"));
        }
        return properties;
    }

}
