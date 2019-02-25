package com.geekbrains.cloud.command;

public class ResultOfAuto extends AbstractMessage {
    private Boolean rezult;

    public Boolean getRezult() {
        return rezult;
    }


    public ResultOfAuto(Boolean rezult) {
        this.rezult = rezult;
    }
}