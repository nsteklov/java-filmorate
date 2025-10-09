package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class Film {
    private Long id;
    private String name;
    private String description;
    private MPARating mpa;
    private LocalDate releaseDate;
    private int duration;
    private Set<Genre> genres;
}
