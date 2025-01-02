package config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class Config {
    private int defaultClickLimit;
    private Duration defaultExpirationTime;

    public void loadFromFile(String filename) throws IOException {
        var lines = Files.readAllLines(Path.of(filename));
        for (String line : lines) {
            String[] parts = line.split("=");
            switch (parts[0].trim()) {
                case "defaultClickLimit" -> defaultClickLimit = Integer.parseInt(parts[1].trim());
                case "defaultExpirationTime" -> defaultExpirationTime = Duration.ofHours(Long.parseLong(parts[1].trim()));
            }
        }
    }

    public int getDefaultClickLimit() {
        return defaultClickLimit;
    }

    public Duration getDefaultExpirationTime() {
        return defaultExpirationTime;
    }
}
