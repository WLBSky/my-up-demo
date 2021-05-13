package com.example.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {

    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true) // 保持连接
                .option(ChannelOption.SO_SNDBUF, 32 * 1024) // 设置发送缓冲大小
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        sc.pipeline().addLast(new ClientHandler());
                    }
                });

        ChannelFuture cf1 = b.connect("127.0.0.1", 8765).sync();
        //ChannelFuture cf2 = b.connect("127.0.0.1", 8764).sync();  //可以使用多个端口
        //发送消息, Buffer类型. write需要flush才发送, 可用writeFlush代替
        Channel channel = cf1.channel();
        channel.writeAndFlush(Unpooled.copiedBuffer("777$_".getBytes()));
        channel.writeAndFlush(Unpooled.copiedBuffer("666".getBytes()));
        channel.writeAndFlush(Unpooled.copiedBuffer("888$_".getBytes()));
        //cf2.channel().writeAndFlush(Unpooled.copiedBuffer("999".getBytes()));
        channel.closeFuture().sync();
        //cf2.channel().closeFuture().sync();
        group.shutdownGracefully();
    }
}