package utils;

import java.util.Random;

public class LinkGenerator {
    public static String generateShortUrl() {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder shortUrl = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) { // Генерируем 6-символьный идентификатор
            shortUrl.append(characters.charAt(random.nextInt(characters.length())));
        }
        return "http://short.url/" + shortUrl;
    }
}
