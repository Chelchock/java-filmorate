package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {
    Director create(Director director);

    Director update(Director director);

    List<Director> findAll();

    Director findById(Long id);

    void delete(Long id);
}
