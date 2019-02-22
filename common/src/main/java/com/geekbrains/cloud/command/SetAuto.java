package com.geekbrains.cloud.command;

public class SetAuto extends AbstractMessage {
    private String login;
    private String password;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public SetAuto(String login, String password) {
        this.login = login;
        this.password = password;
    }
}

