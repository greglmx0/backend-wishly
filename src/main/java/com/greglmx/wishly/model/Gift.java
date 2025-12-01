package com.greglmx.wishly.model;

import jakarta.persistence.Id;
import java.util.ArrayList;

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
    private ArrayList<String> images;
    private ArrayList<String> tags;
    private String url;

    @ManyToOne
    private Wishlist wishlist;
    private Visibility visibility;
    public enum Visibility {
        PUBLIC,
        DISABLED,
        PRIVATE
    }
}
