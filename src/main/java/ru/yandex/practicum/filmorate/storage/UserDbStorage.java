package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
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
        user.setFriends(new HashSet<>());
        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            error = "Id должен быть указан";
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
            user.setFriends(new HashSet<>());
            log.debug("Найден пользователь по id: {}", idOfUser);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}