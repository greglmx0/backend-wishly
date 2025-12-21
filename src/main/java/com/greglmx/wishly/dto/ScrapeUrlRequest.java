package com.greglmx.wishly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ScrapeUrlRequest {
    @NotBlank(message = "URL cannot be blank")
    @Pattern(regexp = "^(https?://).+", message = "url must be http(s)")
    private String url;
}
