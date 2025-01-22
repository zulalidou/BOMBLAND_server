package com.BOMBLAND_server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;


@Configuration
public class WebFluxRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions
                // Define routes
                .route(RequestPredicates.GET("/webflux/"), this::handleDefaultRoute)
                .andRoute(RequestPredicates.GET("/webflux/route2"), this::handleRoute2)
                .andRoute(RequestPredicates.GET("/webflux/get-environment-variables"), this::handleGetEnvironmentVariables);
    }


    private Mono<ServerResponse> handleDefaultRoute(ServerRequest request) {
        return ServerResponse.ok().bodyValue("\n---handleDefaultRoute---...\n");
    }

    private Mono<ServerResponse> handleRoute2(ServerRequest request) {
        return ServerResponse.ok().bodyValue("\n---handleRoute2---...\n");
    }

    private Mono<ServerResponse> handleGetEnvironmentVariables(ServerRequest request) {
        return ServerResponse.ok().bodyValue("\n---handleGetEnvironmentVariables---...\n");
    }
}