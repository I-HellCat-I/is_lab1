package com.hellcat.movie_app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hellcat.movie_app.entity.MovieGenre;
import com.hellcat.movie_app.entity.MpaaRating;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MovieDto {
    @NotBlank(message = "Название не может быть пустым.")
    private String name;

    // Координаты приходят как два отдельных поля
    @NotNull
    private Long coordinatesX;
    @NotNull
    @DecimalMin(value = "-157.9", message = "Координата Y должна быть > -158.")
    private Double coordinatesY;

    @NotNull @Positive
    private Long oscarsCount;

    @Positive
    private Double budget;

    @NotNull @Positive
    private Float totalBoxOffice;

    private MpaaRating mpaaRating;

    // Персоны приходят как вложенные объекты с ID
    private PersonRef director;
    @NotNull(message = "Сценарист - обязательное поле!")
    private PersonRef screenwriter;
    private PersonRef operator;

    @NotNull @Positive
    private Integer length;
    @NotNull @Positive
    private Integer goldenPalmCount;
    private MovieGenre genre;
    private Long version;

    // Вложенный класс для ссылок на персон
    @Data
    public static class PersonRef {
        private Long id;
    }

    @JsonProperty("directorId")
    public void setDirectorId(Long id) {
        if (id != null) {
            this.director = new PersonRef();
            this.director.setId(id);
        }
    }

    @JsonProperty("screenwriterId")
    public void setScreenwriterId(Long id) {
        if (id != null) {
            this.screenwriter = new PersonRef();
            this.screenwriter.setId(id);
        }
    }

    @JsonProperty("operatorId")
    public void setOperatorId(Long id) {
        if (id != null) {
            this.operator = new PersonRef();
            this.operator.setId(id);
        }
    }
}