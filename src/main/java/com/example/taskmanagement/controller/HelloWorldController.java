package com.example.taskmanagement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is a simple controller to test if the application is running.
 * It provides a single endpoint that returns a "Hello, World!" message.
 */
@RestController
public class HelloWorldController {

    /**
     * This method handles HTTP GET requests to the "/hello" endpoint.
     *
     * @return A simple string "Hello, World!".
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
}
