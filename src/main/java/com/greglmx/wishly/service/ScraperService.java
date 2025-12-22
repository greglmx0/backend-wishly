package com.greglmx.wishly.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ScraperService {

    private static final int TIMEOUT_MS = 10000; // 10 seconds timeout
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final String ACCEPT_HEADER = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    private static final String ACCEPT_LANGUAGE = "en-US,en;q=0.9,fr;q=0.8";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Scrape product information from a given URL
     * Strategy:
     * 1. Extract meta tags (Open Graph, Twitter Card)
     * 2. Extract title and description
     * 3. Extract images
     * 4. Detect price with regex patterns
     */
    public GiftResponse scrapeUrl(String url) {
        log.info("[scrapeUrl] Scraping URL: {}", url);
        try {
            // Validate URL format
            new URI(url).toURL();

            // Fetch and parse HTML document
                Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .header("Accept", ACCEPT_HEADER)
                    .header("Accept-Language", ACCEPT_LANGUAGE)
                    .referrer("https://www.google.com/")
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .get();

            GiftResponse response = new GiftResponse();
            response.setUrl(url);

            StructuredDataResult structuredData = extractStructuredData(doc, url);

            // Extract title from structured data/meta or regular title
            String title = structuredData.name != null ? structuredData.name : extractTitle(doc);
            response.setName(title);

            // Extract description from meta tags or structured data
            String description = structuredData.description != null ? structuredData.description : extractDescription(doc);
            response.setDescription(description);

            // Extract images merging structured data and document images
            List<String> images = mergeImages(structuredData.images, extractImages(doc, url));
            response.setImages(images);

            // Extract price from structured data, meta price tags, then regex
            Double price = structuredData.price != null ? structuredData.price : extractPrice(doc);
            response.setPrice(price);

            return response;

        } catch (MalformedURLException e) {
            log.warn("Malformed URL: {}", url, e);
            throw new IllegalArgumentException("Invalid URL format: " + url);
        } catch (URISyntaxException e) {
            log.warn("Invalid URL syntax: {}", url, e);
            throw new IllegalArgumentException("Invalid URL format: " + url);
        } catch (IOException e) {
            log.error("Error scraping URL: {}", url, e);
            throw new RuntimeException("Unable to scrape the page: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error scraping URL: {}", url, e);
            throw new RuntimeException("Unexpected error while scraping: " + e.getMessage());
        }
    }

    private StructuredDataResult extractStructuredData(Document doc, String baseUrl) {
        StructuredDataResult result = new StructuredDataResult();

        // JSON-LD Product blocks (schema.org)
        Elements ldJson = doc.select("script[type=application/ld+json]");
        for (Element script : ldJson) {
            String json = script.data();
            if (json == null || json.isBlank()) {
                continue;
            }
            try {
                JsonNode node = MAPPER.readTree(json);
                if (node.isArray()) {
                    for (JsonNode item : node) {
                        if (parseProductNode(item, result, baseUrl)) {
                            break;
                        }
                    }
                } else {
                    parseProductNode(node, result, baseUrl);
                }
            } catch (Exception e) {
                log.debug("Failed to parse ld+json block", e);
            }
        }

        // Meta price fallbacks
        String priceCandidate = firstNonBlank(
                metaContent(doc, "meta[property=product:price:amount]"),
                metaContent(doc, "meta[property=og:price:amount]"),
                metaContent(doc, "meta[name=price]"),
                metaContent(doc, "meta[itemprop=price]")
        );
        if (priceCandidate != null && result.price == null) {
            result.price = parsePriceNumber(priceCandidate);
        }

        // Meta name/description fallbacks if JSON-LD missed
        if (result.name == null) {
            result.name = firstNonBlank(
                    metaContent(doc, "meta[itemprop=name]"),
                    metaContent(doc, "meta[name=title]")
            );
        }
        if (result.description == null) {
            result.description = metaContent(doc, "meta[itemprop=description]");
        }

        // Meta image fallbacks
        addImageIfPresent(result.images, metaContent(doc, "meta[property=og:image:secure_url]"), baseUrl);
        addImageIfPresent(result.images, metaContent(doc, "meta[name=twitter:image:src]"), baseUrl);
        addImageIfPresent(result.images, metaContent(doc, "meta[itemprop=image]"), baseUrl);
        addImageIfPresent(result.images, linkHref(doc, "link[rel=image_src]"), baseUrl);

        return result;
    }

    private boolean parseProductNode(JsonNode node, StructuredDataResult result, String baseUrl) {
        if (node == null) {
            return false;
        }

        String type = null;
        if (node.has("@type")) {
            JsonNode typeNode = node.get("@type");
            if (typeNode.isArray() && typeNode.size() > 0) {
                type = typeNode.get(0).asText();
            } else if (typeNode.isTextual()) {
                type = typeNode.asText();
            }
        }

        if (type == null || !type.toLowerCase().contains("product")) {
            return false;
        }

        if (result.name == null && node.has("name")) {
            result.name = node.get("name").asText(null);
        }
        if (result.description == null && node.has("description")) {
            result.description = node.get("description").asText(null);
        }

        if (node.has("image")) {
            JsonNode imageNode = node.get("image");
            if (imageNode.isTextual()) {
                addImageIfPresent(result.images, imageNode.asText(), baseUrl);
            } else if (imageNode.isArray()) {
                for (JsonNode img : imageNode) {
                    if (img.isTextual()) {
                        addImageIfPresent(result.images, img.asText(), baseUrl);
                    }
                }
            }
        }

        if (result.price == null && node.has("offers")) {
            JsonNode offers = node.get("offers");
            if (offers.isArray()) {
                for (JsonNode offer : offers) {
                    Double parsed = extractPriceFromOffer(offer);
                    if (parsed != null) {
                        result.price = parsed;
                        break;
                    }
                }
            } else {
                Double parsed = extractPriceFromOffer(offers);
                if (parsed != null) {
                    result.price = parsed;
                }
            }
        }

        return true;
    }

    private Double extractPriceFromOffer(JsonNode offer) {
        if (offer == null) {
            return null;
        }
        if (offer.has("price")) {
            String priceStr = offer.get("price").asText(null);
            Double parsed = parsePriceNumber(priceStr);
            if (parsed != null) {
                return parsed;
            }
        }
        if (offer.has("priceSpecification")) {
            JsonNode spec = offer.get("priceSpecification");
            if (spec.has("price")) {
                return parsePriceNumber(spec.get("price").asText(null));
            }
        }
        return null;
    }

    private List<String> mergeImages(List<String> structuredImages, List<String> extractedImages) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (structuredImages != null) {
            merged.addAll(structuredImages);
        }
        if (extractedImages != null) {
            merged.addAll(extractedImages);
        }
        return merged.isEmpty() ? null : new ArrayList<>(merged);
    }

    private void addImageIfPresent(List<String> images, String candidate, String baseUrl) {
        if (candidate == null || candidate.isBlank()) {
            return;
        }
        images.add(normalizeUrl(candidate, baseUrl));
    }

    private String metaContent(Document doc, String cssQuery) {
        Element el = doc.selectFirst(cssQuery);
        if (el == null) {
            return null;
        }
        String value = el.hasAttr("content") ? el.attr("content") : el.attr("value");
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String linkHref(Document doc, String cssQuery) {
        Element el = doc.selectFirst(cssQuery);
        if (el == null) {
            return null;
        }
        String value = el.attr("href");
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }

    private Double parsePriceNumber(String priceStr) {
        if (priceStr == null) {
            return null;
        }
        // Keep only digits, separators, and signs for heuristic parsing
        String cleaned = priceStr
                .replaceAll("[A-Za-z]{3}", " ") // remove currency codes
                .replaceAll("[\\u00A3\\u20AC$¥₹]", " ") // remove symbols
                .replaceAll("[^0-9.,]", "")
                .trim();

        if (cleaned.isEmpty()) {
            return null;
        }

        int lastComma = cleaned.lastIndexOf(',');
        int lastDot = cleaned.lastIndexOf('.');

        // Determine decimal separator heuristic
        char decimalSep;
        if (lastComma == -1 && lastDot == -1) {
            decimalSep = '.'; // integer value
        } else if (lastComma == -1) {
            decimalSep = '.';
        } else if (lastDot == -1) {
            decimalSep = ',';
        } else {
            decimalSep = lastComma > lastDot ? ',' : '.';
        }

        // Remove thousands separators (the opposite of decimalSep) and normalize decimal
        if (decimalSep == ',') {
            cleaned = cleaned.replace(".", "");
            cleaned = cleaned.replace(',', '.');
        } else {
            cleaned = cleaned.replace(",", "");
        }

        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            // Fallback: simple regex to grab first numeric fragment
            Matcher matcher = Pattern.compile("(\\d+(?:\\.\\d{1,2})?)").matcher(cleaned);
            if (matcher.find()) {
                try {
                    return Double.parseDouble(matcher.group(1));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
            return null;
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
        Set<String> images = new LinkedHashSet<>();

        // Try Open Graph image
        Element ogImage = doc.selectFirst("meta[property=og:image]");
        if (ogImage != null) {
            String imgUrl = ogImage.attr("content");
            if (imgUrl != null && !imgUrl.isBlank()) {
                images.add(normalizeUrl(imgUrl, baseUrl));
            }
        }

        // Secure OG image
        Element ogImageSecure = doc.selectFirst("meta[property=og:image:secure_url]");
        if (ogImageSecure != null) {
            String imgUrl = ogImageSecure.attr("content");
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
        Element twitterImageSrc = doc.selectFirst("meta[name=twitter:image:src]");
        if (twitterImageSrc != null) {
            String imgUrl = twitterImageSrc.attr("content");
            if (imgUrl != null && !imgUrl.isBlank()) {
                images.add(normalizeUrl(imgUrl, baseUrl));
            }
        }

        // Itemprop/link images
        String itempropImage = metaContent(doc, "meta[itemprop=image]");
        if (itempropImage != null) {
            images.add(normalizeUrl(itempropImage, baseUrl));
        }
        String linkImage = linkHref(doc, "link[rel=image_src]");
        if (linkImage != null) {
            images.add(normalizeUrl(linkImage, baseUrl));
        }

        // Extract from img tags
        Elements imgTags = doc.select("img[src]");
        for (Element img : imgTags) {
            String imgUrl = firstNonBlank(img.attr("src"), img.attr("data-src"), img.attr("data-original"), img.attr("data-zoom-image"));
            if (imgUrl != null && !imgUrl.isBlank()) {
                String normalizedUrl = normalizeUrl(imgUrl, baseUrl);
                // Avoid duplicates and too small images
                if (!normalizedUrl.toLowerCase().contains("favicon")) {
                    images.add(normalizedUrl);
                }
            }

            // srcset support (take the first candidate)
            String srcset = img.attr("srcset");
            if (srcset != null && !srcset.isBlank()) {
                String[] candidates = srcset.split(",");
                if (candidates.length > 0) {
                    String first = candidates[0].trim().split(" ")[0];
                    if (first != null && !first.isBlank()) {
                        images.add(normalizeUrl(first, baseUrl));
                    }
                }
            }
        }

        // picture source tags
        Elements sources = doc.select("picture source[srcset]");
        for (Element source : sources) {
            String srcset = source.attr("srcset");
            if (srcset != null && !srcset.isBlank()) {
                String[] candidates = srcset.split(",");
                if (candidates.length > 0) {
                    String first = candidates[0].trim().split(" ")[0];
                    if (first != null && !first.isBlank()) {
                        images.add(normalizeUrl(first, baseUrl));
                    }
                }
            }
        }

        // Limit to 8 images to avoid bloat
        List<String> limited = new ArrayList<>();
        for (String img : images) {
            limited.add(img);
            if (limited.size() >= 8) {
                break;
            }
        }

        return limited.isEmpty() ? null : limited;
    }

    /**
     * Extract price using regex patterns for common currency symbols
     * Matches patterns like: €99.99, $19.99, £10.50, etc.
     */
    private Double extractPrice(Document doc) {
        // Check meta price declarations first
        String metaPrice = firstNonBlank(
                metaContent(doc, "meta[property=product:price:amount]"),
                metaContent(doc, "meta[property=og:price:amount]"),
                metaContent(doc, "meta[name=price]"),
                metaContent(doc, "meta[itemprop=price]")
        );
        Double parsedMeta = parsePriceNumber(metaPrice);
        if (parsedMeta != null) {
            return parsedMeta;
        }

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

    private static class StructuredDataResult {
        String name;
        String description;
        Double price;
        List<String> images = new ArrayList<>();
    }
}
