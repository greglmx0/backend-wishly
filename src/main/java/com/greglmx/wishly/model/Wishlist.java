package com.greglmx.wishly.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnoreProperties({"wishlists", "password", "email"})
    private User owner;

    private String name;
    private String description;
    private Visibility visibility;
    public enum Visibility {
        PUBLIC,
        PRIVATE,
        FRIENDS_ONLY
    }

    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Gift> gifts;

    // Explicit accessors to avoid relying on Lombok at compile time in all environments
    public Long getId() {
        return this.id;
    }

    public User getOwner() {
        return this.owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
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
