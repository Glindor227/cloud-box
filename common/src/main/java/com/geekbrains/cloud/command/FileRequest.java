package com.geekbrains.cloud.command;

public class FileRequest extends AbstractMessage {
    private String filename;
    private String login;
    private String password;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getFilename() {
        return filename;
    }


    public FileRequest(String login, String password, String filename) {
        this.filename = filename;
        this.login = login;
        this.password = password;
    }
}
