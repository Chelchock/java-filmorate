package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ru.yandex.practicum.filmorate.validation.ValidReleaseDate;

@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата релиза должна быть указана")
    @ValidReleaseDate
    private LocalDate releaseDate;

    @Min(value = 1, message = "Продолжительность должна быть положительным числом")
    private Integer duration;

    private List<Genre> genres = new ArrayList<>();

    private List<Director> directors = new ArrayList<>();

    private Double rating = 0.0;
}
