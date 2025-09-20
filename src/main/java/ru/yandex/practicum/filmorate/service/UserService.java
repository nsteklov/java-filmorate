package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.model.FriendStatus;

import java.util.Collection;
import java.util.Optional;
import java.util.Map;
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
        Map<Long, FriendStatus> friendsOfUser = user.getFriends();
        friendsOfUser.put(friendId, FriendStatus.UNCONFIRMED);
        Map<Long, FriendStatus> friendsOfFriend = friend.getFriends();
        friendsOfFriend.put(id, FriendStatus.UNCONFIRMED);
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
        Optional<User> optFriend = userStorage.findUserById(friendId);
        User user = optUser.get();
        user.getFriends().remove(friendId);
        User friend = optFriend.get();
        friend.getFriends().remove(id);
        log.info("Пользователи {} и {} больше не друзья", user, optFriend.get());
        return user;
    }

    public Collection getUserFriends(Long id) {
        Optional<User> optUser = userStorage.findUserById(id);
        log.debug("Получены список друзей пользователя {}", optUser.get());
        return optUser.get().getFriends().keySet().stream()
                .map(userStorage::findUserById)
                .collect(Collectors.toList());
    }

    public Collection getCommonFriends(Long id, Long otherId) {
        Optional<User> optUser = userStorage.findUserById(id);
        Optional<User> optOtherUser = userStorage.findUserById(otherId);
        log.debug("Получены список общий друзей пользователей {} и {}", optUser.get(), optOtherUser.get());
        return optUser.get().getFriends().keySet().stream()
                .filter(optOtherUser.get().getFriends().keySet()::contains)
                .map(userStorage::findUserById)
                .collect(Collectors.toList());
    }
}
