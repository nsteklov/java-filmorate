package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final UserStorage userStorage;
    String error;

    public InMemoryFilmStorage(@Qualifier("InMemoryUserStorage")UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public Collection<Film> findAll() {
        log.debug("Получен список фильмов");
        return films.values();
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
        film.setId(getNextId());
        film.setGenres(new HashSet<>());
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            error = "Id должен быть указан";
            log.error(error);
            throw new NotFoundException(error);
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() == null || newFilm.getName().isBlank()) {
                error = "Название не может быть пустым";
                log.error(error);
                throw new ValidationException(error);
            }
            if (newFilm.getDescription().length() > 200) {
                error = "Длина описания не может превышать 200 символов";
                log.error(error);
                throw new ValidationException(error);
            }
            if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                error = "Дата релиза не может быть раньше 28 декабря 1895 года";
                log.error(error);
                throw new ValidationException(error);
            }
            if (oldFilm.getDuration() <= 0) {
                error = "Продолжительность фильма должна быть положительным числом";
                log.error(error);
                throw new ValidationException(error);
            }
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setGenres(newFilm.getGenres());
            oldFilm.setMpa(newFilm.getMpa());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            log.info("Обновлены данные фильма: {}", oldFilm);
            return oldFilm;
        }
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    @Override
    public Optional<Film> findFilmById(Long idOfFilm) {
        Film film = films.keySet()
                .stream()
                .map(id -> Optional.ofNullable(films.get(idOfFilm)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + idOfFilm + " не найден"));
        log.debug("Найден фильм по id: {}", idOfFilm);
        return Optional.ofNullable(film);
    }

    public void addLike(Long id, Long userId) {
    }

    public boolean deleteLike(Long id, Long userId) {
        return true;
    }

    public Collection<Film> findTheMostPopular(int count) {
        return null;
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        log.debug("Получен следующий по порядку id фильма: {}", ++currentMaxId);
        return currentMaxId;
    }
}
