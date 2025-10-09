package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
@Qualifier("UserDbStorage")
public class UserDbStorage implements UserStorage {
    private final NamedParameterJdbcOperations jdbc;
    private final UserRowMapper mapper;
    String error;

    @Override
    public List<User> findAll() {
        String query = "SELECT * FROM users";
        return jdbc.query(query, mapper);
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
        String query = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (:email, :login, :name, :birthday)""";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", user.getEmail());
        params.addValue("login", user.getLogin());
        params.addValue("name", user.getName());
        params.addValue("birthday", user.getBirthday());
        jdbc.update(query, params, keyHolder, new String[]{"id"});
        user.setId(keyHolder.getKeyAs(Long.class));
        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        Long id = user.getId();
        if (user.getId() == null) {
            error = "Id должен быть указан";
            log.error(error);
            throw new NotFoundException(error);
        }
        Optional<User> optUser = findUserById(id);
        if (optUser.isEmpty()) {
            error = "Пользователь с id = " + id + " не найден";
            log.error(error);
            throw new NotFoundException(error);
        }
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
        String query = "UPDATE users SET email = :email, login = :login, name = :name, birthday = :birthday WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", user.getEmail());
        params.addValue("login", user.getLogin());
        params.addValue("name", user.getName());
        params.addValue("birthday", user.getBirthday());
        params.addValue("id", user.getId());
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        log.info("Обновлены данные пользователя: {}", user);
        return user;
    }

    @Override
    public Optional<User> findUserById(Long idOfUser) {
        String query = "SELECT * FROM users WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", idOfUser);
        try {
            User user = jdbc.queryForObject(query, params, mapper);
            log.debug("Найден пользователь по id: {}", idOfUser);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public void addFriend(Long id, Long friendId) {
        Optional<User> optUser = findUserById(id);
        if (optUser.isEmpty()) {
            error = "Пользователь с id = " + id + " не найден";
            log.error(error);
            throw new NotFoundException(error);
        }
        Optional<User> optFriend = findUserById(friendId);
        if (optFriend.isEmpty()) {
            error = "Пользователь с id = " + friendId + " не найден";
            log.error(error);
            throw new NotFoundException(error);
        }
        if (id == friendId) {
            error = "Пользователя нельзя добавить в друзья к самому себе";
            log.error(error);
            throw new ValidationException(error);
        }
        String query = """
                INSERT INTO user_friends (user1_id, user2_id)
                VALUES (:user1_id, :user2_id)""";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("user1_id", id);
        params.addValue("user2_id", friendId);
        jdbc.update(query, params);
    }

    public boolean deleteFriend(Long id, Long friendId) {
        Optional<User> optUser = findUserById(id);
        if (optUser.isEmpty()) {
            error = "Пользователь с id = " + id + " не найден";
            log.error(error);
            throw new NotFoundException(error);
        }
        Optional<User> optFriend = findUserById(friendId);
        if (optFriend.isEmpty()) {
            error = "Пользователь с id = " + friendId + " не найден";
            log.error(error);
            throw new NotFoundException(error);
        }
        if (id == friendId) {
            error = "Пользователя не может быть в друзьях у самого себя";
            log.error(error);
            throw new ValidationException(error);
        }

        String query = "DELETE FROM user_friends WHERE user1_id = :id1 AND user2_id = :id2";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id1", id);
        params.addValue("id2", friendId);
        int rowsDeleted = jdbc.update(query, params);
        if (rowsDeleted > 0) {
            log.warn("Пользователь с id = " + friendId + " не найден");
        } else {
            log.info("Пользователи с id {} и {} больше не друзья", id, friendId);
        }
        return rowsDeleted > 0;
    }

    public Collection<User> getUserFriends(Long id) {
        Optional<User> optUser = findUserById(id);
        if (optUser.isEmpty()) {
            error = "Пользователь с id = " + id + " не найден";
            log.error(error);
            throw new NotFoundException(error);
        }
        String query = """
                SELECT *
                FROM users as u
                JOIN user_friends as uf ON u.id = uf.user2_id
                WHERE uf.user1_id = :id""";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        log.debug("Получен список друзей пользователя с id {}", id);
        return jdbc.query(query, params, mapper);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        Optional<User> optUser = findUserById(id);
        if (optUser.isEmpty()) {
            error = "Пользователь с id = " + id + " не найден";
            log.error(error);
            throw new NotFoundException(error);
        }
        Optional<User> optFriend = findUserById(otherId);
        if (optFriend.isEmpty()) {
            error = "Пользователь с id = " + otherId + " не найден";
            log.error(error);
            throw new NotFoundException(error);
        }
        String query = """
                    SELECT *
                FROM users AS u
                    INNER JOIN user_friends AS uf1 ON u.id = uf1.user2_id
                INNER JOIN user_friends AS uf2 ON u.id = uf2.user2_id
                    WHERE uf1.user1_id = :id
                    AND uf2.user1_id = :otherId""";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        params.addValue("otherId", otherId);
        log.debug("Получен список общий друзей пользователей c id {} и {}", id, otherId);
        return jdbc.query(query, params, mapper);
    }
}