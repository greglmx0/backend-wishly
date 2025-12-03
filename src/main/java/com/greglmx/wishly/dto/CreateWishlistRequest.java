
package com.greglmx.wishly.dto;

import com.greglmx.wishly.model.Wishlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateWishlistRequest {

    @NotBlank(message = "name is required")
    @Size(max = 200, message = "name must be at most 200 characters")
    private String name;

    @Size(max = 2000, message = "description must be at most 2000 characters")
    private String description;
    
    private String visibility;

    public CreateWishlistRequest() {}

    public CreateWishlistRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Wishlist.Visibility getVisibility() {
        return Wishlist.Visibility.valueOf(visibility);
    }
    public void setVisibility( Wishlist.Visibility visibility) {
        this.visibility = visibility.name();
    }
}