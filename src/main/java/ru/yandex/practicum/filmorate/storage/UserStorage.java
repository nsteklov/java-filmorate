package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User update(User newUser);

    Optional<User> findUserById(Long idOfUser);

    void addFriend(Long id, Long friendId);

    boolean deleteFriend(Long id, Long friendId);

    Collection<User> getUserFriends(Long id);

    Collection<User> getCommonFriends(Long id, Long otherId);
}
