package io.javabrains.ratingsdataservice.resources;

import io.javabrains.ratingsdataservice.models.Movie;
import io.javabrains.ratingsdataservice.models.Rating;
import io.javabrains.ratingsdataservice.models.UserRating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class RatingsResource {
    private static Logger log = LoggerFactory.getLogger(RatingsResource.class);


    @Bean
    //@LoadBalanced
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    @Autowired
    private RestTemplate restTemplate = getRestTemplate();

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

    @RequestMapping("/ratingsdata/{movieId}")
    public Rating getRating(@PathVariable("movieId") String movieId){

        return new Rating(movieId, 4);
    }

    @RequestMapping("/ratingsdata/users/{userId}")
    public UserRating getUserRating(@PathVariable("userId") String userId,@RequestHeader("authorization") String authzToken) {

        log.info("authorization :"+authzToken);

      /*  Movie movie = webClientBuilder.build()
                .get()
                .uri("http://localhost:8089/movies/123")
                .header("Authorization",authzToken)
                .retrieve()
                .bodyToMono(Movie.class)
                .block();


        System.out.println(">>>> movie - "+movie.getName());*/

        List<Rating> ratings = Arrays.asList(
                new Rating("1000", 4),
                new Rating("5678", 3));

        UserRating userRating = new UserRating();
        userRating.setUserRating(ratings);
log.info("returning user rating");
        return userRating;
    }

}
