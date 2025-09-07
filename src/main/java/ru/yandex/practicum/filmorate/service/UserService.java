package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class
UserService {
    private final UserStorage userStorage;
    String error;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public User addFriend(Long id, Long friendId) {
        if (id == friendId) {
            error = "Пользователя нельзя добавить в друзья к самому себе";
            log.error(error);
            throw new ValidationException(error);
        }
        Optional<User> optUser = userStorage.findUserById(id);
        Optional<User> optFriend = userStorage.findUserById(friendId);
        User user = optUser.get();
        User friend = optFriend.get();
        Set<Long> friendsOfUser = user.getFriends();
        friendsOfUser.add(friendId);
        Set<Long> friendsOfFriend = friend.getFriends();
        friendsOfFriend.add(id);
        log.info("Пользователи {} и {} стали друзьями", user, friend);
        return user;
    }

    public User deleteFriend(Long id, Long friendId) {
        if (id == friendId) {
            error = "Пользователя не может быть в друзьях у самого себя";
            log.error(error);
            throw new ValidationException(error);
        }
        Optional<User> optUser = userStorage.findUserById(id);
        User user = optUser.get();
        user.getFriends().remove(friendId);
        Optional<User> optFriend = userStorage.findUserById(friendId);
        if (optFriend.isEmpty()) {
            log.warn("Пользователь с id = " + friendId + " не найден");
        } else {
            log.info("Пользователи {} и {} больше не друзья", user, optFriend.get());
        }
        return user;
    }

    public Collection getUserFriends(Long id) {
        Optional<User> optUser = userStorage.findUserById(id);
        log.debug("Получены список друзей пользователя {}", optUser.get());
        return optUser.get().getFriends().stream()
                .map(userStorage::findUserById)
                .collect(Collectors.toList());
    }

    public Collection getCommonFriends(Long id, Long otherId) {
        Optional<User> optUser = userStorage.findUserById(id);
        Optional<User> optOtherUser = userStorage.findUserById(otherId);
        log.debug("Получены список общий друзей пользователей {} и {}", optUser.get(), optOtherUser.get());
        return optUser.get().getFriends().stream()
                .filter(optOtherUser.get().getFriends()::contains)
                .map(userStorage::findUserById)
                .collect(Collectors.toList());
    }
}
