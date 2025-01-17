package com.BOMBLAND_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;

@SpringBootApplication
public class BomblandServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(BomblandServerApplication.class, args);

		int port = 8081; // The port to listen on
		BOMBLAND_WebSocketServer server = new BOMBLAND_WebSocketServer(port);
		server.start();
		System.out.println("Server started on port " + port);
	}
}