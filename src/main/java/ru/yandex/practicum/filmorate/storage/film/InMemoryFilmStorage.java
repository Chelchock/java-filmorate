package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();

    @Override
    public Film create(Film film) {
        film.setId(nextId.getAndIncrement());
        films.put(film.getId(), film);
        filmLikes.put(film.getId(), new HashSet<>());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film findById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        return film;
    }

    @Override
    public void delete(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        films.remove(id);
        filmLikes.remove(id);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        filmLikes.get(filmId).add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        filmLikes.get(filmId).remove(userId);
    }

    public List<Film> getPopular(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(
                        filmLikes.get(f2.getId()).size(),
                        filmLikes.get(f1.getId()).size()))
                .limit(count)
                .toList();
    }

    public int getLikesCount(Long filmId) {
        return filmLikes.getOrDefault(filmId, new HashSet<>()).size();
    }
}