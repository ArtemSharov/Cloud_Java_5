import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class MainHandler extends ChannelInboundHandlerAdapter {

    private String tmpUser;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{

        if (msg instanceof LoginMessage){
            LoginMessage lm = (LoginMessage) msg;
            System.out.println("Try login " + lm.getLogin());
            DbHandler dbHandler = new DbHandler();
            for (User u: dbHandler.getUsers()) {
                if (u.getLogin().equals(lm.getLogin()) && u.getPassword().equals(lm.getPassword())) {
                    lm.setAuthorized(true);
                }
            }
                if(lm.getAuthorized()){
                    ctx.writeAndFlush(lm);
                    System.out.println("Hi! " + lm.getLogin());
                    tmpUser = lm.getLogin();
                        if(!Files.exists(Paths.get("Server_storage/" + lm.getLogin()))) {
                            File userDir = new File("Server_storage/" + lm.getLogin());
                            userDir.mkdir();
                        }
                }else{
                    lm.setAuthorized(false);
                    ctx.writeAndFlush(lm);
                }

            }


        if (msg instanceof FileRequest) {
                System.out.println("upload ");
                FileRequest fr = (FileRequest) msg;
                System.out.println("file " + fr.getName());
                if (Files.exists(Paths.get("Server_storage/"+ tmpUser + "/" + fr.getName()))) {
                    FileMessage fm = new FileMessage(Paths.get("Server_storage/"  + tmpUser + "/" +
                            fr.getName()));
                    ctx.writeAndFlush(fm);
                }
            }

            if(msg instanceof FileMessage){
                System.out.println("download");
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get("Server_storage/"  + tmpUser + "/" + fm.getFileName()), fm.getData(),
                        StandardOpenOption.CREATE);
            }


        if(msg.equals("/getFileList")){

            File dir = new File("Server_storage/" + tmpUser);
            String [] files = dir.list();
            if (files != null) {
                Integer length = 0;
                length =files.length;

                ctx.writeAndFlush(length);
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