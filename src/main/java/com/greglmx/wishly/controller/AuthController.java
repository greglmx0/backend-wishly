package com.greglmx.wishly.controller;

import com.greglmx.wishly.dto.LoginRequest;
import com.greglmx.wishly.dto.LoginResponse;
import com.greglmx.wishly.dto.SuccessCreateResponse;
import com.greglmx.wishly.model.User;
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
    public ResponseEntity<SuccessCreateResponse> registerUser(@Valid @RequestBody User user) {
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Secure World!";
    }
}