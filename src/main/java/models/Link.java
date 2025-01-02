package models;

import java.time.LocalDateTime;

public class Link {
    private final String originalUrl; // Оригинальный URL
    private final String shortUrl;   // Короткая ссылка
    private final int clickLimit;    // Лимит переходов
    private int clicks;              // Текущее количество переходов
    private final LocalDateTime expirationTime; // Время истечения ссылки

    // Конструктор
    public Link(String originalUrl, String shortUrl, int clickLimit, LocalDateTime expirationTime) {
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.clickLimit = clickLimit;
        this.expirationTime = expirationTime;
        this.clicks = 0; // Изначально переходов нет
    }

    // Геттеры
    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public int getClickLimit() {
        return clickLimit;
    }

    public int getClicks() {
        return clicks;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    // Проверка, активна ли ссылка
    public boolean isActive() {
        return clicks < clickLimit && LocalDateTime.now().isBefore(expirationTime);
    }

    // Увеличение количества переходов
    public void incrementClicks() {
        if (isActive()) {
            clicks++;
        } else {
            throw new IllegalStateException("Ссылка больше не активна.");
        }
    }
}
