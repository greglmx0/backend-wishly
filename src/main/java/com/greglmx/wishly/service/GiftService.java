package com.greglmx.wishly.service;

import com.greglmx.wishly.dto.CreateGiftRequest;
import com.greglmx.wishly.dto.UpdateGiftRequest;
import com.greglmx.wishly.dto.GiftResponse;
import com.greglmx.wishly.exception.NotFoundException;
import com.greglmx.wishly.model.Gift;
import com.greglmx.wishly.model.Wishlist;
import com.greglmx.wishly.repository.GiftRepository;
import com.greglmx.wishly.repository.WishlistRepository;
import com.greglmx.wishly.validator.GiftValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.greglmx.wishly.security.UserPrincipal;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GiftService {
    private final GiftRepository giftRepository;
    private final WishlistRepository wishlistRepository;
    private final GiftValidator giftValidator;

    public GiftService(GiftRepository giftRepository,
                       WishlistRepository wishlistRepository,
                       GiftValidator giftValidator) {
        this.giftRepository = giftRepository;
        this.wishlistRepository = wishlistRepository;
        this.giftValidator = giftValidator;
    }

    public List<GiftResponse> listByWishlist(Long wishlistId) {
        ensureWishlistExists(wishlistId);
        return giftRepository.findByWishlistId(wishlistId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GiftResponse create(Long wishlistId, CreateGiftRequest request) {
        validate(request);
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new NotFoundException("Wishlist not found"));
        ensureOwner(wishlist);
        if (giftRepository.existsByWishlistIdAndNameIgnoreCase(wishlistId, request.getName())) {
            throw new IllegalArgumentException("Gift with same name already exists in wishlist");
        }
        Gift gift = new Gift();
        gift.setName(request.getName());
        gift.setDescription(request.getDescription());
        gift.setPrice(request.getPrice());
        gift.setImages(request.getImages() != null ? new java.util.ArrayList<>(request.getImages()) : null);
        gift.setTags(request.getTags() != null ? new java.util.ArrayList<>(request.getTags()) : null);
        gift.setUrl(request.getUrl());
        gift.setVisibility(request.getVisibility());
        gift.setWishlistId(wishlistId);
        gift = giftRepository.save(gift);

        wishlist.setCountGifts(wishlist.getCountGifts() + 1);
        wishlistRepository.save(wishlist);
        
        return toResponse(gift);
    }

    @Transactional
    public GiftResponse update(Long giftId, UpdateGiftRequest request) {
        validate(request);
        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new NotFoundException("Gift not found"));
        gift.setName(request.getName());
        gift.setDescription(request.getDescription());
        gift.setPrice(request.getPrice());
        gift.setImages(request.getImages() != null ? new java.util.ArrayList<>(request.getImages()) : null);
        gift.setTags(request.getTags() != null ? new java.util.ArrayList<>(request.getTags()) : null);
        gift.setUrl(request.getUrl());
        gift.setVisibility(request.getVisibility());
        gift = giftRepository.save(gift);
        return toResponse(gift);
    }

    @Transactional
    public void delete(Long giftId) {
        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new NotFoundException("Gift not found"));
        Wishlist wishlist = wishlistRepository.findById(gift.getWishlistId())
                .orElseThrow(() -> new NotFoundException("Wishlist not found"));
        ensureOwner(wishlist);
        giftRepository.delete(gift);

        wishlist.setCountGifts(wishlist.getCountGifts() - 1);
        wishlistRepository.save(wishlist);
    }

    private void validate(Object req) {
        BindingResult br = new BeanPropertyBindingResult(req, req.getClass().getSimpleName());
        giftValidator.validate(req, br);
        if (br.hasErrors()) {
            throw new IllegalArgumentException(br.getAllErrors().toString());
        }
    }

    private void ensureWishlistExists(Long wishlistId) {
        if (!wishlistRepository.existsById(wishlistId)) {
            throw new NotFoundException("Wishlist not found");
        }
    }

    private void ensureOwner(Wishlist wishlist) {
        Long ownerId = wishlist.getOwnerId();
        Long currentUserId = getCurrentUserId();
        if (ownerId == null || currentUserId == null || !ownerId.equals(currentUserId)) {
            throw new SecurityException("Not allowed: not the wishlist owner");
        }
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }

    private GiftResponse toResponse(Gift gift) {
        GiftResponse r = new GiftResponse();
        r.setId(gift.getId());
        r.setName(gift.getName());
        r.setDescription(gift.getDescription());
        r.setPrice(gift.getPrice());
        r.setImages(gift.getImages());
        r.setTags(gift.getTags());
        r.setUrl(gift.getUrl());
        r.setVisibility(gift.getVisibility());
        r.setWishlistId(gift.getWishlistId());
        return r;
    }
}
