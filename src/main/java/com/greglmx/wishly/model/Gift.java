package com.greglmx.wishly.model;

import jakarta.persistence.Id;
import java.util.List;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Gift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Double price;
    @ElementCollection
    private List<String> images;
    @ElementCollection
    private List<String> tags;
    private String url;

    private Long wishlistId;
    private Visibility visibility;
    public enum Visibility {
        PUBLIC,
        DISABLED,
        PRIVATE
    }

    @Column(updatable = false)
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
