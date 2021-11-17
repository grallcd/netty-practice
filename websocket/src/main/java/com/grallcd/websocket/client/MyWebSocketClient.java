package com.grallcd.websocket.client;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * @author grallcd
 * @since 2021/11/17
 */
@Slf4j
public class MyWebSocketClient {

    public static void main(String[] args) throws URISyntaxException, InterruptedException {

        // 此处无法使用回环地址
        String url = "ws://10.0.3.119:9090/websocket";

        WebSocketClient client = initClient(url);
        client.connectBlocking();

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            // 按 q 退出
            while (true) {
                String line = scanner.nextLine();
                if ("q".equalsIgnoreCase(line)) {
                    client.close();
                    break;
                }
                client.send(line);
            }
        }).start();
    }

    private static WebSocketClient initClient(String url) throws URISyntaxException {
        return new WebSocketClient(new URI(url)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                log.info("连接成功...");
            }

            @Override
            public void onMessage(String message) {
                log.info(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log.info("连接关闭...");
            }

            @Override
            public void onError(Exception ex) {
                log.error("client error", ex);
            }
        };
    }
}
