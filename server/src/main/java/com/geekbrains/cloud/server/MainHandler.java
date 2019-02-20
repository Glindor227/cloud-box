package com.geekbrains.cloud.server;

import com.geekbrains.cloud.command.FileMessage;
import com.geekbrains.cloud.command.FileRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Paths;

public class MainHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof FileRequest) {
                FileRequest fr = (FileRequest) msg;
                String filePath = "server_storage/" + fr.getFilename();
                AuthService AuthService = new DBAuthService();
                if(AuthService.ExistLoginAndPassword(fr.getLogin(),fr.getPassword())){
                    System.out.println("Авторизация "+fr.getLogin()+"/"+fr.getPassword()+" прошла успешно");
                }else {
                    System.out.println("Авторизация "+fr.getLogin()+"/"+fr.getPassword()+" НЕ ПРОШЛА");
                }


                fr.getLogin();
                if (Files.exists(Paths.get(filePath))) {
                    FileMessage fm = new FileMessage(Paths.get(filePath));
                    ctx.writeAndFlush(fm);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
