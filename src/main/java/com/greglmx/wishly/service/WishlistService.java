package com.greglmx.wishly.service;

import com.greglmx.wishly.dto.CreateWishlistRequest;
import com.greglmx.wishly.model.User;
import com.greglmx.wishly.model.Wishlist;
import com.greglmx.wishly.repository.UserRepository;
import com.greglmx.wishly.repository.WishlistRepository;
import com.greglmx.wishly.exception.NotFoundException;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class WishlistService {

    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private UserRepository userRepository;

    @Transactional
    public Wishlist createWishlist(CreateWishlistRequest dto, Long userId) {
        User owner = userRepository.findById(userId);
        if (owner == null) {
            throw new UsernameNotFoundException("User not found: " + userId);
        }

        if (wishlistRepository.existsByOwnerIdAndName(owner.getId(), dto.getName())) {
            throw new IllegalArgumentException("Wishlist with the same name already exists");
        }

        Wishlist w = new Wishlist();
        w.setName(dto.getName());
        w.setDescription(dto.getDescription());
        w.setOwnerId(owner.getId());
        Wishlist.Visibility vis = Wishlist.Visibility.PUBLIC;
        if (dto.getVisibility() != null) {
            try {
                vis = Wishlist.Visibility.valueOf(dto.getVisibility());
            } catch (IllegalArgumentException ex) {
                vis = Wishlist.Visibility.PUBLIC;
            }
        }
        w.setVisibility(vis);
        w.setCountGifts(0);

        return wishlistRepository.save(w);
    }

    public List<Wishlist> getWishlistsByOwnerId(Long ownerId) {
        User owner = userRepository.findById(ownerId);

        if (owner == null) {
            throw new NotFoundException("User not found: " + ownerId);
        }
        List<Wishlist> wishlists = wishlistRepository.findByOwnerId(owner.getId());
        return wishlists != null ? wishlists : Collections.emptyList();
    }

    public Wishlist updateWishlist(Wishlist wishlistUpdates) {
        Wishlist existing = wishlistRepository.findById(wishlistUpdates.getId()).orElse(null);
        if (existing == null) {
            throw new NotFoundException("Wishlist not found: " + wishlistUpdates.getId());
        }

        if (wishlistUpdates.getName() != null) {
            existing.setName(wishlistUpdates.getName());
        }
        if (wishlistUpdates.getDescription() != null) {
            existing.setDescription(wishlistUpdates.getDescription());
        }
        if (wishlistUpdates.getVisibility() != null) {
            existing.setVisibility(wishlistUpdates.getVisibility());
        }
        if (wishlistUpdates.getCountGifts() != null) {
            existing.setCountGifts(wishlistUpdates.getCountGifts());
        }
        // Do NOT allow changing ownerId via updates

        return wishlistRepository.save(existing);
    }

    public void deleteWishlist(Long wishlistId) {
        wishlistRepository.deleteById(wishlistId);
    }

    public Boolean ChekOwnerWishlist(Long wishlistId, Long userId) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId).orElse(null);
        if (wishlist == null) {
            throw new NotFoundException("Wishlist not found: " + wishlistId);
        }
        return wishlist.getOwnerId().equals(userId);
    }
}
