package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    String error;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Film addLike(Long id, Long userId) {
        Optional<Film> optFilm = filmStorage.findFilmById(id);
        Optional<User> optUser = userStorage.findUserById(userId);
        if (optUser.isEmpty()) {
            error = "Пользователь с id = " + userId + " не найден";
            log.error(error);
            throw new NotFoundException(error);
        }
        Film film = optFilm.get();
        Set<Long> likes = film.getLikes();
        likes.add(userId);
        log.info("Добавлен лайк фильму: {} от пользователя: {}", film, optUser.get());
        return film;
    }

    public Film deleteLike(Long id, Long userId) {
        Optional<Film> optFilm = filmStorage.findFilmById(id);
        Film film = optFilm.get();
        film.getLikes().remove(userId);
        Optional<User> optUser = userStorage.findUserById(userId);
        if (optUser.isEmpty()) {
            log.warn("Пользователь с id = " + userId + " не найден");
        } else {
            log.info("Удален лайк фильма: {} от пользователя: {}", film, optUser.get());
        }
        return film;
    }

    public Collection<Film> findTheMostPopular(int count) {
        Comparator<Film> likeComparator = Comparator.comparing(film -> film.getLikes().size());
        likeComparator = likeComparator.reversed();
        log.debug("Получен список {} наиболее популярных фильмов", count);
        return filmStorage.findAll()
                .stream()
                .sorted(likeComparator)
                .limit(count)
                .collect(Collectors.toList());
    }
}
