package com.BOMBLAND_server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;


public class BOMBLAND_WebSocketServer extends WebSocketServer {
    private List<WebSocket> clients = new ArrayList<>();

    public BOMBLAND_WebSocketServer(InetSocketAddress address) {
        super(address);
    }


    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String path = handshake.getResourceDescriptor();

        System.out.println("onOpen()");
        System.out.println("conn: " + conn);
        System.out.println("handshake: " + handshake);
        System.out.println("path: " + path + "\n");

//        if (path.equals("/game")) {
//            // Handle WebSocket for game connections
//            System.out.println("Handling game WebSocket connection");
//            conn.send("Welcome to the Game WebSocket!");
//        } else if (path.equals("/chat")) {
//            // Handle WebSocket for chat connections
//            System.out.println("Handling chat WebSocket connection");
//            conn.send("Welcome to the Chat WebSocket!");
//        } else if (path.equals("/wss")) {
            // Add the new client connection to the list
            clients.add(conn);
            System.out.println("New client connected: " + conn.getRemoteSocketAddress());

            // Handle WebSocket for admin connections
            System.out.println("Handling admin WebSocket connection");
            conn.send("Welcome to the /wss WebSocket!");
//        } else {
//            // Close the connection if the path is unknown
//            System.out.println("Unknown WebSocket path: " + path);
//            conn.send("Unknown WebSocket path: " + path);
//            conn.close();
//        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("onClose()");
        System.out.println("conn: " + conn);
        System.out.println("code: " + code);
        System.out.println("reason: " + reason);

        // Remove the client from the list when it disconnects
        clients.remove(conn);
        System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("onMessage()");
        System.out.println("conn: " + conn);
        System.out.println("message: " + message);

        // When a new high score is received, broadcast it to all other clients
        broadcastNewHighScore(message, conn);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("onError()");
        System.out.println("==============");
        System.out.println(ex.getCause());
        System.out.println("--------------");
        System.out.println(ex.getMessage());
        System.out.println("--------------");
        ex.printStackTrace();
        System.out.println("==============");
    }

    @Override
    public void onStart() {
        System.out.println("\nonStart()");
        System.out.println("onStart() == WebSocket server started successfully == onStart()\n");
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