package com.geekbrains.cloud.command;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileMessage extends AbstractMessage {
    private static int SIZEBLOCK = 1024*1024*5;//5 метров - размер для оставного файла

    private String filename;// имя для записи( при чтении из класса)
    private String filePath;//полный путь для считывание в массив класса
    private Boolean endPart;//признак что это окончание файла
    private Boolean firstPart;//признак что это начало файла
    private Integer numerPart;
    private byte[] data;

    public String getFilename() {
        return filename;
    }
    public byte[] getData() {
        return data;
    }

    public Boolean getEndPart() {
        return endPart;
    }

    public Boolean getFirstPart() {
        return firstPart;
    }

    public FileMessage(Path path){
        firstPart=true;
        endPart = false;
        filename = path.getFileName().toString();
        filePath = path.toString();
        System.out.println("new FileMessage "+ filename+"   "+path.getFileName().toString());
        numerPart =-1;
//        data = Files.readAllBytes(path);
    }


    public Boolean next() throws IOException {
        System.out.println("файл next");

        if(endPart){
            System.out.println("файл кончился");
            return false;
        }
        Path path = Paths.get(filePath);
        long fSize = Files.size(path);
        if(fSize<SIZEBLOCK){
            System.out.println("Маленький файл("+ fSize +") шлем целиком");
            data = Files.readAllBytes(path);
            firstPart = endPart = true;
            return true;
        }
        numerPart++;
        firstPart = numerPart==0;
        System.out.println("Большой файл("+ fSize +"). Часть "+numerPart);
        RandomAccessFile raf = new RandomAccessFile(filePath,"r");
        raf.seek(numerPart*SIZEBLOCK);
        byte[] dataTemp = new byte[SIZEBLOCK];
        int countCopy = raf.read(dataTemp);
        raf.close();
        endPart = (countCopy!=SIZEBLOCK);
        System.out.println("Блок размером в "+countCopy);
        data = Arrays.copyOf(dataTemp,countCopy);
        return true;
    }
}
