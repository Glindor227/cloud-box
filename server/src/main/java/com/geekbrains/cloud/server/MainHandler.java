package com.geekbrains.cloud.server;

import com.geekbrains.cloud.command.*;
import com.geekbrains.cloud.command.Error;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private String login="";
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }
            System.out.println("Пришло сообщение для  "+login);
            if(msg instanceof SetAuto){
                System.out.println("Пришло SetAuto");
                SetAuto sa = (SetAuto)msg;
                AuthService AuthService = new DBAuthService();
                if(AuthService.ExistLoginAndPassword(sa.getLogin(),sa.getPassword())){
                    System.out.println("Авторизация "+sa.getLogin()+"/"+sa.getPassword()+" прошла успешно");
                    login = sa.getLogin();
                    ctx.writeAndFlush(new ResultOfAuto(true));
                    sendFilesListToClient(ctx);
                }else {
                    System.out.println("Авторизация "+sa.getLogin()+"/"+sa.getPassword()+" НЕ ПРОШЛА");
                    ctx.writeAndFlush(new ResultOfAuto(false));
                }
            }
            if (login.length() == 0) {
                System.out.println("Аутентификации не было");
                ctx.writeAndFlush(new Error("Аутентификации не было"));
                return;
            }

            if (msg instanceof FileRequest) {
                System.out.println("Пришло FileRequest");

                FileRequest fr = (FileRequest) msg;
                String filePath = "server_storage/"+ login +"/"+fr.getFilename();
                System.out.println("Запрос файла "+filePath);
                if (Files.exists(Paths.get(filePath))) {
                    FileMessage fm = new FileMessage(Paths.get(filePath));
                    ctx.writeAndFlush(fm);
                }
            }
            if (msg instanceof FilesListRequest) {
                System.out.println("Пришло FilesListRequest");
                sendFilesListToClient(ctx);
            }
            if (msg instanceof FileMessage) {
                System.out.println("Пришло FileMessage");
                FileMessage fm = (FileMessage) msg;
                String sPath = "server_storage/"+ login+"/";
                try {
                    Files.write(Paths.get(sPath + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                }catch (Exception e){
                    System.out.println("Почемуто не записали на сервер файл");
                    e.printStackTrace();
                    ctx.writeAndFlush(new Error("Почемуто не записали на сервер файл"));
                }
                sendFilesListToClient(ctx);

            }

        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void sendFilesListToClient(ChannelHandlerContext ctx) throws IOException {
        String sPath = "server_storage/"+ login;
        FilesListRezult filesListClass = new  FilesListRezult(Files.list(Paths.get(sPath)).map(p -> p.getFileName().toString()).collect(Collectors.toList()));
        System.out.println("Готов список из "+filesListClass.getFileList().size()+"файлов");
        ctx.writeAndFlush(filesListClass);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
