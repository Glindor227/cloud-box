package com.geekbrains.cloud.server;

import com.geekbrains.cloud.command.*;
import com.geekbrains.cloud.command.Error;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private String userDir="";
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof String) {
                userDir = (String)msg;
                System.out.println("Получили поддериктоию для работы клиента - " + userDir);
            }


            if (msg instanceof FileRequest) {
                System.out.println("Пришло FileRequest");

                FileRequest fr = (FileRequest) msg;
                String filePath = "server_storage/"+ userDir +"/"+fr.getFilename();
                System.out.println("Запрос файла "+filePath);
                if (Files.exists(Paths.get(filePath))) {

                    //Разбитие файлов на части реализовано внутри FileMessage в проекте "common"
                    FileMessage fm = new FileMessage(Paths.get(filePath));
                    while(fm.next()) {
                        ctx.writeAndFlush(fm);
                    }
                }
            }
            if (msg instanceof FileDelete) {
                System.out.println("Пришло FileDelete");

                FileDelete fd = (FileDelete) msg;
                String filePath = "server_storage/"+ userDir +"/"+fd.getFilename();
                System.out.println("Запрос файла "+filePath);
                if (Files.deleteIfExists(Paths.get(filePath)))
                {
                    (new FilesListCommon()).sendFilesListToClient(userDir,ctx);
                }
                else{
                    ctx.writeAndFlush(new Error("Не смогли удалить файл '" + fd.getFilename()+"'"));
                }

            }
            if (msg instanceof FilesListRequest) {
                System.out.println("Пришло FilesListRequest");
                (new FilesListCommon()).sendFilesListToClient(userDir,ctx);
            }
            if (msg instanceof FileMessage) {
                System.out.println("Пришло FileMessage");
                FileMessage fm = (FileMessage) msg;
                String sPath = "server_storage/"+ userDir+"/";
                try {
                    if(fm.getFirstPart()){
                        System.out.println("Пришло начало файл("+fm.getFilename()+") - "+(fm.getEndPart()?"Это же и окончание":""));
                        Files.write(Paths.get(sPath + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                    }
                    else{
                        System.out.println("Пришла часть файла("+fm.getFilename()+") - "+(fm.getEndPart()?"Окончание":"середина"));
                        Files.write(Paths.get(sPath + fm.getFilename()), fm.getData(), StandardOpenOption.APPEND);
                    }
                    if(fm.getEndPart())
                        (new FilesListCommon()).sendFilesListToClient(userDir,ctx);
                }catch (Exception e){
                    System.out.println("Почемуто не записали на сервер файл");
                    e.printStackTrace();
                    ctx.writeAndFlush(new Error("Почемуто не записали на сервер файл"));
                }
            }


        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            System.out.println("MainHandler Освобождаем msg");
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
