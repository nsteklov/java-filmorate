package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.Collection;
import java.util.Optional;


@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;

    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage, NamedParameterJdbcOperations jdbc, FilmRowMapper mapper) {
        this.filmStorage = filmStorage;
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
        filmStorage.addLike(id, userId);
    }

    public boolean deleteLike(Long id, Long userId) {
        return filmStorage.deleteLike(id, userId);
    }

    public Collection<Film> findTheMostPopular(int count) {
        return filmStorage.findTheMostPopular(count);
    }
}
