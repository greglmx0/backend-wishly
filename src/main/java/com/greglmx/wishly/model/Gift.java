package com.greglmx.wishly.model;

import jakarta.persistence.Id;
import java.util.List;

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
}
