package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class FilmExtractor implements ResultSetExtractor {
    @Override
    public Map<Long, Film> extractData(ResultSet resultSet) throws SQLException {
        Map<Long, Film> films = new HashMap<>();
        Long id = null;
        Long prevId = null;
        Film film = null;
        Set<Integer> genres = new HashSet<>();
        while (resultSet.next()) {
            id = resultSet.getLong("id");
            if (prevId != id) {
                if (prevId != null) {
                    film.setGenres(genres);
                    films.put(id, film);
                }
                film = new Film();
                genres = new HashSet<>();
                prevId = id;
            }
            film.setId(resultSet.getLong("id"));
            film.setName(resultSet.getString("name"));
            film.setDescription(resultSet.getString("description"));
            film.setDuration(resultSet.getInt("duration"));
            Date releaseDate = resultSet.getDate("releaseDate");
            if (releaseDate != null) {
                film.setReleaseDate(releaseDate.toLocalDate());
            }
            film.setRating_id(resultSet.getInt("rating_id"));
            int genre_id = resultSet.getInt("genre_id");
            if (genre_id != 0) {
                genres.add(genre_id);
            }
        }
        if (id != null) {
            film.setGenres(genres);
            films.put(id, film);
        }
        return films;
    }
}
