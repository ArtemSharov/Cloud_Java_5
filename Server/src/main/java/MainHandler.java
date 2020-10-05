import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class MainHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        if (msg instanceof FileRequest){
            FileRequest fr = (FileRequest) msg;
            if(Files.exists(Paths.get("Server_storage/" + fr.getName()))){
                FileMessage fm = new FileMessage(Paths.get("Server_storage/" +
                        fr.getName()));
                ctx.writeAndFlush(fm);
            }
        }
        System.out.println(msg.toString());
        if(msg.equals("./getFilesList")){
            File dir = new File("Server_storage");
            String [] files = dir.list();
            if (files != null) {

                for (String file : files) {
                    ctx.writeAndFlush(file);
                }
            } else {
                ctx.writeAndFlush(0);
            }

        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        cause.printStackTrace();
        ctx.close();
    }
}