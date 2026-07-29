package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.MpaRating;
import java.util.*;

public class InMemoryMpaRatingStorage implements MpaRatingStorage {
    private final Map<Long, MpaRating> mpaRatings = new HashMap<>();

    public InMemoryMpaRatingStorage() {
        mpaRatings.put(1L, new MpaRating(1L, "G"));
        mpaRatings.put(2L, new MpaRating(2L, "PG"));
        mpaRatings.put(3L, new MpaRating(3L, "PG-13"));
        mpaRatings.put(4L, new MpaRating(4L, "R"));
        mpaRatings.put(5L, new MpaRating(5L, "NC-17"));
    }

    @Override
    public Collection<MpaRating> findAll() {
        return mpaRatings.values();
    }

    @Override
    public Optional<MpaRating> findById(Long id) {
        return Optional.ofNullable(mpaRatings.get(id));
    }
}