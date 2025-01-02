package app;

import config.Config;
import models.User;

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
        while (true) {
            displayMenu();
            int choice = scanner.nextInt();
            if (choice == 8) break;
            processCommand(choice);
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
                7. Сменить имя
                8. Выход
                """);
    }

    private void processCommand(int command) {
        switch (command) {
            case 1 -> System.out.println("Ввод имени пользователя");
            case 2 -> System.out.println("Создание короткой ссылки");
            case 3 -> System.out.println("Получение длинной ссылки");
            case 4 -> System.out.println("Просмотр ссылок");
            case 5 -> System.out.println("Редактирование лимита переходов");
            case 6 -> System.out.println("Удаление ссылки");
            case 7 -> System.out.println("Смена имени");
            default -> System.out.println("Неверная команда");
        }
    }
}
