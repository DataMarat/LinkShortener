package app;

import config.Config;

public class Main {
    public static void main(String[] args) {
        Config config = new Config();
        try {
            // Загружаем конфигурацию
            System.out.println("Загрузка конфигурации...");
            config.loadFromFile("src/main/resources/config.txt");
            System.out.println("Конфигурация успешно загружена.");

            // Запускаем приложение
            App app = new App(config);
            app.run();
        } catch (SecurityException e) {
            System.err.println("Ошибка доступа к файлу конфигурации: недостаточно прав.");
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка в содержимом файла конфигурации: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Произошла ошибка: некорректный ввод или неизвестная ошибка.");
            e.printStackTrace(); // Для отладки
        } finally {
            System.out.println("Программа завершена.");
        }
    }
}

