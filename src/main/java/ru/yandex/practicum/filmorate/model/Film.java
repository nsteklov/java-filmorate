package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class Film {
    private Long id;
    private String name;
    private String description;
    private int ratingId;
    private LocalDate releaseDate;
    private int duration;
    private Set<Integer> genres;
    private Set<Long> likes;
}
