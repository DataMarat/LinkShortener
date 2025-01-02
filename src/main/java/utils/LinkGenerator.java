package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class LinkGenerator {
    public static String generateShortUrl(String originalUrl, String userId, int clickLimit, long timestamp) {
        try {
            // Формируем строку для хэширования
            String input = originalUrl + userId + clickLimit + timestamp;

            // Создаем хэш MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());

            // Преобразуем в строку Base64 и обрезаем до 7 символов
            return "http://short.url/" + Base64.getUrlEncoder().withoutPadding().encodeToString(digest).substring(0, 7);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка: MD5 алгоритм недоступен.", e);
        }
    }
}

