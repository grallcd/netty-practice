package com.grallcd.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author grallcd
 * @since 2021/11/5
 */
@Slf4j
@ChannelHandler.Sharable
public class EchoClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("connect to server,server address:{}", ctx.channel().remoteAddress());
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes("hello world".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(buffer);
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        log.info("receive message from server: {}", buf.toString(CharsetUtil.UTF_8));
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client error.", cause);
        ctx.close();
    }
}
