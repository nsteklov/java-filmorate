package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MPARating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MPARatingRowMapper implements RowMapper<MPARating> {
    @Override
    public MPARating mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        MPARating rating = new MPARating();
        rating.setId(resultSet.getInt("id"));
        rating.setName(resultSet.getString("name"));

        return rating;
    }
}
