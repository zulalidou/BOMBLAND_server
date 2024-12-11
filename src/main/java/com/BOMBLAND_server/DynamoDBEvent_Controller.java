package com.BOMBLAND_server;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/database-changes")
public class DynamoDBEvent_Controller {
    @PostMapping
    public Mono<Void> handleInserts(@RequestBody DynamoDB_Event event) {
        // Print the received message from the DynamoDB stream
        System.out.println("Received DynamoDB Event: " + event.getMessage());
        return Mono.empty();
    }
}

class DynamoDB_Event {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}