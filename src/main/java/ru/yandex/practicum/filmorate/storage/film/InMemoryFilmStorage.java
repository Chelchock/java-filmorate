package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final Map<Long, Map<Long, Integer>> filmMarks = new HashMap<>();

    @Override
    public Film create(Film film) {
        film.setId(nextId.getAndIncrement());
        films.put(film.getId(), film);
        film.setRating(0.0);
        filmMarks.put(film.getId(), new HashMap<>());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        updateRating(film.getId());
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
        filmMarks.remove(id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        addMark(filmId, userId, 10);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        removeMark(filmId, userId);
    }

    @Override
    public void addMark(Long filmId, Long userId, int mark) {
        findById(filmId);
        filmMarks.get(filmId).put(userId, mark);
        updateRating(filmId);
    }

    @Override
    public void removeMark(Long filmId, Long userId) {
        findById(filmId);
        filmMarks.get(filmId).remove(userId);
        updateRating(filmId);
    }

    @Override
    public void removeUserLikes(Long userId) {
        for (Map<Long, Integer> marks : filmMarks.values()) {
            marks.remove(userId);
        }
        for (Long filmId : films.keySet()) {
            updateRating(filmId);
        }
    }

    @Override
    public void removeDirector(Long directorId) {
        for (Film film : films.values()) {
            film.getDirectors().removeIf(director -> directorId.equals(director.getId()));
        }
    }

    @Override
    public List<Film> getPopular(int count) {
        return getPopular(count, null, null);
    }

    @Override
    public List<Film> getPopular(int count, Long genreId, Integer year) {
        return films.values().stream()
                .filter(film -> genreId == null || film.getGenres().stream()
                        .anyMatch(genre -> genreId.equals(genre.getId())))
                .filter(film -> year == null || film.getReleaseDate().getYear() == year)
                .sorted(ratingComparator())
                .limit(count)
                .toList();
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return films.values().stream()
                .filter(film -> {
                    Map<Long, Integer> marks = filmMarks.get(film.getId());
                    return marks.containsKey(userId) && marks.containsKey(friendId);
                })
                .sorted(ratingComparator())
                .toList();
    }

    @Override
    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        Comparator<Film> comparator = "year".equals(sortBy)
                ? Comparator.comparing(Film::getReleaseDate).thenComparing(Film::getId)
                : ratingComparator();

        return films.values().stream()
                .filter(film -> film.getDirectors().stream()
                        .anyMatch(director -> directorId.equals(director.getId())))
                .sorted(comparator)
                .toList();
    }

    @Override
    public List<Film> search(String query, boolean byTitle, boolean byDirector) {
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        return films.values().stream()
                .filter(film -> matches(film, normalizedQuery, byTitle, byDirector))
                .sorted(ratingComparator())
                .toList();
    }

    @Override
    public List<Film> getRecommendations(Long userId) {
        Set<Long> markedFilms = films.values().stream()
                .filter(film -> filmMarks.get(film.getId()).containsKey(userId))
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Long, Long> matchingScores = new HashMap<>();
        for (Long filmId : markedFilms) {
            int userMark = filmMarks.get(filmId).get(userId);
            if (userMark <= 5) {
                continue;
            }
            for (Map.Entry<Long, Integer> entry : filmMarks.get(filmId).entrySet()) {
                if (!userId.equals(entry.getKey()) && entry.getValue() > 5
                        && Math.abs(entry.getValue() - userMark) <= 1) {
                    matchingScores.merge(entry.getKey(), 1L, Long::sum);
                }
            }
        }

        Set<Long> similarUsers = matchingScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        return films.values().stream()
                .filter(film -> !markedFilms.contains(film.getId()))
                .filter(film -> filmMarks.get(film.getId()).keySet().stream().anyMatch(similarUsers::contains))
                .filter(film -> film.getRating() >= 6)
                .sorted(ratingComparator())
                .toList();
    }

    public int getLikesCount(Long filmId) {
        return filmMarks.getOrDefault(filmId, new HashMap<>()).size();
    }

    public Double getRating(Long filmId) {
        findById(filmId);
        return films.get(filmId).getRating();
    }

    private Comparator<Film> ratingComparator() {
        return Comparator.comparing(Film::getRating)
                .reversed()
                .thenComparing(Film::getId);
    }

    private void updateRating(Long filmId) {
        Map<Long, Integer> marks = filmMarks.get(filmId);
        double rating = marks.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        films.get(filmId).setRating(rating);
    }

    private boolean matches(Film film, String query, boolean byTitle, boolean byDirector) {
        boolean titleMatches = byTitle && containsIgnoreCase(film.getName(), query);
        boolean directorMatches = byDirector && film.getDirectors().stream()
                .map(director -> director.getName())
                .anyMatch(name -> containsIgnoreCase(name, query));
        return titleMatches || directorMatches;
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
