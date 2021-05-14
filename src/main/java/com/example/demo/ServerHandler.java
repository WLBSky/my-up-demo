package com.example.demo;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileOutputStream;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server channel active... ");
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server channel remove... ");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server channel active... ");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ByteBuf buf = (ByteBuf) msg;
//        byte[] req = new byte[buf.readableBytes()];
//        buf.readBytes(req);
//        String body = new String(req, "utf-8");
//        String body = (String) msg;
//        System.out.println("Server :" + body);
//        String response = "返回给客户端的响应：" + body;
//        ctx.writeAndFlush(Unpooled.copiedBuffer(response.getBytes()));//.addListener(ChannelFutureListener.CLOSE);
        // future完成后触发监听器, 此处是写完即关闭(短连接). 因此需要关闭连接时, 要通过server端关闭. 直接关闭用方法ctx[.channel()].close()


        Request req = (Request) msg;
        System.out.println("Server : " + req.getId() + ", " + req.getName() + ", " + req.getRequestMessage());
        byte[] attachment = GzipUtils.ungzip(req.getAttachment());

        String path = System.getProperty("user.dir") + File.separatorChar + "receive" + File.separatorChar + req.getName();
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(attachment);
        fos.close();

        Response resp = new Response();
        resp.setId(req.getId());
        resp.setName("resp" + req.getId());
        resp.setResponseMessage("响应内容" + req.getId());
        ctx.channel().writeAndFlush(resp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
            throws Exception {
        System.out.println("Server 读完了");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable t)
            throws Exception {
        t.printStackTrace();
        ctx.channel().close().sync();
    }
}
