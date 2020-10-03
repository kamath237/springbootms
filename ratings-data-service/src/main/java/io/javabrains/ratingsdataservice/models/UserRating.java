package io.javabrains.ratingsdataservice.models;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserRating {
    private List<Rating> userRating;

    public UserRating(List<Rating> userRating) {
        this.userRating = userRating;
    }

    public UserRating() {
    }
}
