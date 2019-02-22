package com.geekbrains.cloud.command;

public class AutoRezult extends AbstractMessage {
    private Boolean rezult;

    public Boolean getRezult() {
        return rezult;
    }


    public AutoRezult(Boolean rezult) {
        this.rezult = rezult;
    }
}

