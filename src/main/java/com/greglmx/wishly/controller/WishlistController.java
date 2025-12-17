package com.greglmx.wishly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.greglmx.wishly.security.UserPrincipal;
import com.greglmx.wishly.dto.CreateWishlistRequest;
import com.greglmx.wishly.service.WishlistService;
import com.greglmx.wishly.model.Wishlist;

import java.util.List;

import jakarta.validation.Valid;

@RestController
public class WishlistController {

     @Autowired
    private WishlistService wishlistService;
    
    @PostMapping("/wishlists")
    public ResponseEntity<Wishlist> createWishlist(
            @Valid @RequestBody CreateWishlistRequest req,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Wishlist created = wishlistService.createWishlist(req, userPrincipal.getId());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/wishlists")
    public ResponseEntity<List<Wishlist>> getWishlistsByOwner(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Wishlist> wishlists = wishlistService.getWishlistsByOwnerId(userPrincipal.getId());
        return ResponseEntity.ok(wishlists);
            }

    @PutMapping("/wishlist/{id}")
    public ResponseEntity<Wishlist> updateWishlist
            (@PathVariable Long id,
             @Valid @RequestBody Wishlist wishlistUpdates,
             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        wishlistUpdates.setId(id);
        Wishlist updatedWishlist = wishlistService.updateWishlist(wishlistUpdates);
        return ResponseEntity.ok(updatedWishlist);
            }

    @DeleteMapping("/wishlist/{id}")
    public ResponseEntity<Void> deleteWishlist(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        wishlistService.deleteWishlist(id);
        return ResponseEntity.noContent().build();
            }

    @GetMapping("/wishlist/{id}/check-owner")
    public ResponseEntity<Boolean> checkOwner(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Boolean isOwner = wishlistService.ChekOwnerWishlist(id, userPrincipal.getId());
        return ResponseEntity.ok(isOwner);
    }
}
