package com.BOMBLAND_server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnvironmentVariablesController {
//    @Value("{local.server.port}")
//    private int port;

    @GetMapping("/get-environment-variables")
    public EnvironmentVariablesReponse getEnvironmentVariables() {
        EnvironmentVariablesReponse envVarResp = new EnvironmentVariablesReponse(System.getenv("IDENTITY_POOL_ID"));
        System.out.println("getEnvironmentVariables()");
        System.out.println(envVarResp);
        System.out.println("webflux server running on port = " + System.getenv("PORT"));

        return envVarResp;
    }
}

class EnvironmentVariablesReponse {
    private String identityPoolID;

    public EnvironmentVariablesReponse(String identityPoolID) {
        this.identityPoolID = identityPoolID;
    }

    public String getIdentityPoolID() {
        return identityPoolID;
    }
}