package com.BOMBLAND_server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;


public class BOMBLAND_WebSocketServer extends WebSocketServer {
    private List<WebSocket> clients = new ArrayList<>();

    public BOMBLAND_WebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // Add the new client connection to the list
        clients.add(conn);
        System.out.println("New client connected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Remove the client from the list when it disconnects
        clients.remove(conn);
        System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message: " + message);

        // When a new high score is received, broadcast it to all other clients
        broadcastNewHighScore(message, conn);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started successfully.");
    }

    // Method to broadcast the new high score to all other connected clients
    private void broadcastNewHighScore(String message, WebSocket sender) {
        System.out.println("broadcastNewHighScore()");
        System.out.println("sender:" + sender);
        System.out.println("\nactive clients:");

        for (WebSocket client : clients) {
            System.out.println("client:" + client);

            if (client != sender && client.isOpen()) {
                client.send("New high score: " + message);  // Send to all clients except the sender
            }
        }
    }
}