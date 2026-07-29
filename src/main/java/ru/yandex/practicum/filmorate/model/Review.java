package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    private Long reviewId;

    @NotBlank(message = "Текст отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Необходимо указать, является ли отзыв положительным")
    private Boolean isPositive;

    @NotNull(message = "Необходимо указать пользователя")
    private Long userId;

    @NotNull(message = "Необходимо указать фильм")
    private Long filmId;

    private Integer useful;
}
