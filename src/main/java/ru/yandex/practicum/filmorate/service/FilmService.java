package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreService genreService;
    private final DirectorService directorService;
    private final EventService eventService;

    public Film create(Film film) {
        log.debug("Создание фильма: {}", film);
        film.setGenres(genreService.normalize(film.getGenres()));
        film.setDirectors(directorService.normalize(film.getDirectors()));
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        log.debug("Обновление фильма: {}", film);
        film.setGenres(genreService.normalize(film.getGenres()));
        film.setDirectors(directorService.normalize(film.getDirectors()));
        return filmStorage.update(film);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id);
    }

    public void delete(Long id) {
        log.debug("Удаление фильма: id={}", id);
        filmStorage.delete(id);
    }

    public void addLike(Long filmId, Long userId) {
        log.debug("Добавление лайка: filmId={}, userId={}", filmId, userId);
        filmStorage.findById(filmId);
        userStorage.findById(userId);
        filmStorage.addLike(filmId, userId);
        eventService.add(userId, EventType.LIKE, Operation.ADD);
    }

    public void removeLike(Long filmId, Long userId) {
        log.debug("Удаление лайка: filmId={}, userId={}", filmId, userId);
        filmStorage.findById(filmId);
        userStorage.findById(userId);
        filmStorage.removeLike(filmId, userId);
        eventService.add(userId, EventType.LIKE, Operation.REMOVE);
    }

    public void markFromUser(Long filmId, Long userId, Integer mark) {
        if (mark == null || mark < 1 || mark > 10) {
            throw new IllegalArgumentException("Оценка должна быть целым числом от 1 до 10");
        }
        filmStorage.findById(filmId);
        userStorage.findById(userId);
        filmStorage.addMark(filmId, userId, mark);
        eventService.add(userId, EventType.LIKE, Operation.ADD);
    }

    public void unmarkFromUser(Long filmId, Long userId) {
        filmStorage.findById(filmId);
        userStorage.findById(userId);
        filmStorage.removeMark(filmId, userId);
        eventService.add(userId, EventType.LIKE, Operation.REMOVE);
    }

    public List<Film> getPopular(Integer count, Long genreId, Integer year) {
        if (genreId != null) {
            genreService.findById(genreId);
        }

        int limit = count != null && count > 0 ? count : 10;
        return filmStorage.getPopular(limit, genreId, year);
    }

    public List<Film> getPopular(Integer count) {
        return getPopular(count, null, null);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        userStorage.findById(userId);
        userStorage.findById(friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        if (!"year".equals(sortBy) && !"likes".equals(sortBy) && !"marks".equals(sortBy)) {
            throw new IllegalArgumentException("Параметр sortBy должен быть year, likes или marks");
        }
        directorService.findById(directorId);
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }

    public List<Film> search(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Поисковый запрос не может быть пустым");
        }

        Set<String> searchFields = parseSearchFields(by);
        return filmStorage.search(query, searchFields.contains("title"), searchFields.contains("director"));
    }

    private Set<String> parseSearchFields(String by) {
        String fields = by == null || by.isBlank() ? "title,director" : by;
        Set<String> searchFields = new HashSet<>();
        Arrays.stream(fields.split(","))
                .map(String::trim)
                .forEach(searchFields::add);

        if (searchFields.isEmpty() || searchFields.stream()
                .anyMatch(field -> !"title".equals(field) && !"director".equals(field))) {
            throw new IllegalArgumentException("Параметр by может содержать только title и director");
        }
        return searchFields;
    }
}
