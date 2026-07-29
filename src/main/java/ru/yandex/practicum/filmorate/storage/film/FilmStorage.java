package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    Collection<Film> findAll();

    Film findById(Long id);

    void delete(Long id);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    void addMark(Long filmId, Long userId, int mark);

    void removeMark(Long filmId, Long userId);

    void removeUserLikes(Long userId);

    void removeDirector(Long directorId);

    List<Film> getPopular(int count);

    List<Film> getPopular(int count, Long genreId, Integer year);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getFilmsByDirector(Long directorId, String sortBy);

    List<Film> search(String query, boolean byTitle, boolean byDirector);

    List<Film> getRecommendations(Long userId);
}
