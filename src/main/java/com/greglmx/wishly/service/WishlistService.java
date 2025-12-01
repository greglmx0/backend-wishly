package com.greglmx.wishly.service;

import com.greglmx.wishly.dto.SuccessCreateResponse;
import com.greglmx.wishly.dto.CreateWishlistRequest;
import com.greglmx.wishly.model.User;
import com.greglmx.wishly.model.Wishlist;
import com.greglmx.wishly.repository.UserRepository;
import com.greglmx.wishly.repository.WishlistRepository;

import jakarta.transaction.Transactional;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class WishlistService {

    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private UserRepository userRepository;

    @Transactional
    public SuccessCreateResponse createWishlist(CreateWishlistRequest dto, String username) {
        User owner = userRepository.findByUsername(username);
        if (owner == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // check for duplicate wishlist names for the same user could be added here
        if (wishlistRepository.existsByOwnerAndName(owner, dto.getName())) {
            throw new IllegalArgumentException("Wishlist with the same name already exists");
        }

        Wishlist w = new Wishlist();
        w.setName(dto.getName());
        w.setDescription(dto.getDescription());
        w.setOwner(owner);

        Wishlist saved = wishlistRepository.save(w);
        return new SuccessCreateResponse("Wishlist %s created".formatted(saved.getId()));
    }

    public ArrayList<Wishlist> getWishlistsByOwnerId(String username) {
        User owner = userRepository.findByUsername(username);
        System.out.println("Fetching wishlists for user: ");
        System.out.println("Fetching wishlists for user: " + owner);

        if (owner == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return wishlistRepository.findByOwnerId(owner.getId());
    }
}
