package com.geekbrains.cloud.command;

import java.util.ArrayList;

public class FilesListRezult extends AbstractMessage {
    private ArrayList<String> fileList;

    public FilesListRezult() {
        fileList= new ArrayList<>();
    }

    public ArrayList<String> getFileList() {
        return fileList;
    }
    public void AddFile(String newFile){
        fileList.add(newFile);
    }

}
