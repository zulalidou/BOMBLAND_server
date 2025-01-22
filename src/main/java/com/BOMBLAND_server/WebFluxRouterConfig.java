package com.BOMBLAND_server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;


@Configuration
public class WebFluxRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions
                .route(RequestPredicates.GET("/get-environment-variables"), this::handleGetEnvironmentVariables);
    }

    private Mono<ServerResponse> handleGetEnvironmentVariables(ServerRequest request) {
        return ServerResponse.ok().bodyValue("Environment variables...");
    }
}