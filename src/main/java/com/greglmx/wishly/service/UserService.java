package com.greglmx.wishly.service;

import com.greglmx.wishly.model.User;
import com.greglmx.wishly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // For Spring Security's loadUserByUsername, we still use this method but load by email
        User user = userRepository.findByEmailIgnoreCase(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }

    /**
     * Load user by email (used for authentication).
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Return the JPA User entity by username (kept for backward compatibility).
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}