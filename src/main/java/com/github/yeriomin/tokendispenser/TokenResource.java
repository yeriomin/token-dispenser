package com.github.yeriomin.tokendispenser;

import com.github.yeriomin.playstoreapi.GooglePlayAPI;
import spark.HaltException;
import spark.Request;
import spark.Response;

import java.io.IOException;

public class TokenResource extends TokenAc2dmResource {

    private String gsfId;

    @Override
    public String handle(Request request, Response response) {
        gsfId = request.params("gsf-id");
        return super.handle(request, response);
    }

    @Override
    protected String getToken(String password) throws IOException {
        GooglePlayAPI api = getApi();
        api.setGsfId(gsfId);
        return api.getToken(password);
    }
}
