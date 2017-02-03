package com.github.yeriomin.tokendispenser;

import static spark.Spark.*;

public class Server {

    static PasswordsDb passwords;

    public static void main(String[] args) {
        port(8080);
//        staticFiles.location("/public");
        passwords = new PasswordsDb();

//        secure("deploy/keystore.jks", "password", null, null);
        get("/token/email/:email/gsf-id/:gsf-id", (req, res) -> new TokenResource().handle(req, res));
        get("/token-ac2dm/email/:email", (req, res) -> new TokenAc2dmResource().handle(req, res));
        after((request, response) -> response.type("text/plain"));
    }
}
