package com.example.taskmanagement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is a simple controller to test if the application is running.
 * It provides a single endpoint that returns a "Connection Successful" message.
 */
@RestController
public class TestController {
    /**
     * This method handles HTTP GET requests to the "test" endpoint.
     *
     * @return A simple string "Connection Successful".
     */
    @GetMapping("/test")
    public String test() {
        return "Connection Successful";
    }
}
