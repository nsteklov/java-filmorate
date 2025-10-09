package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@Qualifier("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    String error;

    @Override
    public Collection<User> findAll() {
        log.debug("Получен список пользователей");
        return users.values();
    }

    @Override
    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            error = "Электронная почта не может быть пустой и должна содержать символ @";
            log.error(error);
            throw new ValidationException(error);
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            error = "Логин не может быть пустым и содержать пробелы";
            log.error(error);
            throw new ValidationException(error);
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            error = "Дата рождения не может быть будущей датой";
            log.error(error);
            throw new ValidationException(error);
        }
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Добавлен пользователь: {}", user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            error = "Id должен быть указан";
            log.error(error);
            throw new NotFoundException(error);
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getEmail() == null || newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
                error = "Электронная почта не может быть пустой и должна содержать символ @";
                log.error(error);
                throw new ValidationException(error);
            }
            if (newUser.getLogin() == null || newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
                error = "Логин не может быть пустым и содержать пробелы";
                log.error(error);
                throw new ValidationException(error);
            }
            if (newUser.getBirthday().isAfter(LocalDate.now())) {
                error = "Дата рождения не может быть будущей датой";
                log.error(error);
                throw new ValidationException(error);
            }
            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setBirthday(newUser.getBirthday());
            if (newUser.getName() == null || newUser.getName().isBlank()) {
                oldUser.setName(newUser.getLogin());
            } else {
                oldUser.setName(newUser.getName());
            }
            users.put(oldUser.getId(), oldUser);
            log.info("Обновлены данные пользователя: {}", oldUser);
            return oldUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public Optional<User> findUserById(Long idOfUser) {
        User user = users.keySet()
                .stream()
                .map(id -> Optional.ofNullable(users.get(idOfUser)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + idOfUser + " не найден"));
        log.debug("Найден пользователь по id: {}", idOfUser);
        return Optional.ofNullable(user);
    }

    public void addFriend(Long id, Long friendId) {
    }

    public boolean deleteFriend(Long id, Long friendId) {
        return true;
    }

    public Collection getUserFriends(Long id) {
        return null;
    }

    public Collection getCommonFriends(Long id, Long otherId) {
        return null;
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        log.debug("Получен следующий по порядку id пользователя: {}", ++currentMaxId);
        return currentMaxId;
    }
}
