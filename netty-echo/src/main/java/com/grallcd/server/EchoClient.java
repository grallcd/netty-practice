package com.grallcd.server;

import com.grallcd.handler.EchoClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;


/**
 * @author grallcd
 * @since 2021/11/5
 */
@Slf4j
public class EchoClient {

    public static void main(String[] args) {

        NioEventLoopGroup group = new NioEventLoopGroup();
        StringEncoder stringEncoder = new StringEncoder();
        EchoClientHandler echoClientHandler = new EchoClientHandler();

        try {
            // 创建客户端 bootStrap 对象，配置参数
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    // 设置客户端的通道实现
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(final SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("echoHandler", echoClientHandler);
                            ch.pipeline().addLast("stringEncoder", stringEncoder);
                        }
                    });

            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().addListener((ChannelFutureListener) future -> {
                group.shutdownGracefully();
            });

            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                // 按 q 退出
                while (true) {
                    String line = scanner.nextLine();
                    if ("q".equalsIgnoreCase(line)) {
                        channel.close();
                        break;
                    }
                    channel.writeAndFlush(line);

                    // 半包测试 (合并20次 发送一条消息，结果 server 返回多条消息)
                    /*StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 20; i++) {
                        sb.append(line);
                    }
                    channel.writeAndFlush(sb.toString());*/

                    // 粘包测试 (发送 10 次消息，结果 server 返回的消息数小于 10)
                    /*for (int i = 0; i < 10; i++) {
                        channel.writeAndFlush(line);
                    }*/
                }
            }).start();
        } catch (InterruptedException e) {
            log.error("client error.", e);
        }

    }

}
