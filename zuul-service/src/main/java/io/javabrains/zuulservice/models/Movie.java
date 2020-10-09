package io.javabrains.zuulservice.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Movie {
    String movieId;
    String name;

    public Movie() {
    }

    public Movie(String movieId, String name) {
        this.movieId = movieId;
        this.name = name;
    }
}
