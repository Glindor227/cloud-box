package com.geekbrains.cloud.command;

public class Error extends AbstractMessage {
    private String message;

    public String getMessage() {
        return message;
    }

    public Error(String message) {
        this.message = message;
    }
}
