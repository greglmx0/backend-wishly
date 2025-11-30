package com.greglmx.wishly.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private Instant timestamp = Instant.now();
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldValidationError> errors;

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<FieldValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldValidationError> errors) {
        this.errors = errors;
    }

    public static class FieldValidationError {
        private String field;
        private Object rejectedValue;
        private String message;
        private String code;

        public FieldValidationError() {}

        public FieldValidationError(String field, Object rejectedValue, String message, String code) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
            this.code = code;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
