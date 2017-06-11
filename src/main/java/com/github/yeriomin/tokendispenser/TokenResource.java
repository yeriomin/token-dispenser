package com.github.yeriomin.tokendispenser;

import java.io.IOException;

public class TokenResource extends TokenAc2dmResource {

    @Override
    protected String getToken(String email, String password) throws IOException {
        return getApi().generateToken(email, password);
    }
}
