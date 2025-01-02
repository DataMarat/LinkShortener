package models;
import utils.TimeUtils;
import java.time.LocalDateTime;

public class Link {
    private final String originalUrl; // Оригинальный URL
    private final String shortUrl;   // Короткая ссылка
    private int clickLimit;    // Лимит переходов
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
        return clicks < clickLimit && !TimeUtils.isExpired(expirationTime);
    }

    // Увеличение количества переходов
    public void incrementClicks() {
        if (isActive()) {
            clicks++;
        } else {
            throw new IllegalStateException("Ссылка больше не активна.");
        }
    }

    public void setClickLimit(int newLimit) {
        if (newLimit < clicks) {
            throw new IllegalArgumentException("Новый лимит не может быть меньше текущего количества переходов.");
        }
        this.clickLimit = newLimit;
    }
}
