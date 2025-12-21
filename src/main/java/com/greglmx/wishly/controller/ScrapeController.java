package com.greglmx.wishly.controller;

import com.greglmx.wishly.dto.ScrapeUrlRequest;
import com.greglmx.wishly.dto.ScrapeResponse;
import com.greglmx.wishly.service.ScraperService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class ScrapeController {

    private final ScraperService scraperService;

    public ScrapeController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    /**
     * Scrape product information from a URL
     * POST /api/scrape-url
     * 
     * @param request Contains the URL to scrape
     * @return ScrapeResponse with extracted product data or error message
     */
    @PostMapping("/scrape-url")
    public ResponseEntity<ScrapeResponse> scrapeUrl(@Valid @RequestBody ScrapeUrlRequest request) {
        try {
            log.info("Starting scrape for URL: {}", request.getUrl());
            
            var giftResponse = scraperService.scrapeUrl(request.getUrl());
            ScrapeResponse response = new ScrapeResponse(giftResponse);
            
            log.info("Successfully scraped URL: {}", request.getUrl());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid URL: {} - {}", request.getUrl(), e.getMessage());
            ScrapeResponse response = new ScrapeResponse(e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (RuntimeException e) {
            log.error("Error scraping URL: {} - {}", request.getUrl(), e.getMessage());
            ScrapeResponse response = new ScrapeResponse(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
