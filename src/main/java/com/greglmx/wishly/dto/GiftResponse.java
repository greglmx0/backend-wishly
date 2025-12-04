package com.greglmx.wishly.dto;

import com.greglmx.wishly.model.Gift.Visibility;
import lombok.Data;

import java.util.List;

@Data
public class GiftResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private List<String> images;
    private List<String> tags;
    private String url;
    private Visibility visibility;
    private Long wishlistId;
}
