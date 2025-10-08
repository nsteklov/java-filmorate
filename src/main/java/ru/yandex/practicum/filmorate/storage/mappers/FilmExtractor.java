package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MPARatingStorage;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FilmExtractor implements ResultSetExtractor {
    private final MPARatingStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmExtractor(MPARatingStorage mpaStorage, GenreStorage genreStorage) {
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    @Override
    public Map<Long, Film> extractData(ResultSet resultSet) throws SQLException {
        Map<Long, Film> films = new HashMap<>();
        Long id = null;
        Long prevId = null;
        Film film = null;
        Comparator<Genre> genreComparator = Comparator.comparingInt(Genre::getId);
        Set<Genre> genres = new HashSet<>();
        while (resultSet.next()) {
            id = resultSet.getLong("id");
            if (prevId != id) {
                if (prevId != null) {
                    Set<Genre> genreTree = new TreeSet<>(genreComparator);
                    genreTree.addAll(genres);
                    film.setGenres(genreTree);
                    films.put(id, film);
                }
                film = new Film();
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
            int ratingId = resultSet.getInt("rating_id");
            Optional<MPARating> optMpa = mpaStorage.findMPARatingById(ratingId);
            if (optMpa.isPresent()) {
                film.setMpa(optMpa.get());
            }
            int genreId = resultSet.getInt("genre_id");
            Optional<Genre> optGenre = genreStorage.findGenreById(genreId);
            if (optGenre.isPresent()) {
                Genre genre = optGenre.get();
                genres.add(genre);
            }
        }
        if (id != null) {
            Set<Genre> genreTree = new TreeSet<>(genreComparator);
            genreTree.addAll(genres);
            film.setGenres(genreTree);
            films.put(id, film);
        }
        return films;
    }
}
