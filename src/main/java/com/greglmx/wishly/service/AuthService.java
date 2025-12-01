package com.greglmx.wishly.service;

import com.greglmx.wishly.model.AuthenticationRequest;
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

    public String register(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null ||
        userRepository.findByEmail(user.getEmail()) != null) {
            throw new AlreadyExistsException("Username or email already exists");
        }
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole(User.Role.USER);

        User responce = userRepository.save(user);
        return "User %s registered successfully".formatted(responce.getUsername());
    }

    public String login(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
        return jwtUtil.generateToken(userDetails);
    }
}
