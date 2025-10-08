package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.MPARatingStorage;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    private final MPARatingStorage mpaStorage;

    public FilmRowMapper(MPARatingStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setDuration(resultSet.getInt("duration"));
        Date releaseDate = resultSet.getDate("releaseDate");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }
        int ratingId = resultSet.getInt("rating_id");
        Optional<MPARating> optMpa = mpaStorage.findMPARatingById(ratingId);
        if (optMpa.isPresent()) {
            film.setMpa(optMpa.get());
        }
        film.setGenres(new HashSet<>());

        return film;
    }
}