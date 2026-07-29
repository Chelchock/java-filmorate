package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;
    private final FilmStorage filmStorage;

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        return directorStorage.update(director);
    }

    public List<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director findById(Long id) {
        return directorStorage.findById(id);
    }

    public void delete(Long id) {
        directorStorage.delete(id);
        filmStorage.removeDirector(id);
    }

    public List<Director> normalize(List<Director> directors) {
        if (directors == null) {
            return new ArrayList<>();
        }

        LinkedHashSet<Director> normalizedDirectors = new LinkedHashSet<>();
        for (Director director : directors) {
            if (director == null) {
                throw new IllegalArgumentException("Режиссёр не может быть пустым");
            }
            normalizedDirectors.add(findById(director.getId()));
        }
        return new ArrayList<>(normalizedDirectors);
    }
}
