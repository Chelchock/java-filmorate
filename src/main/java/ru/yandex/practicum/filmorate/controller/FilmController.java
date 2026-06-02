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
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final List<Film> films = new ArrayList<>();
    private long nextId = 1;

    @GetMapping
    public List<Film> getAllFilms() {
        return new ArrayList<>(films);
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        validateReleaseDate(film);
        film.setId(nextId++);
        films.add(film);
        log.info("Фильм добавлен: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        validateReleaseDate(film);
        Film existing = films.stream()
                .filter(f -> f.getId().equals(film.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));

        existing.setName(film.getName());
        existing.setDescription(film.getDescription());
        existing.setReleaseDate(film.getReleaseDate());
        existing.setDuration(film.getDuration());

        log.info("Фильм обновлен: {}", existing);
        return existing;
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза должна быть не позднее 28 декабря 1895 года");
        }
    }
}
