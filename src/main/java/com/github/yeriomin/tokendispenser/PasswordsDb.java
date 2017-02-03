package com.github.yeriomin.tokendispenser;

import com.mongodb.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Properties;

class PasswordsDb {

    static private final String FIELD_EMAIL = "email";
    static private final String FIELD_PASSWORD = "password";

    private DBCollection collection;

    PasswordsDb(Properties config) {
        String host = config.getProperty(Server.PROPERTY_MONGODB_HOST, "");
        int port = Integer.parseInt(config.getProperty(Server.PROPERTY_MONGODB_PORT, "0"));
        String username = config.getProperty(Server.PROPERTY_MONGODB_USERNAME, "");
        String password = config.getProperty(Server.PROPERTY_MONGODB_PASSWORD, "");
        String databaseNameStorage = config.getProperty(Server.PROPERTY_MONGODB_DB, "");
        String collectionName = config.getProperty(Server.PROPERTY_MONGODB_COLLECTION, "");
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

}
