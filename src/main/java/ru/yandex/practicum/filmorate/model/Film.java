package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * Film.
 */
@Data
@AllArgsConstructor
public class Film {
    private Long id;
    private String name;
    private String description;
    private МРАRating rating;
    private LocalDate releaseDate;
    private int duration;
    private Set<Integer> genres;
    private Set<Long> likes;
}
