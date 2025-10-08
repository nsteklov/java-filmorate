package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final NamedParameterJdbcOperations jdbc;
    private final GenreRowMapper mapper;

    @Override
    public List<Genre> findAll() {
        String query = "SELECT * FROM genres";
        return jdbc.query(query, mapper);
    }

    @Override
    public Genre create(Genre genre) {
        String query = """
                INSERT INTO genres (name)
                VALUES (:name)""";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", genre.getName());
        jdbc.update(query, params, keyHolder, new String[]{"id"});
        genre.setId(keyHolder.getKeyAs(Integer.class));
        return genre;
    }

    @Override
    public Genre update(Genre genre) {
        String query = """
                UPDATE genres SET name = :name
                WHERE id = :id""";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", genre.getName());
        params.addValue("id", genre.getId());
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        return genre;
    }

    @Override
    public Optional<Genre> findGenreById(int idOfGenre) {
        String query = "SELECT * FROM genres WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", idOfGenre);
        try {
            Genre genre = jdbc.queryForObject(query, params, mapper);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}
