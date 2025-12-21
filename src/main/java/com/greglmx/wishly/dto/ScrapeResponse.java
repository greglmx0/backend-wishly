package com.greglmx.wishly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScrapeResponse {
    private boolean success;
    private GiftResponse data;
    private String error;

    public ScrapeResponse(GiftResponse data) {
        this.success = true;
        this.data = data;
        this.error = null;
    }

    public ScrapeResponse(String error) {
        this.success = false;
        this.data = null;
        this.error = error;
    }
}
