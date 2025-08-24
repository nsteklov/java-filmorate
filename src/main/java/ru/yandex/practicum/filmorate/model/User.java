package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class User {
    Integer id;
    String email;
    String login;
    String name;
    LocalDate birthday;
}
