package io.javabrains.movieinfoservice.resources;

import io.javabrains.movieinfoservice.models.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@RequestMapping("/movies")
public class MovieResource {

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

    @RequestMapping("/{movieId}")
    public Movie getMovieInfo(@PathVariable ("movieId") String movieId){
        return new Movie(movieId, movieId+" --- being serving from "+ip+" port"+serverPort);
    }
}
