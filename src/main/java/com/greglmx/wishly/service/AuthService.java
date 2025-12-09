package com.greglmx.wishly.service;

import com.greglmx.wishly.dto.LoginRequest;
import com.greglmx.wishly.dto.LoginResponse;
import com.greglmx.wishly.dto.UserInfo;
import com.greglmx.wishly.model.User;
import com.greglmx.wishly.repository.UserRepository;
import com.greglmx.wishly.util.JwtUtil;
import com.greglmx.wishly.exception.AlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse register(User user) {
        if (userRepository.findByUsername(user.getEmail()) != null ||
        userRepository.findByEmail(user.getEmail()) != null) {
            throw new AlreadyExistsException("Username or email already exists");
        }
        user.setUsername(user.getEmail());
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole(User.Role.USER);

            User response = userRepository.save(user);
            UserDetails userDetails = userService.loadUserByUsername(response.getEmail());
            String token = jwtUtil.generateToken(userDetails, response.getId());
            UserInfo userInfo = new UserInfo(response.getId(), response.getEmail());
            String message = "User %s registered successfully".formatted(response.getEmail());
            return new LoginResponse(message, token, userInfo);
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDetails userDetails = userService.loadUserByUsername(request.getEmail());
        User user = userService.getUserByEmail(request.getEmail());

        String token = jwtUtil.generateToken(userDetails, user != null ? user.getId() : null);
        String message = "User %s logged in successfully".formatted(request.getEmail());

        UserInfo userInfo = new UserInfo(user.getId(), user.getEmail());
        return new LoginResponse(message, token, userInfo);
    }
}
