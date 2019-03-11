package com.geekbrains.cloud.server;

import com.geekbrains.cloud.command.FilesListRezult;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FilesListCommon {
    public FilesListCommon() {
    }
    void sendFilesListToClient(String login, ChannelHandlerContext ctx) throws IOException {
        String sPath = "server_storage/"+ login;
        FilesListRezult filesListClass = new  FilesListRezult(
                Files.list(Paths.get(sPath))
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList()));

        System.out.println("Готов список из "+filesListClass.getFileList().size()+"файлов");
        ctx.writeAndFlush(filesListClass);

    }

}
