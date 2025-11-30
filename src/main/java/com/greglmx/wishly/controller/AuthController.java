package com.greglmx.wishly.controller;

import com.greglmx.wishly.model.*;
import com.greglmx.wishly.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody User user) {
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Secure World!";
    }

    // Validation and error handling moved to GlobalExceptionHandler
}