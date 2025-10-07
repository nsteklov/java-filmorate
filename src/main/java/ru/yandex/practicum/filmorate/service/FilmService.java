package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.Collection;
import java.util.Optional;


@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final NamedParameterJdbcOperations jdbc;
    private final FilmRowMapper mapper;
    String error;

    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage, @Qualifier("UserDbStorage") UserStorage userStorage, NamedParameterJdbcOperations jdbc, FilmRowMapper mapper) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Optional<Film> findFilmById(Long idOfFilm) {
        return filmStorage.findFilmById(idOfFilm);
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public void addLike(Long id, Long userId) {
        Optional<User> optUser = userStorage.findUserById(userId);
        if (optUser.isEmpty()) {
            error = "Пользователь с id = " + userId + " не найден";
            log.error(error);
            throw new NotFoundException(error);
        }

        String query = """
                INSERT INTO film_likes (film_id, user_id)
                VALUES (:film_id, :user_id)""";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("film_id", id);
        params.addValue("user_id", userId);
        jdbc.update(query, params);
    }

    public boolean deleteLike(Long id, Long userId) {
        String query = "DELETE FROM film_likes WHERE film_id = :film_id AND user_id = :user_id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("film_id", id);
        params.addValue("user_id", userId);
        int rowsDeleted = jdbc.update(query, params);
        Optional<User> optUser = userStorage.findUserById(userId);
        if (optUser.isEmpty()) {
            log.warn("Пользователь с id = " + userId + " не найден");
        } else {
            log.info("Удален лайк фильма с id {} от пользователя с id {}", id, userId);
        }
        return rowsDeleted > 0;
    }

    public Collection<Film> findTheMostPopular(int count) {
        String query = """
        SELECT * FROM films AS f LEFT JOIN
        (SELECT film_id, count(user_id) AS likes FROM film_likes
        GROUP BY film_id) AS fl ON f.id = fl.film_id
        ORDER BY likes desc 
        LIMIT :count""";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("count", count);
        log.debug("Получен список {} наиболее популярных фильмов", count);
        return jdbc.query(query, params, mapper);
    }
}
