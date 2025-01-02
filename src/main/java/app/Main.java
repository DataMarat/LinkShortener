package app;

import config.Config;

public class Main {
    public static void main(String[] args) {
        Config config = new Config();
        try {
            config.loadFromFile("resources/config.txt");
            App app = new App(config);
            app.run();
        } catch (Exception e) {
            System.out.println("Ошибка загрузки конфигурации: " + e.getMessage());
        }
    }
}
