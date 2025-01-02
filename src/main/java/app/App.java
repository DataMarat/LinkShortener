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

        // Удаляем устаревшие ссылки при запуске
        System.out.println("Проверка устаревших ссылок...");
        removeExpiredLinks();

        while (true) {
            // Отображаем меню в зависимости от состояния пользователя
            if (currentUser == null) {
                System.out.println("Введите имя пользователя или наберите exit для выхода:");
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("exit")) {
                    System.out.println("Выход из программы...");
                    break;
                }
                handleUserLogin(input); // Авторизация или создание пользователя
            } else {
                displayMenu();
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("8") || input.equals("exit")) {
                    System.out.println("Выход из программы...");
                    break;
                }
                processCommand(input);
            }

            // Удаляем устаревшие ссылки после выполнения каждой команды
            removeExpiredLinks();
        }
    }

    private void displayMenu() {
        System.out.println("""
            1. Ввести имя пользователя
            2. Создать короткую ссылку
            3. Получить длинную ссылку
            4. Просмотреть ссылки
            5. Редактировать лимит переходов
            6. Удалить ссылку
            7. Сменить пользователя
            8. Выход (или `exit`)
            """);
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
            case 7 -> handleChangeUser();
            case 8 -> System.out.println("Выход из программы.");
            default -> System.out.println("Неверная команда");
        }
    }


    private void handleChangeUser() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите имя нового пользователя или UUID существующего, на которого хотите переключиться:");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("Ошибка: Ввод не может быть пустым.");
            return;
        }

        // Попробуем найти пользователя по UUID
        currentUser = users.stream()
                .filter(user -> user.getUuid().equals(input))
                .findFirst()
                .orElseGet(() -> {
                    // Если UUID не найден, предполагаем, что это имя нового пользователя
                    String uuid = java.util.UUID.randomUUID().toString();
                    User newUser = new User(input, uuid);
                    users.add(newUser);
                    System.out.printf("Создан новый пользователь: %s с UUID %s (используйте UUID как логин)%n", input, uuid);
                    return newUser;
                });

        System.out.printf("Вы переключились на пользователя: %s%n", currentUser.getUuid());
    }

    private void handleDeleteLink() {
        if (currentUser == null) {
            System.out.println("Ошибка: сначала введите имя пользователя (команда 1).");
            return;
        }

        List<Link> links = currentUser.getLinks();
        if (links.isEmpty()) {
            System.out.println("У вас пока нет ссылок для удаления.");
            return;
        }

        System.out.println("Ваши ссылки:");
        for (int i = 0; i < links.size(); i++) {
            System.out.printf("%d. Короткая ссылка: %s (Оригинальный URL: %s)%n", i + 1, links.get(i).getShortUrl(), links.get(i).getOriginalUrl());
        }

        System.out.println("Введите номер ссылки, которую хотите удалить (или 0 для отмены):");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();

        try {
            int index = Integer.parseInt(input) - 1;
            if (index == -1) {
                System.out.println("Удаление отменено.");
                return;
            }

            if (index < 0 || index >= links.size()) {
                System.out.println("Ошибка: Введён некорректный номер.");
                return;
            }

            Link removedLink = links.remove(index); // Удаляем ссылку
            System.out.println("Ссылка удалена: " + removedLink.getShortUrl());
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Введите числовое значение.");
        }
    }


    private void handleEditClickLimit() {
        if (currentUser == null) {
            System.out.println("Ошибка: сначала введите имя пользователя (команда 1).");
            return;
        }

        List<Link> links = currentUser.getLinks();
        if (links.isEmpty()) {
            System.out.println("У вас пока нет ссылок для редактирования.");
            return;
        }

        System.out.println("Ваши ссылки:");
        for (int i = 0; i < links.size(); i++) {
            System.out.printf("%d. Короткая ссылка: %s (Оригинальный URL: %s, Переходы: %d/%d)%n",
                    i + 1, links.get(i).getShortUrl(), links.get(i).getOriginalUrl(),
                    links.get(i).getClicks(), links.get(i).getClickLimit());
        }

        System.out.println("Введите номер ссылки, для которой хотите изменить лимит переходов (или 0 для отмены):");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();

        try {
            int index = Integer.parseInt(input) - 1;
            if (index == -1) {
                System.out.println("Редактирование отменено.");
                return;
            }

            if (index < 0 || index >= links.size()) {
                System.out.println("Ошибка: Введён некорректный номер.");
                return;
            }

            Link link = links.get(index);
            System.out.printf("Текущий лимит переходов для ссылки %s: %d%n", link.getShortUrl(), link.getClickLimit());
            System.out.println("Введите новый лимит переходов:");

            String newLimitInput = scanner.nextLine().trim();
            int newLimit = Integer.parseInt(newLimitInput);

            if (newLimit < link.getClicks()) {
                System.out.println("Ошибка: Новый лимит не может быть меньше текущего количества переходов.");
                return;
            }

            link.setClickLimit(newLimit);
            System.out.printf("Лимит переходов для ссылки %s успешно обновлён на %d.%n", link.getShortUrl(), newLimit);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Введите числовое значение.");
        }
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
                    ? formatDuration(java.time.Duration.between(java.time.LocalDateTime.now(), link.getExpirationTime()))
                    : "Истекла";

            System.out.printf("%d. Оригинальный URL: %s%n", i + 1, link.getOriginalUrl());
            System.out.printf("   Короткая ссылка: %s%n", link.getShortUrl());
            System.out.printf("   Переходы: %d/%d%n", link.getClicks(), link.getClickLimit());
            System.out.printf("   Статус: %s%n", status);
            System.out.printf("   Оставшееся время: %s%n", timeRemaining);
            System.out.println();
        }
    }

    private String formatDuration(java.time.Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%02d ч. %02d мин.", hours, minutes);
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
            System.out.println("Ошибка: Имя пользователя не может быть пустым.");
            return;
        }

        // Проверяем, существует ли пользователь с таким именем
        currentUser = users.stream()
                .filter(user -> user.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    // Создаем нового пользователя
                    String uuid = java.util.UUID.randomUUID().toString();
                    User newUser = new User(name, uuid);
                    users.add(newUser);
                    System.out.printf("Создан новый пользователь: %s с UUID %s (используйте UUID как логин)%n", name, uuid);
                    return newUser;
                });

        System.out.printf("Вы вошли как пользователь: %s%n", currentUser.getUuid());
    }

    private void handleCreateShortLink() {
        if (currentUser == null) {
            System.out.println("Ошибка: сначала введите имя пользователя (команда 1).");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        // Ввод оригинального URL
        System.out.println("Введите длинный URL:");
        String originalUrl = scanner.nextLine().trim();
        if (originalUrl.isEmpty()) {
            System.out.println("Ошибка: URL не может быть пустым.");
            return;
        }

        // Ввод лимита переходов
        System.out.printf("Введите лимит переходов (или нажмите Enter для значения по умолчанию: %d):%n", config.getDefaultClickLimit());
        String clickLimitInput = scanner.nextLine().trim();
        int clickLimit;
        try {
            clickLimit = clickLimitInput.isEmpty() ? config.getDefaultClickLimit() : Integer.parseInt(clickLimitInput);
            if (clickLimit <= 0) {
                System.out.println("Ошибка: Лимит переходов должен быть положительным числом.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Введите корректное числовое значение для лимита переходов.");
            return;
        }

        // Ввод времени жизни ссылки
        System.out.printf("Введите время жизни в часах (или нажмите Enter для значения по умолчанию: %d):%n", config.getDefaultExpirationTime().toHours());
        String expirationTimeInput = scanner.nextLine().trim();
        long expirationHours;
        try {
            expirationHours = expirationTimeInput.isEmpty() ? config.getDefaultExpirationTime().toHours() : Long.parseLong(expirationTimeInput);
            if (expirationHours <= 0) {
                System.out.println("Ошибка: Время жизни должно быть положительным числом.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Введите корректное числовое значение для времени жизни.");
            return;
        }

        // Генерация короткой ссылки
        String shortUrl = LinkGenerator.generateShortUrl(
                originalUrl,
                currentUser.getUuid(),
                clickLimit,
                System.currentTimeMillis()
        );

        // Создание объекта ссылки
        Link link = new Link(
                originalUrl,
                shortUrl,
                clickLimit,
                java.time.LocalDateTime.now().plusHours(expirationHours)
        );

        // Добавление ссылки пользователю
        currentUser.addLink(link);
        System.out.printf("Короткая ссылка создана: %s%n", shortUrl);
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

    private void removeExpiredLinks() {
        for (User user : users) {
            List<Link> links = user.getLinks();
            int initialSize = links.size();
            links.removeIf(link -> !link.isActive()); // Удаляем все неактивные ссылки
            int removedCount = initialSize - links.size();
            if (removedCount > 0) {
                System.out.printf("Пользователь %s: удалено устаревших ссылок: %d%n", user.getName(), removedCount);
            }
        }
    }

}
