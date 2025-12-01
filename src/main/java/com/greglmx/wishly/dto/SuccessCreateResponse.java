package com.greglmx.wishly.dto;

public class SuccessCreateResponse {
    private String message;

    public SuccessCreateResponse() {
    }

    public SuccessCreateResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}