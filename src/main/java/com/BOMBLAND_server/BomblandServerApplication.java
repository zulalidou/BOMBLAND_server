package com.BOMBLAND_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;

@SpringBootApplication
public class BomblandServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(BomblandServerApplication.class, args);
		System.out.println("webflux server running on port = " + System.getProperty("local.server.port"));


		int port = 443; // The port to listen on
		BOMBLAND_WebSocketServer server = new BOMBLAND_WebSocketServer(port);
		server.start();
		System.out.println("Server started on port " + port);
	}
}