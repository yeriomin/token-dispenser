package com.github.yeriomin.tokendispenser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class PasswordsDbPlaintext implements PasswordsDbInterface {

    static private final String FIELD_SEPARATOR = " ";
    static private final String LINE_SEPARATOR = "\n";

    private String path;
    private Map<String, String> passwords = new HashMap<>();

    public PasswordsDbPlaintext(Properties config) {
        path = config.getProperty(Server.PROPERTY_STORAGE_PLAINTEXT_PATH, "passwords.txt");
        try {
            readStorage();
        } catch (IOException e) {
            Logger.getGlobal().warning("Could not read " + path);
        }
    }

    @Override
    public String get(String email) {
        Logger.getGlobal().info(email + (passwords.containsKey(email) ? "" : " NOT") + " found");
        return passwords.get(email);
    }

    @Override
    public void put(String email, String password) {
        passwords.put(email, password);
        try {
            writeStorage();
        } catch (IOException e) {
            Logger.getGlobal().warning("Could not write to " + path);
        }
    }

    private void readStorage() throws IOException {
        Logger.getGlobal().info("Reading " + path);
        int lineNum = 0;
        for (String line: Files.readAllLines(Paths.get(path))) {
            lineNum++;
            String[] pair = line.split(FIELD_SEPARATOR);
            if (pair.length != 2) {
                Logger.getGlobal().warning("Line " + lineNum + " is invalid");
                continue;
            }
            passwords.put(pair[0], pair[1]);
        }
    }

    private void writeStorage() throws IOException {
        Logger.getGlobal().info("Writing to " + path);
        StringBuilder builder = new StringBuilder();
        for (String username: passwords.keySet()) {
            builder.append(username).append(FIELD_SEPARATOR).append(passwords.get(username)).append(LINE_SEPARATOR);
        }
        Files.write(Paths.get(path), builder.toString().getBytes());
    }
}
