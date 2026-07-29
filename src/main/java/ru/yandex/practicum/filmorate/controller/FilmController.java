package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос на список всех фильмов");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        log.info("Запрос на фильм с id = {}", id);
        return filmService.findById(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Создание фильма: {}", film);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Обновление фильма: {}", film);
        return filmService.update(film);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("Удаление фильма с id = {}", id);
        filmService.delete(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление лайка: filmId={}, userId={}", id, userId);
        filmService.addLike(id, userId);
        return filmService.findById(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка: filmId={}, userId={}", id, userId);
        filmService.removeLike(id, userId);
        return filmService.findById(id);
    }

    @PutMapping("/{id}/{mark}/{userId}")
    public void addMark(@PathVariable Long id, @PathVariable Integer mark, @PathVariable Long userId) {
        log.info("Добавление оценки: filmId={}, userId={}, mark={}", id, userId, mark);
        filmService.markFromUser(id, userId, mark);
    }

    @DeleteMapping("/{id}/mark/{userId}")
    public void deleteMark(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление оценки: filmId={}, userId={}", id, userId);
        filmService.unmarkFromUser(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(required = false) Integer count,
                                 @RequestParam(required = false) Long genreId,
                                 @RequestParam(required = false) Integer year) {
        log.info("Получение популярных фильмов, count={}, genreId={}, year={}", count, genreId, year);
        return filmService.getPopular(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        log.info("Получение общих фильмов пользователей: userId={}, friendId={}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Long directorId, @RequestParam String sortBy) {
        log.info("Получение фильмов режиссёра: directorId={}, sortBy={}", directorId, sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<Film> search(@RequestParam String query, @RequestParam(required = false) String by) {
        log.info("Поиск фильмов: query={}, by={}", query, by);
        return filmService.search(query, by);
    }
}
