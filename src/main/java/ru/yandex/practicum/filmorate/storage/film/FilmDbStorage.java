package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
@Primary
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        film.setId(id);

        saveFilmGenres(film);
        loadFilmDetails(film);

        log.debug("Фильм создан в БД: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql,
                film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa() != null ? film.getMpa().getId() : null, film.getId());

        if (rows == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        updateFilmGenres(film);
        loadFilmDetails(film);
        return film;
    }

    @Override
    public Film findById(Long id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, this::mapFilm, id);
            loadFilmDetails(film);
            return film;
        } catch (Exception e) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, this::mapFilm);
        films.forEach(this::loadFilmDetails);
        return films;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        findById(filmId);
        String sql = "MERGE INTO likes (film_id, user_id) KEY(film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        findById(filmId);
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = "SELECT f.*, COUNT(l.user_id) AS likes_count FROM films f " +
                "LEFT JOIN likes l ON f.id = l.film_id GROUP BY f.id ORDER BY likes_count DESC LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapFilm, count);
        films.forEach(this::loadFilmDetails);
        return films;
    }

    private void loadFilmDetails(Film film) {
        if (film == null) return;

        if (film.getMpa() != null && film.getMpa().getId() != null) {
            String mpaSql = "SELECT * FROM mpa_ratings WHERE id = ?";
            try {
                MpaRating mpa = jdbcTemplate.queryForObject(mpaSql, this::mapMpa, film.getMpa().getId());
                film.setMpa(mpa);
            } catch (Exception e) {
                log.warn("MPA с id={} не найден", film.getMpa().getId());
            }
        }

        String genresSql = "SELECT g.* FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id";
        List<Genre> genres = jdbcTemplate.query(genresSql, this::mapGenre, film.getId());
        film.setGenres(genres != null ? genres : new ArrayList<>());
    }

    private Film mapFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getObject("release_date", LocalDate.class));
        film.setDuration(rs.getInt("duration"));

        Long mpaId = rs.getObject("mpa_id", Long.class);
        if (mpaId != null) {
            MpaRating mpa = new MpaRating();
            mpa.setId(mpaId);
            film.setMpa(mpa);
        }
        return film;
    }

    private MpaRating mapMpa(ResultSet rs, int rowNum) throws SQLException {
        MpaRating mpa = new MpaRating();
        mpa.setId(rs.getLong("id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    }

    private Genre mapGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getLong("id"));
        genre.setName(rs.getString("name"));
        return genre;
    }

    private void saveFilmGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        Set<Long> uniqueGenreIds = new HashSet<>();
        List<Object[]> batchArgs = new ArrayList<>();

        for (Genre genre : film.getGenres()) {
            if (uniqueGenreIds.add(genre.getId())) {
                batchArgs.add(new Object[]{film.getId(), genre.getId()});
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    private void updateFilmGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveFilmGenres(film);
    }
}