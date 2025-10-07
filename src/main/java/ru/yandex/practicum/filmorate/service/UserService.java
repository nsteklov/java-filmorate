package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final NamedParameterJdbcOperations jdbc;
    private final UserRowMapper mapper;
    String error;

    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage, NamedParameterJdbcOperations jdbc, UserRowMapper mapper) {
        this.userStorage = userStorage;
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public Optional<User> findUserById(Long idOfUser) {
        return userStorage.findUserById(idOfUser);
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public void addFriend(Long id, Long friendId) {
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
