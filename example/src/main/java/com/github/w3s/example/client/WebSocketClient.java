package com.github.w3s.example.client;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * @author wang xiao
 * date 2022/10/26
 */
public class WebSocketClient {

    private static String message = "{\"subs\":[{\"subId\":1,\"entityId\":\"1\",\"entityType\":\"device\",\"unSub\":false}]}";
    private final String uri = "ws://localhost:8080/ws/123";
    private Session session;

    public static void main(String[] args) throws IOException {
        WebSocketClient client = new WebSocketClient();
        client.start();
        client.getSession().getBasicRemote().sendText(message);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        do {
            line = reader.readLine();
            String exit = "exit";
            if (exit.equals(line)) {
                System.exit(0);
            }
        } while (true);
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    private void start() {
        WebSocketContainer webSocketContainer = null;
        try {
            webSocketContainer = ContainerProvider.getWebSocketContainer();
        } catch (Exception e) {
            System.out.println("Exception" + e.getMessage());
        }

        URI wsUri = URI.create(uri);
        try {
            session = webSocketContainer.connectToServer(WsClientHandler.class, wsUri);
        } catch (DeploymentException | IOException e) {
            e.printStackTrace();
        }
    }
}
