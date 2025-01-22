//package com.BOMBLAND_server;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.function.server.*;
//import org.springframework.web.reactive.function.server.ServerRequest;
//import reactor.core.publisher.Mono;
//
//
//@Configuration
//public class WebFluxRouterConfig {
//    @Bean
//    public RouterFunction<ServerResponse> routes() {
//        return RouterFunctions.route()
//                .nest(RequestPredicates.path("/webflux"), builder -> builder
//                    .GET("/", this::handleDefaultRoute)
//                    .GET("/route2", this::handleRoute2)
//                    .GET("/get-environment-variables", this::getEnvironmentVariables))
//                .build();
//    }
//
//
//    private Mono<ServerResponse> handleDefaultRoute(ServerRequest request) {
//        return ServerResponse.ok().bodyValue("\n---handleDefaultRoute---...\n");
//    }
//
//    private Mono<ServerResponse> handleRoute2(ServerRequest request) {
//        return ServerResponse.ok().bodyValue("\n---handleRoute2---...\n");
//    }
//
//    private Mono<ServerResponse> getEnvironmentVariables(ServerRequest request) {
//        EnvironmentVariablesReponse envVarResp = new EnvironmentVariablesReponse(System.getenv("IDENTITY_POOL_ID"));
//        System.out.println("getEnvironmentVariables()");
//        System.out.println(envVarResp);
//        System.out.println("webflux server running on port = " + System.getenv("PORT"));
//
//        return ServerResponse.ok().bodyValue(envVarResp);
//    }
//}