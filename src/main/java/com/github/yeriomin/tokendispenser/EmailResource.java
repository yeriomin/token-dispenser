package com.github.yeriomin.tokendispenser;

import spark.Request;
import spark.Response;

public class EmailResource {

    public String handle(Request request, Response response) {
        return Server.passwords.getRandomEmail();
    }
}
