package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final EventService eventService;

    public Review create(Review review) {
        validateReviewOwnerAndFilm(review);
        Review createdReview = reviewStorage.create(review);
        eventService.add(createdReview.getUserId(), EventType.REVIEW, Operation.ADD);
        return createdReview;
    }

    public Review update(Review review) {
        reviewStorage.findById(review.getReviewId());
        validateReviewOwnerAndFilm(review);
        Review updatedReview = reviewStorage.update(review);
        eventService.add(updatedReview.getUserId(), EventType.REVIEW, Operation.UPDATE);
        return updatedReview;
    }

    public Review findById(Long reviewId) {
        return reviewStorage.findById(reviewId);
    }

    public void delete(Long reviewId) {
        Review review = reviewStorage.findById(reviewId);
        reviewStorage.delete(reviewId);
        eventService.add(review.getUserId(), EventType.REVIEW, Operation.REMOVE);
    }

    public List<Review> findByFilmId(Long filmId, Integer count) {
        filmStorage.findById(filmId);
        if (count != null && count < 1) {
            throw new IllegalArgumentException("Количество отзывов должно быть положительным");
        }
        return reviewStorage.findByFilmId(filmId, count == null ? 10 : count);
    }

    public void addLike(Long reviewId, Long userId) {
        userStorage.findById(userId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        userStorage.findById(userId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        userStorage.findById(userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        userStorage.findById(userId);
        reviewStorage.removeDislike(reviewId, userId);
    }

    private void validateReviewOwnerAndFilm(Review review) {
        userStorage.findById(review.getUserId());
        filmStorage.findById(review.getFilmId());
    }
}
