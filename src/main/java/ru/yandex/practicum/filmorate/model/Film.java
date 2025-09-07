package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
@AllArgsConstructor
public class Film {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    int duration;
    Set<Long> likes;
}
