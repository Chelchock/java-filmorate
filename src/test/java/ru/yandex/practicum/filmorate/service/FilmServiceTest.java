package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.InMemoryGenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.InMemoryMpaRatingStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {

    @Test
    void testCreateFilm() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        InMemoryMpaRatingStorage mpaStorage = new InMemoryMpaRatingStorage();
        InMemoryGenreStorage genreStorage = new InMemoryGenreStorage();

        FilmService filmService = new FilmService(filmStorage, userStorage, mpaStorage, genreStorage);

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(new MpaRating(1L, "G"));
        film.setGenres(List.of(new Genre(1L, "Комедия")));

        Film created = filmService.create(film);

        assertNotNull(created.getId());
        assertEquals("Test Film", created.getName());
    }
}