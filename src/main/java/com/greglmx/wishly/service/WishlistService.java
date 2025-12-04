package com.greglmx.wishly.service;

import com.greglmx.wishly.dto.SuccessCreateResponse;
import com.greglmx.wishly.dto.CreateWishlistRequest;
import com.greglmx.wishly.model.User;
import com.greglmx.wishly.model.Wishlist;
import com.greglmx.wishly.repository.UserRepository;
import com.greglmx.wishly.repository.WishlistRepository;
import com.greglmx.wishly.exception.NotFoundException;

import jakarta.transaction.Transactional;

import java.util.List;

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

        if (wishlistRepository.existsByOwnerIdAndName(owner.getId(), dto.getName())) {
            throw new IllegalArgumentException("Wishlist with the same name already exists");
        }

        Wishlist w = new Wishlist();
        w.setName(dto.getName());
        w.setDescription(dto.getDescription());
        w.setOwnerId(owner.getId());
        w.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : Wishlist.Visibility.PUBLIC);
        w.setCountGifts(0);

        Wishlist saved = wishlistRepository.save(w);
        return new SuccessCreateResponse("Wishlist %s created".formatted(saved.getName()));
    }

    public List<Wishlist> getWishlistsByOwnerId(Long ownerId) {
        User owner = userRepository.findById(ownerId);

        if (owner == null) {
            throw new NotFoundException("User not found: " + ownerId);
        }
        return wishlistRepository.findByOwnerId(owner.getId());
    }
}
