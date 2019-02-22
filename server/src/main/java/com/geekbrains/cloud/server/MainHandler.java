package com.geekbrains.cloud.server;

import com.geekbrains.cloud.command.*;
import com.geekbrains.cloud.command.Error;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import javax.security.sasl.AuthenticationException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
                    ctx.writeAndFlush(new AutoRezult(true));
                }else {
                    System.out.println("Авторизация "+sa.getLogin()+"/"+sa.getPassword()+" НЕ ПРОШЛА");
                    ctx.writeAndFlush(new AutoRezult(false));
                }
            }
            if (msg instanceof FileRequest) {
                System.out.println("Пришло FileRequest");
                CheckAuthentication(ctx);

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
                CheckAuthentication(ctx);
                String sPath = "server_storage/"+ login;
                FilesListRezult filesListClass = new FilesListRezult();
                Files.list(Paths.get(sPath)).map(p -> p.getFileName().toString()).forEach(o -> filesListClass.AddFile(o));
                System.out.println("Готов список из "+filesListClass.getFileList().size()+"файлов");
                ctx.writeAndFlush(filesListClass);
            }
            if (msg instanceof FileMessage) {
                System.out.println("Пришло FileMessage");
                CheckAuthentication(ctx);
                FileMessage fm = (FileMessage) msg;
                String sPath = "server_storage/"+ login+"/";
                try {
                    Files.write(Paths.get(sPath + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                }catch (Exception e){
                    System.out.println("Почемуто не записали на сервер файл");
                    e.printStackTrace();
                }

            }

        }catch (AuthenticationException e){
            System.out.println("не было авторизации");
        }
        finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void CheckAuthentication(ChannelHandlerContext ctx) throws AuthenticationException {
        if (login.length() == 0) {
            System.out.println("Аутентификации не было");
            ctx.writeAndFlush(new Error("Аутентификации не было"));
            throw new AuthenticationException();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
