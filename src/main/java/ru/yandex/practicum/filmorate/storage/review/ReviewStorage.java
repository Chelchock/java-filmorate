package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    Review findById(Long reviewId);

    void delete(Long reviewId);

    List<Review> findByFilmId(Long filmId, int count);

    void addLike(Long reviewId, Long userId);

    void removeLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void removeDislike(Long reviewId, Long userId);
}
