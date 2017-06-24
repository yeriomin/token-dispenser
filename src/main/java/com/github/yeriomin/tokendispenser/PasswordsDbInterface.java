package com.github.yeriomin.tokendispenser;

public interface PasswordsDbInterface {

    String getRandomEmail();
    String get(String email);
    void put(String email, String password);
}
