package models;

import java.util.HashMap;
import java.util.Map;

public class Session {
    private User currentUser; // Текущий активный пользователь
    private final Map<String, User> userMap; // Карта UUID -> Пользователь

    public Session() {
        this.userMap = new HashMap<>();
    }

    // Установка текущего пользователя
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // Получение текущего пользователя
    public User getCurrentUser() {
        return currentUser;
    }

    // Добавление пользователя
    public void addUser(User user) {
        userMap.put(user.getUuid(), user);
    }

    // Поиск пользователя по UUID
    public User findUserByUuid(String uuid) {
        return userMap.get(uuid);
    }

    // Поиск пользователя по имени
    public User findUserByName(String name) {
        return userMap.values().stream()
                .filter(user -> user.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    // Поиск или создание пользователя
    public User findOrCreateUser(String name) {
        User user = findUserByName(name);
        if (user == null) {
            String uuid = java.util.UUID.randomUUID().toString();
            user = new User(name, uuid);
            addUser(user);
        }
        return user;
    }
}
