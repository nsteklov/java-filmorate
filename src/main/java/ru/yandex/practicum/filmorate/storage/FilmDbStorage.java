package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmExtractor;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
@Qualifier("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final NamedParameterJdbcOperations jdbc;
    private final FilmRowMapper mapper;
    private final FilmExtractor extractor;
    String error;

    @Override
    public List<Film> findAll() {
        String query = "SELECT * FROM films";
        return jdbc.query(query, mapper);
    }

    @Override
    public Film create(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            error = "Название не может быть пустым";
            log.error(error);
            throw new ValidationException(error);
        }
        if (film.getDescription().length() > 200) {
            error = "Длина описания не может превышать 200 символов";
            log.error(error);
            throw new ValidationException(error);
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            error = "Дата релиза не может быть раньше 28 декабря 1895 года";
            log.error(error);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            error = "Продолжительность фильма должна быть положительным числом";
            log.error(error);
            throw new ValidationException(error);
        }
        String query = """
                 INSERT INTO films (name, description, rating_id, releaseDate, duration)
                 VALUES (:name, :description, :rating_id, :releaseDate, :duration);""";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", film.getName());
        params.addValue("description", film.getDescription());
        params.addValue("rating_id", film.getRatingId());
        params.addValue("releaseDate", film.getReleaseDate());
        params.addValue("duration", film.getDuration());
        jdbc.update(query, params, keyHolder, new String[]{"id"});
        film.setId(keyHolder.getKeyAs(Long.class));
        film.setLikes(new HashSet<>());
        Long filmId = film.getId();
        Set<Integer> genres = film.getGenres();
        if (genres != null) {
            String queryGenres = """
                 INSERT INTO film_genres (genre_id, film_id)
                 VALUES (:genre_id, :film_id);""";
            for (Integer genreId : genres) {
                MapSqlParameterSource paramsGenre = new MapSqlParameterSource();
                params.addValue("genre_id", genreId);
                params.addValue("film_id", filmId);
                jdbc.update(queryGenres, params);
            }
        }
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            error = "Id должен быть указан";
            log.error(error);
            throw new NotFoundException(error);
        }
            if (film.getName() == null || film.getName().isBlank()) {
                error = "Название не может быть пустым";
                log.error(error);
                throw new ValidationException(error);
            }
            if (film.getDescription().length() > 200) {
                error = "Длина описания не может превышать 200 символов";
                log.error(error);
                throw new ValidationException(error);
            }
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                error = "Дата релиза не может быть раньше 28 декабря 1895 года";
                log.error(error);
                throw new ValidationException(error);
            }
            if (film.getDuration() <= 0) {
                error = "Продолжительность фильма должна быть положительным числом";
                log.error(error);
                throw new ValidationException(error);
            }
        String query = """
        UPDATE films SET name = :name, description = :description, rating_id = :rating_id, releaseDate = :releaseDate, duration = :duration 
        WHERE id = :id""";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", film.getName());
        params.addValue("description", film.getDescription());
        params.addValue("rating_id", film.getRatingId());
        params.addValue("releaseDate", film.getReleaseDate());
        params.addValue("duration", film.getDuration());
        params.addValue("id", film.getId());
        int rowsUpdated = jdbc.update(query, params);
        Long filmId = film.getId();
        String queryDeleteGenres = "DELETE FROM film_genres WHERE film_id = :film_id";
        MapSqlParameterSource paramsDelete = new MapSqlParameterSource();
        paramsDelete.addValue("film_id", filmId);
        jdbc.update(queryDeleteGenres, paramsDelete);
        System.out.println("d");
        Set<Integer> genres = film.getGenres();
        if (genres != null) {
            String queryGenres = """
                 INSERT INTO film_genres (genre_id, film_id)
                 VALUES (:genre_id, :film_id);""";
            for (Integer genreId : genres) {
                MapSqlParameterSource paramsGenre = new MapSqlParameterSource();
                params.addValue("genre_id", genreId);
                params.addValue("film_id", filmId);
                jdbc.update(queryGenres, params);
            }
        }
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        log.info("Обновлены данные фильма: {}", film);
        return film;
    }

    @Override
    public Optional<Film> findFilmById(Long idOfFilm) {
        String query = """
                SELECT id, name, description, rating_id, releaseDate, duration, genre_id
                FROM films LEFT JOIN film_genres ON id = film_id
                WHERE id = :id""";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", idOfFilm);
        try {
            Map films = (Map) jdbc.query(query, params, extractor);
            log.debug("Найден фильм по id: {}", idOfFilm);
            Film film = (Film)films.get(idOfFilm);
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}