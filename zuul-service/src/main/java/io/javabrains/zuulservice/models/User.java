package io.javabrains.zuulservice.models;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class User {

private UserRating userRating;
private List<Movie> movie;

    public User(UserRating userRating, List<Movie> movie) {
        this.userRating = userRating;
        this.movie = movie;
    }

    public User() {
    }
}
