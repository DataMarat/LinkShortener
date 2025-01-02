package app;

import config.Config;
import models.User;
import models.Link;
import models.Session;
import utils.LinkGenerator;
import utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class App {
    private final List<User> users;
    private User currentUser;
    private final Config config;
    private final Session session = new Session();
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
            if (session.getCurrentUser() == null) {
                System.out.println("Введите имя пользователя или наберите exit для выхода:");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Выход из программы...");
                    break;
                }
                handleUserLogin(input);
            } else {
                displayMenu();
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("8") || input.equalsIgnoreCase("exit")) {
                    System.out.println("Выход из программы...");
                    break;
                }
                processCommand(input); // Обрабатываем команду
            }

            removeExpiredLinks(); // Удаление устаревших ссылок
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
        try {
            // Проверяем, является ли ввод числом
            int command = Integer.parseInt(input);

            // Проверяем доступные команды для неавторизованного пользователя
            if (session.getCurrentUser() == null) {
                if (command != 1) {
                    System.out.println("Ошибка: сначала введите имя пользователя (команда 1).");
                    return;
                }
            } else {
                // Проверяем диапазон доступных команд для авторизованного пользователя
                if (command < 1 || command > 8) {
                    System.out.println("Ошибка: Введите корректный номер команды.");
                    return;
                }
            }

            // Выполняем команду
            switch (command) {
                case 1 -> {
                    if (session.getCurrentUser() == null) {
                        System.out.println("Введите имя пользователя:");
                        Scanner scanner = new Scanner(System.in);
                        String name = scanner.nextLine().trim();
                        handleUserLogin(name);
                    } else {
                        System.out.println("Вы уже вошли как пользователь: " + session.getCurrentUser().getUuid());
                    }
                }
                case 2 -> handleCreateShortLink();
                case 3 -> handleGetOriginalLink();
                case 4 -> handleViewLinks();
                case 5 -> handleEditClickLimit();
                case 6 -> handleDeleteLink();
                case 7 -> handleChangeUser();
                case 8 -> System.out.println("Выход из программы...");
                default -> System.out.println("Ошибка: Команда не распознана.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Введите корректный номер команды.");
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

        User user = session.findUserByUuid(input);
        if (user == null) {
            user = session.findOrCreateUser(input);
            System.out.printf("Создан новый пользователь: %s с UUID %s (используйте UUID как логин)%n", user.getName(), user.getUuid());
        }

        session.setCurrentUser(user);
        System.out.printf("Вы переключились на пользователя: %s%n", user.getUuid());
    }




    private void handleViewLinks() {
        User currentUser = session.getCurrentUser(); // Получаем текущего пользователя из сессии
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
            String timeRemaining = link.isActive()
                    ? TimeUtils.formatRemainingTime(link.getExpirationTime())
                    : "Истекла";

            System.out.printf("%d. Оригинальный URL: %s%n", i + 1, link.getOriginalUrl());
            System.out.printf("   Короткая ссылка: %s%n", link.getShortUrl());
            System.out.printf("   Переходы: %d/%d%n", link.getClicks(), link.getClickLimit());
            System.out.printf("   Статус: %s%n", status);
            System.out.printf("   Оставшееся время: %s%n", timeRemaining);
            System.out.println();
        }
    }

    private void handleEditClickLimit() {
        User currentUser = session.getCurrentUser();
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

    private void handleGetOriginalLink() {
        User currentUser = session.getCurrentUser();
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


    private void handleUserLogin(String input) {
        if (input.isEmpty()) {
            System.out.println("Ошибка: Имя пользователя или UUID не может быть пустым.");
            return;
        }

        // Проверка UUID
        User user = session.findUserByUuid(input);
        if (user == null) {
            // Если это не UUID, считаем, что это имя пользователя
            user = session.findOrCreateUser(input);
            if (session.findUserByName(input) == user) {
                System.out.printf("Создан новый пользователь: %s с UUID %s (используйте UUID как логин)%n", input, user.getUuid());
            }
        }

        session.setCurrentUser(user);
        System.out.printf("Вы вошли как пользователь: %s%n", user.getUuid());
    }


    private void handleCreateShortLink() {
        User currentUser = session.getCurrentUser();
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

    private void handleDeleteLink() {
        User currentUser = session.getCurrentUser();
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

//    private boolean isValidCommand(String input) {
//        try {
//            int command = Integer.parseInt(input); // Проверяем, является ли ввод числом
//            if (currentUser == null) {
//                return command == 1 || command == 8; // Допустимы только команды "1" и "8" до входа
//            }
//            return command >= 1 && command <= 8; // Полный диапазон команд для авторизованного пользователя
//        } catch (NumberFormatException e) {
//            return false; // Если ввод не число
//        }
//    }

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
