package com.grallcd.websocket.server;

import com.grallcd.websocket.handler.WebSocketServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author grallcd
 * @since 2021/11/17
 */
@Slf4j
public class WebsocketServer {

    public static void main(String[] args) {

        int port = 9090;
        WebsocketServer websocketServer = new WebsocketServer();
        websocketServer.run(port);

    }

    private void run(int port) {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("httpCodec", new HttpServerCodec())
                                    // 聚合成一个完整的请求或响应 FullHttpRequest or FullHttpResponse
                                    .addLast("aggregator", new HttpObjectAggregator(65535))
                                    // 支持异步发送大的码流但不占用过多内存
                                    .addLast("http-chunked", new ChunkedWriteHandler())
                                    .addLast(new IdleStateHandler(8, 10, 12))
                                    // 将 http 协议升级成 ws 协议，并保持长连接
                                    .addLast(new WebSocketServerProtocolHandler("/websocket"))
                                    // .addLast("httpHandler", new FullHttpRequestHandler())
                                    .addLast("websocketHandler", new WebSocketServerHandler());
                        }
                    });

            Channel channel = bootstrap.bind(port).sync().channel();
            log.info("websocket server started at port:{}.", port);
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error.", e);
        } finally {
            // 优雅关闭
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
