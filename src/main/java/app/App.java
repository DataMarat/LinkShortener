package app;

import config.Config;
import models.User;
import models.Link;
import utils.LinkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class App {
    private final List<User> users;
    private User currentUser;
    private final Config config;

    public App(Config config) {
        this.users = new ArrayList<>();
        this.config = config;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean isFirstLogin = true; // Флаг для проверки первого входа

        while (true) {
            if (isFirstLogin && currentUser == null) {
                // Сообщение при первом входе
                System.out.println("Введите имя пользователя или наберите exit для выхода:");
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("exit")) {
                    System.out.println("Выход из программы...");
                    break;
                }
                handleUserLogin(input); // Обрабатываем ввод имени
                isFirstLogin = false; // После первой попытки устанавливаем флаг в false
            } else {
                // Обычное меню
                displayMenu();
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("8") || input.equals("exit")) {
                    System.out.println("Выход из программы...");
                    break;
                }
                processCommand(input);
            }
        }
    }



    private void displayMenu() {
        if (currentUser == null) {
            System.out.println("""
                1. Ввести имя пользователя
                2. Выход
                """);
        } else {
            System.out.println("""
                1. Ввести имя пользователя
                2. Создать короткую ссылку
                3. Получить длинную ссылку
                4. Просмотреть ссылки
                5. Редактировать лимит переходов
                6. Удалить ссылку
                7. Сменить имя
                8. Выход (или `exit`)
                """);
        }
    }

    private void processCommand(String input) {
        if (!isValidCommand(input)) {
            System.out.println("Ошибка: Введите корректный номер команды.");
            return;
        }

        int command = Integer.parseInt(input); // К этому моменту ввод гарантированно валиден

        switch (command) {
            case 1 -> handleUserLogin(input);
            case 2 -> handleCreateShortLink();
            case 3 -> handleGetOriginalLink();
            case 4 -> handleViewLinks();
            case 5 -> handleEditClickLimit();
            case 6 -> handleDeleteLink();
            case 7 -> handleChangeUsername();
            case 8 -> System.out.println("Выход из программы.");
            default -> System.out.println("Неверная команда");
        }
    }


    private void handleChangeUsername() {
    }

    private void handleDeleteLink() {
        
    }

    private void handleEditClickLimit() {
        
    }

    private void handleViewLinks() {
        if (currentUser == null) {
            System.out.println("Ошибка: сначала введите имя пользователя (команда 1).");
            return;
        }

        List<Link> links = currentUser.getLinks();
        if (links.isEmpty()) {
            System.out.println("У вас пока нет ссылок.");
            return;
        }

        System.out.println("Ваши ссылки:");
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            boolean isActive = link.isActive();
            String status = isActive ? "Активна" : "Неактивна";
            String timeRemaining = isActive
                    ? java.time.Duration.between(java.time.LocalDateTime.now(), link.getExpirationTime()).toHours() + " ч."
                    : "Истекла";

            System.out.printf("%d. Оригинальный URL: %s%n", i + 1, link.getOriginalUrl());
            System.out.printf("   Короткая ссылка: %s%n", link.getShortUrl());
            System.out.printf("   Переходы: %d/%d%n", link.getClicks(), link.getClickLimit());
            System.out.printf("   Статус: %s%n", status);
            System.out.printf("   Оставшееся время: %s%n", timeRemaining);
            System.out.println();
        }
    }


    private void handleGetOriginalLink() {
        if (currentUser == null) {
            System.out.println("Ошибка: сначала введите имя пользователя (команда 1).");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите короткую ссылку:");
        String shortUrl = scanner.nextLine().trim();

        // Поиск ссылки среди ссылок текущего пользователя
        Link link = currentUser.getLinks().stream()
                .filter(l -> l.getShortUrl().equals(shortUrl))
                .findFirst()
                .orElse(null);

        if (link == null) {
            System.out.println("Ошибка: ссылка не найдена.");
            return;
        }

        if (!link.isActive()) {
            System.out.println("Ошибка: ссылка недоступна. Она либо истекла, либо исчерпан лимит переходов.");
            return;
        }

        // Увеличиваем счётчик переходов и выводим оригинальный URL
        link.incrementClicks();
        System.out.println("Оригинальный URL: " + link.getOriginalUrl());
    }


    private void handleUserLogin(String name) {
        if (name.isEmpty()) {
            System.out.println("Имя пользователя не может быть пустым.");
            return;
        }

        currentUser = users.stream()
                .filter(user -> user.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    User newUser = new User(name, java.util.UUID.randomUUID().toString());
                    users.add(newUser);
                    System.out.println("Создан новый пользователь: " + name);
                    return newUser;
                });

        System.out.println("Вы вошли как пользователь: " + currentUser.getName());
    }
    private void handleCreateShortLink() {
        if (currentUser == null) {
            System.out.println("Ошибка: сначала введите имя пользователя (команда 1).");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите длинный URL:");
        String originalUrl = scanner.nextLine().trim();
        if (originalUrl.isEmpty()) {
            System.out.println("URL не может быть пустым.");
            return;
        }

        System.out.printf("Введите лимит переходов (или нажмите Enter для значения по умолчанию: %d):%n", config.getDefaultClickLimit());
        String clickLimitInput = scanner.nextLine().trim();
        int clickLimit = clickLimitInput.isEmpty() ? config.getDefaultClickLimit() : Integer.parseInt(clickLimitInput);

        System.out.printf("Введите время жизни в часах (или нажмите Enter для значения по умолчанию: %d):%n", config.getDefaultExpirationTime().toHours());
        String expirationTimeInput = scanner.nextLine().trim();
        long expirationHours = expirationTimeInput.isEmpty() ? config.getDefaultExpirationTime().toHours() : Long.parseLong(expirationTimeInput);

        String shortUrl = LinkGenerator.generateShortUrl();
        Link link = new Link(
                originalUrl,
                shortUrl,
                clickLimit,
                java.time.LocalDateTime.now().plusHours(expirationHours)
        );
        currentUser.addLink(link);

        System.out.println("Короткая ссылка создана: " + shortUrl);
    }

    private boolean isValidCommand(String input) {
        try {
            int command = Integer.parseInt(input); // Проверяем, является ли ввод числом
            if (currentUser == null) {
                return command == 1 || command == 8; // Допустимы только команды "1" и "8" до входа
            }
            return command >= 1 && command <= 8; // Полный диапазон команд для авторизованного пользователя
        } catch (NumberFormatException e) {
            return false; // Если ввод не число
        }
    }
}
