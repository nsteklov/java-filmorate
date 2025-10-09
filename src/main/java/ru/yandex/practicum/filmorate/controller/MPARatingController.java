package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.MPARatingStorage;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/mpa")
public class MPARatingController {
    private final MPARatingStorage ratingStorage;

    public MPARatingController(MPARatingStorage ratingStorage) {
        this.ratingStorage = ratingStorage;
    }

    @GetMapping
    public Collection<MPARating> findAll() {
        return ratingStorage.findAll();
    }

    @GetMapping("/{id}")
    public Optional<MPARating> findGenreById(@PathVariable int id) {
        return Optional.ofNullable(ratingStorage.findMPARatingById(id).orElseThrow(() -> new NotFoundException("Рейтинг не найден")));
    }
}
