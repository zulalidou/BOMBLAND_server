package com.BOMBLAND_server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DynamoDBEvent_Controller {
    @PostMapping("/database-changes")
    public ResponseEntity<String> handleInserts(@RequestBody HighScore newHighScore) {
        System.out.println("newHighScore.id = " + newHighScore.getID());
        System.out.println("newHighScore.score = " + newHighScore.getScore());
        System.out.println("newHighScore.name = " + newHighScore.getName());

        return ResponseEntity.ok("Request processed successfully!");
    }
}

class HighScore {
    private String id;
    private int score;
    private String name;

    public String getID() {
        return id;
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }
}