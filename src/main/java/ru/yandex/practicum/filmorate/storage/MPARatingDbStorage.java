package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.mappers.MPARatingRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MPARatingDbStorage implements MPARatingStorage {
    private final NamedParameterJdbcOperations jdbc;
    private final MPARatingRowMapper mapper;

    @Override
    public List<MPARating> findAll() {
        String query = "SELECT * FROM MPA_rating";
        return jdbc.query(query, mapper);
    }

    @Override
    public MPARating create(MPARating rating) {
        String query = """
                 INSERT INTO MPA_rating (name)
                 VALUES (:name)""";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", rating.getName());
        jdbc.update(query, params, keyHolder, new String[]{"id"});
        rating.setId(keyHolder.getKeyAs(Integer.class));
        return rating;
    }

    @Override
    public MPARating update(MPARating rating) {
        String query = """
        UPDATE MPA_rating SET name = :name 
        WHERE id = :id""";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", rating.getName());
        params.addValue("id", rating.getId());
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        return rating;
    }

    @Override
    public Optional<MPARating> findMPARatingById(Long idOfMPARating) {
        String query = "SELECT * FROM MPA_rating WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", idOfMPARating);
        try {
            MPARating rating = jdbc.queryForObject(query, params, mapper);
            return Optional.ofNullable(rating);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}
