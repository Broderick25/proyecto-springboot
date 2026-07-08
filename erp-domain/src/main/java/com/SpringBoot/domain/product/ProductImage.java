package com.SpringBoot.domain.product;

import java.net.URI;
import java.net.URISyntaxException;

public record ProductImage(String imageUrl) {

    public ProductImage {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("ProductImage imageUrl must not be null or blank");
        }
        try {
            URI uri = new URI(imageUrl);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("ProductImage imageUrl must be an absolute URL: " + imageUrl);
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("ProductImage imageUrl is not a valid URL: " + imageUrl, e);
        }
    }

    public static ProductImage of(String imageUrl) {
        return new ProductImage(imageUrl);
    }

    public String getFullUrl() {
        return imageUrl;
    }

    public String getFileName() {
        int lastSlash = imageUrl.lastIndexOf('/');
        return lastSlash >= 0 ? imageUrl.substring(lastSlash + 1) : imageUrl;
    }
}
