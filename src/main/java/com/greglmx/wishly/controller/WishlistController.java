package com.greglmx.wishly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.greglmx.wishly.security.UserPrincipal;
import com.greglmx.wishly.dto.CreateWishlistRequest;
import com.greglmx.wishly.dto.SuccessCreateResponse;
import com.greglmx.wishly.service.WishlistService;
import com.greglmx.wishly.model.Wishlist;

import java.util.List;

import jakarta.validation.Valid;

@RestController
public class WishlistController {

     @Autowired
    private WishlistService wishlistService;
    
    @PostMapping("/wishlists")
    public ResponseEntity<SuccessCreateResponse> createWishlist(
            @Valid @RequestBody CreateWishlistRequest req,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        SuccessCreateResponse resp = wishlistService.createWishlist(req, userPrincipal.getUsername());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/wishlists")
    public ResponseEntity<List<Wishlist>> getWishlistsByOwner(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Wishlist> wishlists = wishlistService.getWishlistsByOwnerId(userPrincipal.getId());
        return ResponseEntity.ok(wishlists);
            }
}
