package com.BOMBLAND_server;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
  /**
   * This method is used to configure and map WebSocket handlers to specific URL endpoints.
   *
   * @param registry An object used for mapping and configuring WebSocket handlers.
   */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new MyWebSocketHandler(), "/websocket/establish-server-connection")
                .setAllowedOrigins("*");
    }
}