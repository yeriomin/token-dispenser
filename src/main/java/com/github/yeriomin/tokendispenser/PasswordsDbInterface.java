package com.github.yeriomin.tokendispenser;

public interface PasswordsDbInterface {

    String get(String email);
    void put(String email, String password);
}
