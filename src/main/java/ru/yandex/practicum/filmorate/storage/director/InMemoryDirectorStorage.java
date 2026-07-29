package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryDirectorStorage implements DirectorStorage {
    private final Map<Long, Director> directors = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Director create(Director director) {
        director.setId(nextId.getAndIncrement());
        directors.put(director.getId(), director);
        return director;
    }

    @Override
    public Director update(Director director) {
        if (!directors.containsKey(director.getId())) {
            throw new NotFoundException("Режиссёр с id = " + director.getId() + " не найден");
        }
        directors.put(director.getId(), director);
        return director;
    }

    @Override
    public List<Director> findAll() {
        return directors.values().stream()
                .sorted(Comparator.comparing(Director::getId))
                .toList();
    }

    @Override
    public Director findById(Long id) {
        Director director = directors.get(id);
        if (director == null) {
            throw new NotFoundException("Режиссёр с id = " + id + " не найден");
        }
        return director;
    }

    @Override
    public void delete(Long id) {
        findById(id);
        directors.remove(id);
    }
}
