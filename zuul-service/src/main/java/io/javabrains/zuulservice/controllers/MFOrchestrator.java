package io.javabrains.zuulservice.controllers;

import io.javabrains.zuulservice.models.Movie;
import io.javabrains.zuulservice.models.User;
import io.javabrains.zuulservice.models.UserRating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class MFOrchestrator {
    private static Logger log = LoggerFactory.getLogger(MFOrchestrator.class);

    @Bean
//    @LoadBalanced
    public WebClient.Builder getWebClient(){
        return WebClient.builder();
    }

    @Autowired
    private WebClient.Builder webClientBuilder;

    @RequestMapping("/")
    public String home(){
        return "index";
    }

    @RequestMapping("/api/users/{userId}")
    public User getUserRating(@PathVariable("userId") String userId, @RequestHeader("authorization") String authzToken) {

        log.info("authorization :"+authzToken);
        System.out.println(">>>orchestrating");
        User user = new User ();
        UserRating userRating = webClientBuilder.build()
                .get()
                .uri("http://localhost:8089/ratingsdata/users/"+userId)
                .header("Authorization",authzToken)
                .retrieve()
                .bodyToMono(UserRating.class)
                .block();

        //TODO implent .onError()
        // Movie movie = restTemplate.getForObject("http://localhost:8089/movies/123", Movie.class);

        user.setUserRating(userRating);
        log.info(">>>   user rating retrieved");
        List<Movie> movies = userRating.getUserRating().stream().map(userrating -> {
            // for each movieid call movie info service and get details
            Movie movie = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8089/movies/" + userrating.getMovieId())
                    .header("Authorization",authzToken)
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();

            return movie;
        }).collect(Collectors.toList());
                //put them all together

        user.setMovie(movies);

        return user;
    }
}
