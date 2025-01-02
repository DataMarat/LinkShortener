package config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private int defaultClickLimit;
    private Duration defaultExpirationTime;

    public void loadFromFile(String filename) {
        try {
            List<String> lines = Files.readAllLines(Path.of(filename));
            if (lines.isEmpty()) {
                throw new IllegalArgumentException("Файл конфигурации пуст.");
            }

            Map<String, String> configMap = parseConfig(lines);

            // Проверяем и загружаем параметры
            defaultClickLimit = parseInteger(configMap.get("defaultClickLimit"), "defaultClickLimit");
            defaultExpirationTime = Duration.ofHours(parseInteger(configMap.get("defaultExpirationTime"), "defaultExpirationTime"));

        } catch (IOException e) {
            System.err.println("Ошибка: Не удалось открыть файл конфигурации. Проверьте путь: " + filename);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка конфигурации: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Неизвестная ошибка при загрузке конфигурации: " + e.getMessage());
        }
    }

    private Map<String, String> parseConfig(List<String> lines) {
        Map<String, String> configMap = new HashMap<>();
        for (String line : lines) {
            String[] parts = line.split("=");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Некорректная строка конфигурации: " + line);
            }
            configMap.put(parts[0].trim(), parts[1].trim());
        }
        return configMap;
    }

    private int parseInteger(String value, String key) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректное значение для параметра " + key + ": " + value);
        }
    }

    public int getDefaultClickLimit() {
        return defaultClickLimit;
    }

    public Duration getDefaultExpirationTime() {
        return defaultExpirationTime;
    }
}
