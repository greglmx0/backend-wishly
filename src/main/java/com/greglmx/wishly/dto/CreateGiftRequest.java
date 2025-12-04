package com.greglmx.wishly.dto;

import com.greglmx.wishly.model.Gift.Visibility;
import jakarta.validation.constraints.*;
import java.util.List;

import lombok.Data;

@Data
public class CreateGiftRequest {
    @NotBlank
    private String name;

    @Size(max = 1024)
    private String description;

    @Positive
    @Digits(integer = 10, fraction = 2)
    private Double price;

    private List<@NotBlank String> images;

    private List<@NotBlank String> tags;

    @Size(max = 2048)
    @Pattern(regexp = "^(https?://).+", message = "url must be http(s)")
    private String url;

    @NotNull
    private Visibility visibility;
}
