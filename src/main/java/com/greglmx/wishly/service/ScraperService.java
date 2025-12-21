package com.greglmx.wishly.service;

import com.greglmx.wishly.dto.GiftResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ScraperService {

    private static final int TIMEOUT_MS = 10000; // 10 seconds timeout
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    /**
     * Scrape product information from a given URL
     * Strategy:
     * 1. Extract meta tags (Open Graph, Twitter Card)
     * 2. Extract title and description
     * 3. Extract images
     * 4. Detect price with regex patterns
     */
    public GiftResponse scrapeUrl(String url) {
        try {
            // Validate URL format
            new URI(url).toURL();

            // Fetch and parse HTML document
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .get();

            GiftResponse response = new GiftResponse();
            response.setUrl(url);

            // Extract title from Open Graph or regular title
            String title = extractTitle(doc);
            response.setName(title);

            // Extract description from meta tags
            String description = extractDescription(doc);
            response.setDescription(description);

            // Extract images
            List<String> images = extractImages(doc, url);
            response.setImages(images);

            // Extract price
            Double price = extractPrice(doc);
            response.setPrice(price);

            return response;

        } catch (MalformedURLException e) {
            log.error("Malformed URL: {}", url, e);
            throw new IllegalArgumentException("Invalid URL format: " + url);
        } catch (URISyntaxException e) {
            log.error("Invalid URL syntax: {}", url, e);
            throw new IllegalArgumentException("Invalid URL format: " + url);
        } catch (IOException e) {
            log.error("Error scraping URL: {}", url, e);
            throw new RuntimeException("Unable to scrape the page: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error scraping URL: {}", url, e);
            throw new RuntimeException("Unexpected error while scraping: " + e.getMessage());
        }
    }

    /**
     * Extract title from:
     * 1. og:title (Open Graph)
     * 2. twitter:title (Twitter Card)
     * 3. <title> tag
     */
    private String extractTitle(Document doc) {
        // Try Open Graph title
        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null) {
            String title = ogTitle.attr("content");
            if (title != null && !title.isBlank()) {
                return title.trim();
            }
        }

        // Try Twitter Card title
        Element twitterTitle = doc.selectFirst("meta[name=twitter:title]");
        if (twitterTitle != null) {
            String title = twitterTitle.attr("content");
            if (title != null && !title.isBlank()) {
                return title.trim();
            }
        }

        // Try regular title tag
        Element titleTag = doc.selectFirst("title");
        if (titleTag != null) {
            String title = titleTag.text();
            if (title != null && !title.isBlank()) {
                return title.trim();
            }
        }

        return "Product from " + extractDomain(doc.location());
    }

    /**
     * Extract description from:
     * 1. og:description (Open Graph)
     * 2. twitter:description (Twitter Card)
     * 3. meta description
     */
    private String extractDescription(Document doc) {
        // Try Open Graph description
        Element ogDesc = doc.selectFirst("meta[property=og:description]");
        if (ogDesc != null) {
            String desc = ogDesc.attr("content");
            if (desc != null && !desc.isBlank()) {
                return desc.trim();
            }
        }

        // Try Twitter Card description
        Element twitterDesc = doc.selectFirst("meta[name=twitter:description]");
        if (twitterDesc != null) {
            String desc = twitterDesc.attr("content");
            if (desc != null && !desc.isBlank()) {
                return desc.trim();
            }
        }

        // Try regular meta description
        Element metaDesc = doc.selectFirst("meta[name=description]");
        if (metaDesc != null) {
            String desc = metaDesc.attr("content");
            if (desc != null && !desc.isBlank()) {
                return desc.trim();
            }
        }

        return null;
    }

    /**
     * Extract images from:
     * 1. og:image (Open Graph)
     * 2. twitter:image (Twitter Card)
     * 3. <img> tags (with src attribute)
     */
    private List<String> extractImages(Document doc, String baseUrl) {
        List<String> images = new ArrayList<>();

        // Try Open Graph image
        Element ogImage = doc.selectFirst("meta[property=og:image]");
        if (ogImage != null) {
            String imgUrl = ogImage.attr("content");
            if (imgUrl != null && !imgUrl.isBlank()) {
                images.add(normalizeUrl(imgUrl, baseUrl));
            }
        }

        // Try Twitter Card image
        Element twitterImage = doc.selectFirst("meta[name=twitter:image]");
        if (twitterImage != null) {
            String imgUrl = twitterImage.attr("content");
            if (imgUrl != null && !imgUrl.isBlank()) {
                images.add(normalizeUrl(imgUrl, baseUrl));
            }
        }

        // Extract from img tags
        Elements imgTags = doc.select("img[src]");
        for (Element img : imgTags) {
            String imgUrl = img.attr("src");
            if (imgUrl != null && !imgUrl.isBlank()) {
                String normalizedUrl = normalizeUrl(imgUrl, baseUrl);
                // Avoid duplicates and too small images
                if (!images.contains(normalizedUrl) && !imgUrl.contains("favicon")) {
                    images.add(normalizedUrl);
                    // Limit to 5 images
                    if (images.size() >= 5) {
                        break;
                    }
                }
            }
        }

        return images.isEmpty() ? null : images;
    }

    /**
     * Extract price using regex patterns for common currency symbols
     * Matches patterns like: €99.99, $19.99, £10.50, etc.
     */
    private Double extractPrice(Document doc) {
        // Get all text from the document
        String text = doc.text();

        // Regular expressions for different currency formats
        Pattern[] patterns = {
                Pattern.compile("[€$£¥₹]\\s*([0-9]+[.,][0-9]{1,2})"),  // € 99.99 or €99,99
                Pattern.compile("([0-9]+[.,][0-9]{1,2})\\s*[€$£¥₹]"),  // 99.99 € or 99,99€
                Pattern.compile("\\b(\\d+[.,]\\d{2})\\b"),              // Generic price format
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String priceStr = matcher.group(1);
                // Replace comma with dot for parsing
                priceStr = priceStr.replace(",", ".");
                try {
                    return Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    log.debug("Failed to parse price: {}", priceStr);
                }
            }
        }

        return null;
    }

    /**
     * Normalize relative URLs to absolute URLs
     */
    private String normalizeUrl(String imageUrl, String baseUrl) {
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }

        if (imageUrl.startsWith("//")) {
            // Protocol-relative URL
            try {
                URL base = new URI(baseUrl).toURL();
                return base.getProtocol() + ":" + imageUrl;
            } catch (MalformedURLException | URISyntaxException e) {
                log.debug("Failed to normalize URL: {}", imageUrl);
                return imageUrl;
            }
        }

        if (imageUrl.startsWith("/")) {
            // Absolute path
            try {
                URL base = new URI(baseUrl).toURL();
                return base.getProtocol() + "://" + base.getHost() + imageUrl;
            } catch (MalformedURLException | URISyntaxException e) {
                log.debug("Failed to normalize URL: {}", imageUrl);
                return imageUrl;
            }
        }

        // Relative path
        try {
            URL base = new URI(baseUrl).toURL();
            String basePath = base.getPath();
            if (!basePath.endsWith("/")) {
                basePath = basePath.substring(0, basePath.lastIndexOf("/") + 1);
            }
            return base.getProtocol() + "://" + base.getHost() + basePath + imageUrl;
        } catch (MalformedURLException | URISyntaxException e) {
            log.debug("Failed to normalize URL: {}", imageUrl);
            return imageUrl;
        }
    }

    /**
     * Extract domain from URL for default product name
     */
    private String extractDomain(String url) {
        try {
            URL parsed = new URI(url).toURL();
            String host = parsed.getHost();
            if (host != null) {
                // Remove "www." prefix if present
                if (host.startsWith("www.")) {
                    host = host.substring(4);
                }
                return host;
            }
        } catch (MalformedURLException | URISyntaxException e) {
            log.debug("Failed to extract domain: {}", url);
        }
        return "Product";
    }
}
