package com.geekbrains.cloud.command;

import java.util.ArrayList;
import java.util.List;

public class FilesListRezult extends AbstractMessage {
    private List<String> fileList;

/*    public FilesListRezult() {
        fileList= new ArrayList<>();
    }
*/
    public FilesListRezult(List<String> fileList) {
        this.fileList = fileList;
    }

    public List<String> getFileList() {
        return fileList;
    }
/*    public void AddFile(String newFile){
        fileList.add(newFile);
    }
*/
}
