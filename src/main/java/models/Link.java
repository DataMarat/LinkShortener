package models;

import java.time.LocalDateTime;

public class Link {
    private final String originalUrl;
    private final String shortUrl;
    private final int clickLimit;
    private int clicks;
    private final LocalDateTime expirationTime;

    public Link(String originalUrl, String shortUrl, int clickLimit, LocalDateTime expirationTime) {
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.clickLimit = clickLimit;
        this.expirationTime = expirationTime;
        this.clicks = 0;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public boolean isActive() {
        return clicks < clickLimit && LocalDateTime.now().isBefore(expirationTime);
    }

    public void incrementClicks() {
        clicks++;
    }
}
