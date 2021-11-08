package com.grallcd.server;

import com.grallcd.handler.EchoServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author grallcd
 * @since 2021/11/5
 */
@Slf4j
public class EchoServer {

    public static void main(String[] args) {

        // 创建 2 个设置线程组
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();

        EchoServerHandler echoServerHandler = new EchoServerHandler();
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);


        try {
            // 创建服务端启动对象，设置参数
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    // 设置通道实现类型
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 设置 netty 的接收缓冲区 （ByteBuf）,可以来测试半包
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16, 16, 16))
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // .handler(loggingHandler)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(loggingHandler);
                            ch.pipeline().addLast("echoHandler",echoServerHandler);
                        }
                    });
            // 绑定端口号，启动服务器
            ChannelFuture channelFuture = bootstrap.bind("127.0.0.1", 8080).sync();
            // 监听关闭通道
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error.", e);
        } finally {
            // 优雅关闭线程组
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

}
