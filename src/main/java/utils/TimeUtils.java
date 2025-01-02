package utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtils {
    // Форматирует оставшееся время в виде "HH ч. MM мин."
    public static String formatRemainingTime(LocalDateTime expirationTime) {
        Duration duration = Duration.between(LocalDateTime.now(), expirationTime);
        if (duration.isNegative() || duration.isZero()) {
            return "Истекла";
        }

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%02d ч. %02d мин.", hours, minutes);
    }

    // Проверяет, истёк ли срок действия
    public static boolean isExpired(LocalDateTime expirationTime) {
        return LocalDateTime.now().isAfter(expirationTime);
    }
}
