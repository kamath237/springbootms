package io.javabrains.movieinfoservice.resources;

import io.javabrains.movieinfoservice.models.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@RequestMapping("/")
public class MovieResource {
    private static Logger log = LoggerFactory.getLogger(MovieResource.class);


    @Value("${server.port}")
    private int serverPort;

    String ip;

    {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/")
    public String home(){
        return "index";
    }

    @RequestMapping("/movies/{movieId}")
    public Movie getMovieInfo(@PathVariable ("movieId") String movieId, @RequestHeader("authorization") String authzToken){

        log.info("authorization :"+authzToken);
        return new Movie(movieId, movieId+" --- being serving from "+ip+" port"+serverPort);
    }
}
