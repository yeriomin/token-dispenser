package com.github.yeriomin.tokendispenser;

import static spark.Spark.*;

public class Server {

    private static final String IP_ADDRESS = System.getenv("OPENSHIFT_DIY_IP") != null ? System.getenv("OPENSHIFT_DIY_IP") : "localhost";
    private static final int PORT = System.getenv("OPENSHIFT_DIY_PORT") != null ? Integer.parseInt(System.getenv("OPENSHIFT_DIY_PORT")) : 8080;

    static PasswordsDb passwords;

    public static void main(String[] args) {
        ipAddress(IP_ADDRESS);
        port(PORT);
        get("/token/email/:email/gsf-id/:gsf-id", (req, res) -> new TokenResource().handle(req, res));
        get("/token-ac2dm/email/:email", (req, res) -> new TokenAc2dmResource().handle(req, res));
        after((request, response) -> response.type("text/plain"));
    }
}
