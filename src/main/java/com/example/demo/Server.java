package com.example.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


public class Server {

    public static void main(String[] args) throws Exception {
        // 1 创建线两个事件循环组
        // 一个是用于处理服务器端接收客户端连接的
        // 一个是进行网络通信的（网络读写的）
        EventLoopGroup pGroup = new NioEventLoopGroup();
        EventLoopGroup cGroup = new NioEventLoopGroup();

        // 2 创建辅助工具类ServerBootstrap，用于服务器通道的一系列配置
        ServerBootstrap b = new ServerBootstrap();
        b.group(pGroup, cGroup) // 绑定俩个线程组
                .channel(NioServerSocketChannel.class) // 指定NIO的模式.NioServerSocketChannel对应TCP, NioDatagramChannel对应UDP
                .option(ChannelOption.SO_BACKLOG, 1024) // 设置TCP缓冲区
//                .option(ChannelOption.SO_SNDBUF, 32 * 1024) // 设置发送缓冲大小
                .option(ChannelOption.SO_RCVBUF, 32 * 1024) // 这是接收缓冲大小
//                .option(ChannelOption.SO_KEEPALIVE, true) // 保持连接
//                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) {  //SocketChannel建立连接后的管道
                        ChannelPipeline pipeline = sc.pipeline();
                        //分隔符，，拆包粘包
//                        ByteBuf buf = Unpooled.copiedBuffer("$_".getBytes());
//                        pipeline.addLast(new DelimiterBasedFrameDecoder(1024, buf));
                        //解析btyeBuf为String
//                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG, ByteBufFormat.SIMPLE));
                        pipeline.addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
                        pipeline.addLast(MarshallingCodeCFactory.buildMarshallingEncoder());
                        // 3 在这里配置 通信数据的处理逻辑, 可以addLast多个...
                        pipeline.addLast(new ServerHandler());
                    }
                });
        // 4 绑定端口, bind返回future(异步), 加上sync阻塞在获取连接处
        ChannelFuture cf1 = b.bind(8765).sync();
        //ChannelFuture cf2 = b.bind(8764).sync();   //可以绑定多个端口
        // 5 等待关闭, 加上sync阻塞在关闭请求处
        Channel channel = cf1.channel();
        channel.closeFuture().sync();
        //cf2.channel().closeFuture().sync();
        pGroup.shutdownGracefully();
        cGroup.shutdownGracefully();
    }
}