package com.github.yeriomin.tokendispenser;

import java.util.Properties;

public class PasswordsDbFactory {

    static public PasswordsDbInterface get(Properties config) {
        String storage = config.getProperty(Server.PROPERTY_STORAGE, Server.STORAGE_MONGODB);
        Server.LOG.info("Initializing storage type " + storage);
        if (storage.equals(Server.STORAGE_MONGODB)) {
            return new PasswordsDbMongo(config);
        } else {
            return new PasswordsDbPlaintext(config);
        }
    }
}
