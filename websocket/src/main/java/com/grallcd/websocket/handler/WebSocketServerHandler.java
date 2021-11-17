package com.grallcd.websocket.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 处理 WebSocketFrame 协议的 ChannelHandler，处理消息帧
 *
 * @author grallcd
 * @since 2021/11/17
 */
@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    // 用于保存连接的 channel
    private static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        // 将 channel 添加到 channelGroup 中
        CHANNEL_GROUP.add(channel);

        ChannelId id = channel.id();
        log.info("id 为 {} 的用户加入了聊天室,在线人数为:{}", id, CHANNEL_GROUP.size());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();

        boolean flag = CHANNEL_GROUP.contains(channel);
        log.info("contains ? {}", flag);

        // 连接正常断开后会自动删除，此处删除预防非正常断开
        CHANNEL_GROUP.remove(channel);
        log.info("id 为 {} 的用户退出了聊天室,在线人数为:{}", channel.id(), CHANNEL_GROUP.size());
        // 用户退出，广播消息
        String msg = String.format("用户 %s 退出了聊天室.", channel.id());
        CHANNEL_GROUP.writeAndFlush(new TextWebSocketFrame(msg));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        log.info("frame type:{}", frame.getClass().getSimpleName());

        Channel currentChannel = ctx.channel();
        ChannelId currentId = currentChannel.id();

        // 判断是否是 ping 消息
        if (frame instanceof PingWebSocketFrame) {
            currentChannel.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        // 判断是否是关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            currentChannel.close();
            return;
        }
        // 本例程仅支持文本消息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            log.warn("仅支持文本消息，暂不支持二进制消息.");
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }

        String now = LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
        String text = ((TextWebSocketFrame) frame).text();
        String commonMsg = String.format("[%s] [%s]\t %s", now, currentChannel.id(), text);
        TextWebSocketFrame frameMsg = new TextWebSocketFrame(commonMsg);
        // 群发消息
        String tmp = currentId.asShortText();
        CHANNEL_GROUP.stream()
                .filter(e -> !tmp.equals(e.id().asShortText()))
                .forEach(channel -> {
                    channel.writeAndFlush(frameMsg);
                });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server error.", cause);
        Channel channel = ctx.channel();
        CHANNEL_GROUP.writeAndFlush(String.format("[%s] 用户 %s 出现异常掉线！", LocalDateTime.now(), channel.id()));
        ctx.close();
    }
}