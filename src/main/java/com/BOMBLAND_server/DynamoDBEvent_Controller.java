package com.BOMBLAND_server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DynamoDBEvent_Controller {
    @PostMapping("/database-changes")
    public ResponseEntity<String> handleInserts(@RequestBody String event) {
//        System.out.println("Received DynamoDB Event: " + event.getMessage());
        System.out.println("Received DynamoDB Event: " + event);
        return ResponseEntity.ok("Request processed successfully!");
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