package com.geekbrains.cloud.server;

public interface AuthService {
    boolean ExistLoginAndPassword(String login, String password);
}
