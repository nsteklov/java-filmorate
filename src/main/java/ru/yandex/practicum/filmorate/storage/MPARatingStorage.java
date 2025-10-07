package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.MPARating;

import java.util.Collection;
import java.util.Optional;

public interface MPARatingStorage {
    Collection<MPARating> findAll();

    MPARating create(MPARating MPARating);

    MPARating update(MPARating newMPARating);

    Optional<MPARating> findMPARatingById(Long idOfGenre);
}
