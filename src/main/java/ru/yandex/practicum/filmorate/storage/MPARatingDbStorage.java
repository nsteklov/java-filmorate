package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
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
    public Optional<MPARating> findMPARatingById(int idOfMPARating) {
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
