package com.greglmx.wishly.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // exclude owner from JSON serialization to prevent circular references
    @JsonIgnore
    private Long ownerId;

    private String name;
    private String description;
    private Visibility visibility;
    public enum Visibility {
        PUBLIC,
        PRIVATE,
        FRIENDS_ONLY
    }

    // @OneToMany(mappedBy = "wishlistId", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Gift> gifts;

    private Integer countGifts;

    public Long getId() {
        return this.id;
    }

    public Long getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // public java.util.List<Gift> getGifts() {
    //     return this.gifts;
    // }

    // public void setGifts(java.util.List<Gift> gifts) {
    //     this.gifts = gifts;
    // }

    public Wishlist.Visibility getVisibility() {
        return this.visibility;
    }

    public void setVisibility(Wishlist.Visibility visibility) {
        this.visibility = visibility;
    }

    public Integer getCountGifts() {
        return this.countGifts;
    }

    public void setCountGifts(Integer countGifts) {
        this.countGifts = countGifts;
    }
}
