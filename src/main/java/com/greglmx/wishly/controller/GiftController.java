package com.greglmx.wishly.controller;

import com.greglmx.wishly.dto.CreateGiftRequest;
import com.greglmx.wishly.dto.UpdateGiftRequest;
import com.greglmx.wishly.dto.GiftResponse;
import com.greglmx.wishly.service.GiftService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class GiftController {
    private final GiftService giftService;

    public GiftController(GiftService giftService) {
        this.giftService = giftService;
    }

    @GetMapping("/wishlist/{wishlistId}/gifts")
    public ResponseEntity<List<GiftResponse>> list(@PathVariable Long wishlistId) {
        return ResponseEntity.ok(giftService.listByWishlist(wishlistId));
    }

    @PostMapping("/wishlist/{wishlistId}/gifts")
    public ResponseEntity<GiftResponse> create(@PathVariable Long wishlistId,
                                       @Valid @RequestBody CreateGiftRequest request) {
        return ResponseEntity.ok(giftService.create(wishlistId, request));
    }

    @PutMapping("/gifts/{giftId}")
    public ResponseEntity<GiftResponse> update(@PathVariable Long giftId,
                                       @Valid @RequestBody UpdateGiftRequest request) {
        return ResponseEntity.ok(giftService.update(giftId, request));
    }

    @DeleteMapping("/gifts/{giftId}")
    public ResponseEntity<Void> delete(@PathVariable Long giftId) {
        giftService.delete(giftId);
        return ResponseEntity.noContent().build();
    }
}
