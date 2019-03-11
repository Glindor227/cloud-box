package com.geekbrains.cloud.server;

import com.geekbrains.cloud.command.Error;
import com.geekbrains.cloud.command.ResultOfAuto;
import com.geekbrains.cloud.command.SetAuto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;


public class АuthenticationHandler extends ChannelInboundHandlerAdapter {

    private String login="";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

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
                    ctx.fireChannelRead(login);//передали(единократно) пароль дальше, чтобы он использовался как директория для файлов этого клиента
                    ctx.writeAndFlush(new ResultOfAuto(true));
                    (new FilesListCommon()).sendFilesListToClient(login,ctx);
                }else {
                    System.out.println("Авторизация "+sa.getLogin()+"/"+sa.getPassword()+" НЕ ПРОШЛА");
                    ctx.writeAndFlush(new ResultOfAuto(false));
                }
            }else{
                if(login.length()!=0){
                    ctx.fireChannelRead(msg);//передаем сообщешие дальше
                    return;
                }else{
                    System.out.println("Аутентификации не было");
                    ctx.writeAndFlush(new Error("Аутентификации не было"));
                }
            }


        }
        catch (Exception e){
        e.printStackTrace();
        }

        System.out.println("АuthenticationHandler Освобождаем msg");
        ReferenceCountUtil.release(msg);

    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
