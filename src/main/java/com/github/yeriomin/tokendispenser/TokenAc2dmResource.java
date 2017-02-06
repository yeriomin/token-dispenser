package com.github.yeriomin.tokendispenser;

import com.github.yeriomin.playstoreapi.GooglePlayAPI;
import com.github.yeriomin.playstoreapi.GooglePlayException;
import com.github.yeriomin.playstoreapi.PropertiesDeviceInfoProvider;
import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import static spark.Spark.halt;

public class TokenAc2dmResource {

    private String email;

    public String handle(Request request, Response response) {
        email = request.params("email");
        String password = Server.passwords.get(email);
        if (null == password || password.isEmpty()) {
            halt(404, "No password for this email");
        }
        int code = 500;
        String message;
        try {
            return getToken(password);
        } catch (GooglePlayException e) {
            if (e.getCode() >= 400) {
                code = e.getCode();
            }
            message = e.getMessage();
            halt(code, message);
        } catch (IOException e) {
            message = e.getMessage();
            halt(code, message);
        }
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

        GooglePlayAPI api = new GooglePlayAPI(email);
        api.setDeviceInfoProvider(deviceInfoProvider);
        api.setLocale(Locale.US);
        return api;
    }

    protected String getToken(String password) throws IOException {
        return getApi().getAC2DMToken(password);
    }

}
