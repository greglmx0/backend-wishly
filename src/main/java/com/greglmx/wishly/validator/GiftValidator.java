package com.greglmx.wishly.validator;

import com.greglmx.wishly.dto.CreateGiftRequest;
import com.greglmx.wishly.dto.UpdateGiftRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class GiftValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return CreateGiftRequest.class.isAssignableFrom(clazz)
                || UpdateGiftRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        // Additional business rules beyond annotations
        if (target instanceof CreateGiftRequest req) {
            commonRules(req.getPrice(), req.getUrl(), errors);
        } else if (target instanceof UpdateGiftRequest req) {
            commonRules(req.getPrice(), req.getUrl(), errors);
        }
    }

    private void commonRules(Double price, String url, Errors errors) {
        if (price != null && price > 1000000) {
            errors.rejectValue("price", "price.tooHigh", "price exceeds limit");
        }
        if (url != null && url.length() > 2048) {
            errors.rejectValue("url", "url.tooLong", "url too long");
        }
    }
}
