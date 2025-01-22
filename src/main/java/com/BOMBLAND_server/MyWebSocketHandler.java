package com.BOMBLAND_server;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MyWebSocketHandler extends TextWebSocketHandler {
    // Set to keep track of connected clients
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        session.sendMessage(new TextMessage("Welcome to the WebSocket server!"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("handleTextMessage()");
        System.out.println("Received message: " + message.getPayload());
        broadcastHighScore(session, message.getPayload());
    }

    private void broadcastHighScore(WebSocketSession currentSession, String highScore) {
        System.out.println("broadcastHighScore()");

        // Broadcast the new high score to all connected clients
        synchronized (sessions) {
            for (WebSocketSession session: sessions) {
                if (currentSession == session) {
                    continue;
                }

                try {
                    session.sendMessage(new TextMessage(highScore)); // Send the message to each client
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("handleTransportError()");
        System.err.println("Error occurred with WebSocket connection: " + session.getId());
        exception.printStackTrace();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("afterConnectionClosed()");
        System.out.println("Connection closed: " + session.getId());
    }
}