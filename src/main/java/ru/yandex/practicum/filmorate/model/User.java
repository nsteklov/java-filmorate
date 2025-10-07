package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
//import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
//@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<Long> friends;
}
