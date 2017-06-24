package com.github.yeriomin.tokendispenser;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

class PasswordsDbMongo implements PasswordsDbInterface {

    static private final String FIELD_EMAIL = "email";
    static private final String FIELD_PASSWORD = "password";

    private DBCollection collection;

    public PasswordsDbMongo(Properties config) {
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
            Server.LOG.error("UnknownHostException: " + e.getMessage());
            return;
        }
        DB mongoDb = mongo.getDB(databaseNameStorage);
        if (!mongoDb.authenticate(username, password.toCharArray())) {
            Server.LOG.error("Failed to authenticate against db: " + databaseNameStorage);
            return;
        }
        collection = mongoDb.getCollection(collectionName);
    }

    @Override
    public String getRandomEmail() {
        List<String> emails = new ArrayList<>();
        collection.find().forEach((DBObject dbObject) -> emails.add((String) dbObject.get(FIELD_EMAIL)));
        return emails.get(new Random().nextInt(emails.size()));
    }

    @Override
    public String get(String email) {
        BasicDBObject query = new BasicDBObject(FIELD_EMAIL, email);
        DBObject object = collection.findOne(query);
        String password = null;
        if (null != object) {
            password = (String) object.get(FIELD_PASSWORD);
        }
        return password;
    }

    @Override
    public void put(String email, String password) {
        DBObject object = new BasicDBObject();
        object.put(FIELD_EMAIL, email);
        object.put(FIELD_PASSWORD, password);
        collection.insert(object);
    }
}
