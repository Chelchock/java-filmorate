package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final Map<Long, Set<Long>> friends = new HashMap<>();

    @Override
    public User create(User user) {
        user.setId(nextId.getAndIncrement());
        users.put(user.getId(), user);
        friends.put(user.getId(), new HashSet<>());
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return user;
    }

    @Override
    public void delete(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        users.remove(id);
        friends.remove(id);
        for (Set<Long> friendSet : friends.values()) {
            friendSet.remove(id);
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        friends.get(userId).add(friendId);
        friends.get(friendId).add(userId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        friends.get(userId).remove(friendId);
        friends.get(friendId).remove(userId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        findById(userId);
        return friends.get(userId).stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        findById(userId);
        findById(otherId);
        Set<Long> common = new HashSet<>(friends.get(userId));
        common.retainAll(friends.get(otherId));
        return common.stream().map(this::findById).collect(Collectors.toList());
    }
}