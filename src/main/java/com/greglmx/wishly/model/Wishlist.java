package com.greglmx.wishly.model;

import java.util.List;

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

    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private List<Gift> gifts;

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

    public java.util.List<Gift> getGifts() {
        return this.gifts;
    }

    public void setGifts(java.util.List<Gift> gifts) {
        this.gifts = gifts;
    }
}
