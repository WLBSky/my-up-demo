package com.example.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.File;
import java.io.FileInputStream;

public class Client {

    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true) // 保持连接
                .option(ChannelOption.SO_SNDBUF, 1024) // 设置发送缓冲大小
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        ChannelPipeline pipeline = sc.pipeline();
                        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG, ByteBufFormat.SIMPLE));
                        pipeline.addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
                        pipeline.addLast(MarshallingCodeCFactory.buildMarshallingEncoder());
                        pipeline.addLast(new ClientHandler());
                    }
                });

        ChannelFuture cf1 = b.connect("127.0.0.1", 8765).sync();
        //ChannelFuture cf2 = b.connect("127.0.0.1", 8764).sync();  //可以使用多个端口
        //发送消息, Buffer类型. write需要flush才发送, 可用writeFlush代替

//        channel.writeAndFlush(Unpooled.copiedBuffer("777$_".getBytes()));
//        channel.writeAndFlush(Unpooled.copiedBuffer("666".getBytes()));
//        channel.writeAndFlush(Unpooled.copiedBuffer("888$_".getBytes()));
        //cf2.channel().writeAndFlush(Unpooled.copiedBuffer("999".getBytes()));
//        Channel channel = cf1.channel();
        for (int i = 1; i < 5; i++) {
            Request req = new Request();
            req.setId(String.valueOf(i));
            req.setName("图片 (" + i + ").jpg");
            req.setRequestMessage("数据信息" + i);
            String path = System.getProperty("user.dir") + File.separatorChar + "sources" + File.separatorChar + "图片 (" + i + ").jpg";
            File file = new File(path);
            FileInputStream in = new FileInputStream(file);
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            req.setAttachment(GzipUtils.gzip(data)); //压缩
            cf1.channel().writeAndFlush(req);
        }
        cf1.channel().closeFuture().sync();
        //cf2.channel().closeFuture().sync();
        group.shutdownGracefully();
    }
}