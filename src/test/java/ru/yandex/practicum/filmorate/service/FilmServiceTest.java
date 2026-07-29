package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.InMemoryDirectorStorage;
import ru.yandex.practicum.filmorate.storage.event.InMemoryEventStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.review.InMemoryReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmServiceTest {
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;
    private FilmService filmService;
    private UserService userService;
    private DirectorService directorService;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        InMemoryDirectorStorage directorStorage = new InMemoryDirectorStorage();
        GenreService genreService = new GenreService();
        EventService eventService = new EventService(new InMemoryEventStorage());
        directorService = new DirectorService(directorStorage, filmStorage);
        filmService = new FilmService(filmStorage, userStorage, genreService, directorService, eventService);
        userService = new UserService(userStorage, filmStorage, eventService);
        reviewService = new ReviewService(new InMemoryReviewStorage(), filmStorage, userStorage, eventService);
    }

    @Test
    void shouldReturnCommonFilmsSortedByPopularity() {
        User firstUser = createUser("first@example.com", "first");
        User secondUser = createUser("second@example.com", "second");
        User thirdUser = createUser("third@example.com", "third");

        Film popularFilm = createFilm("Популярный", 2024, 1L);
        Film lessPopularFilm = createFilm("Менее популярный", 2024, 1L);
        Film privateFilm = createFilm("Личный", 2024, 2L);

        addLike(popularFilm, firstUser, secondUser, thirdUser);
        addLike(lessPopularFilm, firstUser, secondUser);
        addLike(privateFilm, firstUser);

        List<Long> actualFilmIds = filmService.getCommonFilms(firstUser.getId(), secondUser.getId()).stream()
                .map(Film::getId)
                .toList();

        assertEquals(List.of(popularFilm.getId(), lessPopularFilm.getId()), actualFilmIds);
    }

    @Test
    void shouldFilterPopularFilmsByGenreAndYear() {
        User user = createUser("user@example.com", "user");
        Film matchingFilm = createFilm("Подходит", 2024, 1L);
        Film differentGenreFilm = createFilm("Другой жанр", 2024, 2L);
        Film differentYearFilm = createFilm("Другой год", 2023, 1L);

        addLike(matchingFilm, user);
        addLike(differentGenreFilm, user);
        addLike(differentYearFilm, user);

        List<Long> actualFilmIds = filmService.getPopular(10, 1L, 2024).stream()
                .map(Film::getId)
                .toList();

        assertEquals(List.of(matchingFilm.getId()), actualFilmIds);
    }

    @Test
    void shouldDeleteUserAndRelatedLikes() {
        User deletedUser = createUser("deleted@example.com", "deleted");
        User remainingUser = createUser("remaining@example.com", "remaining");
        Film film = createFilm("Фильм", 2024, 1L);

        userService.addFriend(deletedUser.getId(), remainingUser.getId());
        addLike(film, deletedUser, remainingUser);
        userService.delete(deletedUser.getId());

        assertThrows(NotFoundException.class, () -> userService.findById(deletedUser.getId()));
        assertEquals(List.of(), userService.getFriends(remainingUser.getId()));
        assertEquals(1, filmStorage.getLikesCount(film.getId()));
    }

    @Test
    void shouldReturnDirectorFilmsAndSearchResults() {
        User firstUser = createUser("first@example.com", "first");
        User secondUser = createUser("second@example.com", "second");
        Director director = createDirector("Алиса Режиссёр");
        Director anotherDirector = createDirector("Борис Режиссёр");
        Film firstDirectorFilm = createFilm("Звёздный путь", 2024, 1L, director);
        Film secondDirectorFilm = createFilm("Приключение", 2023, 1L, director);
        Film titleMatchFilm = createFilm("Путь домой", 2024, 2L, anotherDirector);

        addLike(firstDirectorFilm, firstUser, secondUser);
        addLike(titleMatchFilm, firstUser);

        List<Long> directorFilmIds = filmService.getFilmsByDirector(director.getId(), "year").stream()
                .map(Film::getId)
                .toList();
        List<Long> titleSearchIds = filmService.search("путь", "title").stream()
                .map(Film::getId)
                .toList();
        List<Long> directorSearchIds = filmService.search("алиса", "director").stream()
                .map(Film::getId)
                .toList();

        assertEquals(List.of(secondDirectorFilm.getId(), firstDirectorFilm.getId()), directorFilmIds);
        assertEquals(List.of(firstDirectorFilm.getId(), titleMatchFilm.getId()), titleSearchIds);
        assertEquals(List.of(firstDirectorFilm.getId(), secondDirectorFilm.getId()), directorSearchIds);
    }

    @Test
    void shouldCalculateReviewUsefulnessAndAddReviewEventsToFeed() {
        User author = createUser("author@example.com", "author");
        User critic = createUser("critic@example.com", "critic");
        Film film = createFilm("Фильм", 2024, 1L);
        Review review = createReview(author, film, "Отличный фильм");

        reviewService.addLike(review.getReviewId(), critic.getId());
        reviewService.addDislike(review.getReviewId(), critic.getId());
        reviewService.addLike(review.getReviewId(), critic.getId());
        review.setContent("Обновлённый отзыв");
        reviewService.update(review);

        assertEquals(1, reviewService.findById(review.getReviewId()).getUseful());
        List<EventType> eventTypes = userService.getFeed(author.getId()).stream()
                .map(event -> event.getEventType())
                .toList();
        assertEquals(List.of(EventType.REVIEW, EventType.REVIEW), eventTypes);
    }

    @Test
    void shouldRecommendFilmsLikedByUsersWithSimilarTaste() {
        User targetUser = createUser("target@example.com", "target");
        User similarUser = createUser("similar@example.com", "similar");
        User unrelatedUser = createUser("unrelated@example.com", "unrelated");
        Film commonFilm = createFilm("Общий", 2024, 1L);
        Film recommendedFilm = createFilm("Рекомендация", 2024, 1L);
        Film unrelatedFilm = createFilm("Не подходит", 2024, 1L);

        addLike(commonFilm, targetUser, similarUser);
        addLike(recommendedFilm, similarUser);
        addLike(unrelatedFilm, unrelatedUser);

        List<Long> recommendations = userService.getRecommendations(targetUser.getId()).stream()
                .map(Film::getId)
                .toList();

        assertEquals(List.of(recommendedFilm.getId()), recommendations);
    }

    @Test
    void shouldCalculateRatingFromMarksAndSortPopularFilmsByRating() {
        User firstUser = createUser("first@example.com", "first");
        User secondUser = createUser("second@example.com", "second");
        Film lowerRatedFilm = createFilm("Низкий рейтинг", 2024, 1L);
        Film higherRatedFilm = createFilm("Высокий рейтинг", 2024, 1L);

        filmService.markFromUser(lowerRatedFilm.getId(), firstUser.getId(), 5);
        filmService.markFromUser(lowerRatedFilm.getId(), secondUser.getId(), 7);
        filmService.markFromUser(higherRatedFilm.getId(), firstUser.getId(), 9);
        filmService.markFromUser(higherRatedFilm.getId(), secondUser.getId(), 10);

        List<Film> popularFilms = filmService.getPopular(2);

        assertEquals(6.0, lowerRatedFilm.getRating());
        assertEquals(9.5, higherRatedFilm.getRating());
        assertEquals(List.of(higherRatedFilm.getId(), lowerRatedFilm.getId()), popularFilms.stream()
                .map(Film::getId)
                .toList());
        assertThrows(IllegalArgumentException.class,
                () -> filmService.markFromUser(lowerRatedFilm.getId(), firstUser.getId(), 11));
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        return userService.create(user);
    }

    private Film createFilm(String name, int releaseYear, Long genreId) {
        return createFilm(name, releaseYear, genreId, new Director[0]);
    }

    private Film createFilm(String name, int releaseYear, Long genreId, Director... directors) {
        Genre genre = new Genre();
        genre.setId(genreId);

        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(releaseYear, 1, 1));
        film.setDuration(120);
        film.setGenres(List.of(genre));
        film.setDirectors(List.of(directors));
        return filmService.create(film);
    }

    private Director createDirector(String name) {
        Director director = new Director();
        director.setName(name);
        return directorService.create(director);
    }

    private Review createReview(User user, Film film, String content) {
        Review review = new Review();
        review.setUserId(user.getId());
        review.setFilmId(film.getId());
        review.setContent(content);
        review.setIsPositive(true);
        return reviewService.create(review);
    }

    private void addLike(Film film, User... users) {
        for (User user : users) {
            filmService.addLike(film.getId(), user.getId());
        }
    }
}
