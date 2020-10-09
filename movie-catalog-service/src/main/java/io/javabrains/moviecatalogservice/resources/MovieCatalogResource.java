package io.javabrains.moviecatalogservice.resources;

import io.javabrains.moviecatalogservice.models.CatalogItem;
import io.javabrains.moviecatalogservice.models.Movie;
import io.javabrains.moviecatalogservice.models.Rating;
import io.javabrains.moviecatalogservice.models.UserRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class MovieCatalogResource {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @RequestMapping("/")
    public String home(){
        return "index";
    }


    @RequestMapping("/catalog/{userId}")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId){
        /*List<CatalogItem> l = new ArrayList<CatalogItem>();
                l.add(new CatalogItem("Test", "Test",4)) ;
        return l;*/
        System.out.println(">>>orchestrating");
        //get all rated movie ids
 //       UserRating userRating = restTemplate.getForObject("http://ratings-data-service/ratingsdata/users/"+userId, UserRating.class);
        UserRating userRating = restTemplate.getForObject("http://localhost:8089/ratingsdata/users/"+userId, UserRating.class);
/*        UserRating userRating = webClientBuilder.build()
                .get()
                .uri("http://localhost:8089/ratingsdata/users/"+userId)
                .retrieve()
                .bodyToMono(UserRating.class)
                .block();*/
        if(userRating == null){
            System.out.println("failed to get user rating");
        }
        System.out.println("size of userrating "+userRating.getUserRating().size());

        return  userRating.getUserRating().stream().map(userrating -> {
            // for each movieid call movie info service and get details

//            Movie movie = restTemplate.getForObject("http://localhost:8091/movies/"+rating.getMovieId(), Movie.class);
            Movie movie = webClientBuilder.build()
                    .get()
                    .uri("http://movie-info-service/movies/"+userrating.getMovieId())
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();

            //put them all together
            return new CatalogItem(movie.getName(), "Test",userrating.getRating());
        }).collect(Collectors.toList());

    }
}
