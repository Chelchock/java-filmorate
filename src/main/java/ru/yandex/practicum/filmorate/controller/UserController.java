package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final List<User> users = new ArrayList<>();
    private long nextId = 1;

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        user.setId(nextId++);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.add(user);
        log.info("Создан новый пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        User existing = users.stream()
                .filter(u -> u.getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        existing.setEmail(user.getEmail());
        existing.setLogin(user.getLogin());
        existing.setName(user.getName());
        existing.setBirthday(user.getBirthday());

        log.info("Пользователь обновлен: {}", existing);
        return existing;
    }
}