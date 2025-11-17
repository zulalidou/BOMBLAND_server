package com.BOMBLAND_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main class.
 */
@SpringBootApplication
public class BomblandServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(BomblandServerApplication.class, args);
		System.out.println("\n===BomblandServerApplication===\n");
	}
}