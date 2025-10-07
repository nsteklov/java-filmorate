package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/genres")
public class GenreController {
    private final GenreStorage genreStorage;

    public GenreController(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    @GetMapping
    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Genre> findGenreById(@PathVariable Long id) {
        return genreStorage.findGenreById(id);
    }

    @PostMapping
    public Genre create(@RequestBody Genre genre) {
        return genreStorage.create(genre);
    }

    @PutMapping
    public Genre update(@RequestBody Genre newGenre) {
        return genreStorage.update(newGenre);
    }
}
