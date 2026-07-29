package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class GenreService {
    private static final Map<Long, String> GENRES = Map.of(
            1L, "Комедия",
            2L, "Драма",
            3L, "Мультфильм",
            4L, "Триллер",
            5L, "Документальный",
            6L, "Боевик"
    );

    public Genre findById(Long id) {
        String name = GENRES.get(id);
        if (name == null) {
            throw new NotFoundException("Жанр с id = " + id + " не найден");
        }

        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        return genre;
    }

    public List<Genre> normalize(List<Genre> genres) {
        if (genres == null) {
            return new ArrayList<>();
        }

        LinkedHashSet<Genre> normalizedGenres = new LinkedHashSet<>();
        for (Genre genre : genres) {
            if (genre == null) {
                throw new IllegalArgumentException("Жанр не может быть пустым");
            }
            normalizedGenres.add(findById(genre.getId()));
        }
        return new ArrayList<>(normalizedGenres);
    }
}
