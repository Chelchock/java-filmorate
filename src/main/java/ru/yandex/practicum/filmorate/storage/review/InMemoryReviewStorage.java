package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryReviewStorage implements ReviewStorage {
    private final Map<Long, Review> reviews = new HashMap<>();
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private final Map<Long, Set<Long>> dislikes = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Review create(Review review) {
        review.setReviewId(nextId.getAndIncrement());
        review.setUseful(0);
        reviews.put(review.getReviewId(), review);
        likes.put(review.getReviewId(), new HashSet<>());
        dislikes.put(review.getReviewId(), new HashSet<>());
        return review;
    }

    @Override
    public Review update(Review review) {
        findById(review.getReviewId());
        review.setUseful(getUseful(review.getReviewId()));
        reviews.put(review.getReviewId(), review);
        return review;
    }

    @Override
    public Review findById(Long reviewId) {
        Review review = reviews.get(reviewId);
        if (review == null) {
            throw new NotFoundException("Отзыв с id = " + reviewId + " не найден");
        }
        return review;
    }

    @Override
    public void delete(Long reviewId) {
        findById(reviewId);
        reviews.remove(reviewId);
        likes.remove(reviewId);
        dislikes.remove(reviewId);
    }

    @Override
    public List<Review> findByFilmId(Long filmId, int count) {
        return reviews.values().stream()
                .filter(review -> filmId.equals(review.getFilmId()))
                .sorted(Comparator.comparingInt(Review::getUseful).reversed()
                        .thenComparing(Review::getReviewId))
                .limit(count)
                .toList();
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        findById(reviewId);
        likes.get(reviewId).add(userId);
        dislikes.get(reviewId).remove(userId);
        updateUseful(reviewId);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        findById(reviewId);
        likes.get(reviewId).remove(userId);
        updateUseful(reviewId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        findById(reviewId);
        dislikes.get(reviewId).add(userId);
        likes.get(reviewId).remove(userId);
        updateUseful(reviewId);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        findById(reviewId);
        dislikes.get(reviewId).remove(userId);
        updateUseful(reviewId);
    }

    private int getUseful(Long reviewId) {
        return likes.get(reviewId).size() - dislikes.get(reviewId).size();
    }

    private void updateUseful(Long reviewId) {
        findById(reviewId).setUseful(getUseful(reviewId));
    }
}
