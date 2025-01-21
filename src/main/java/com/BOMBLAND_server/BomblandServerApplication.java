package com.BOMBLAND_server;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
public class BomblandServerApplication {
	public static void main(String[] args) throws URISyntaxException {
		SpringApplication.run(BomblandServerApplication.class, args);
		System.out.println("webflux server running on port = " + System.getProperty("local.server.port"));
		System.out.println("System.getenv(\"PORT\") = " + System.getenv("PORT"));

//		BOMBLAND_WebSocketServer server = new BOMBLAND_WebSocketServer(new URI("ws://localhost"));
//		server.start();
//		System.out.println("Server started on port " + port);
	}

//	@PostConstruct
//	public void startWebSocketServer() {
//		System.out.println("\nstartWebSocketServer() - start");
//
//		BOMBLAND_WebSocketServer myWebSocketServer = new BOMBLAND_WebSocketServer(new InetSocketAddress("0.0.0.0", 8081));
////		BOMBLAND_WebSocketServer myWebSocketServer = new BOMBLAND_WebSocketServer(new InetSocketAddress("localhost", 443));
//		myWebSocketServer.start();
//
//		System.out.println("startWebSocketServer() - end\n");
//	}
}