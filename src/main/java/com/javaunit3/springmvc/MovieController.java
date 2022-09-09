package com.javaunit3.springmvc;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
public class MovieController {

    @Autowired
    private SessionFactory sessionFactory;

    @RequestMapping("/")
    public String getIndexPage() {

        return "index";
    }

    @RequestMapping("/bestMovie")
    public String getBestMoviePage(Model model) {

        //model.addAttribute("BestMovie", bestMovieService.getBestMovie().getTitle());
        Session dbSession = sessionFactory.getCurrentSession();;
        dbSession.beginTransaction();
        List<MovieEntity> movieList = dbSession.createQuery("FROM MovieEntity").list();
        movieList.sort(Comparator.comparing(movieEntity -> movieEntity.getVotes().size()));

        Integer movieListSize = movieList.size() - 1;

        if (movieList != null && !movieList.isEmpty()) {
            // sort movie list
            // https://www.codebyamir.com/blog/sort-list-of-objects-by-field-java
            MovieEntity movie = (MovieEntity) movieList.get(movieListSize);
            model.addAttribute("bestMovie", movie.getTitle());

            //get list of voters
            List<String> bestVoters = new ArrayList<>();

            for (VoteEntity vote: movie.getVotes()) {
                bestVoters.add(vote.getVoterName());
            }
            model.addAttribute("bestVoters", String.join(", ", bestVoters));

        } else {
            model.addAttribute("bestMovie", "::No Movie Is Available!!!");
            model.addAttribute("bestVoters", "::No Voters Are Available!!!");
        }

        dbSession.getTransaction().commit();

        return "bestMovie";
    }

    @RequestMapping("/voteForBestMovieForm")
    public String voteForBestMovieFormPage(Model model) {

        Session dbSession = sessionFactory.getCurrentSession();
        dbSession.beginTransaction();
        List<MovieEntity> movieList = dbSession.createQuery("FROM MovieEntity").list();
        dbSession.getTransaction().commit();

        model.addAttribute("movies", movieList);

        return "voteForBestMovie";
    }

    @RequestMapping("/voteForBestMovie")
    public String voteForBestMovie(HttpServletRequest request) {

        String voterName = request.getParameter("voterName");
        String movieId = request.getParameter("movieId");

        Session dbSession = sessionFactory.getCurrentSession();
        dbSession.beginTransaction();

        MovieEntity movieEntity = (MovieEntity) dbSession.get(MovieEntity.class, Integer.parseInt(movieId));
        VoteEntity newVote = new VoteEntity();
        newVote.setVoterName(voterName);
        movieEntity.addVote(newVote);

        dbSession.update(movieEntity);
        dbSession.getTransaction().commit();

        return "voteForBestMovie";
    }

    @RequestMapping("/addMovieForm")
    public String addMovieFormPage() {

        return "addMovie";
    }

    @RequestMapping("/addMovie")
    public String addMovie(HttpServletRequest request) {

        String movieTitle = request.getParameter("movieTitle");
        String genre = request.getParameter("genre");
        String maturityRating = request.getParameter("maturityRating");

        MovieEntity movieEntity = new MovieEntity();
        movieEntity.setTitle(movieTitle);
        movieEntity.setMaturityRating(maturityRating);
        movieEntity.setGenre(genre);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        session.save(movieEntity);
        session.getTransaction().commit();

        return "addMovie";
    }

}
