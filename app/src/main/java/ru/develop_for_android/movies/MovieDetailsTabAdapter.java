package ru.develop_for_android.movies;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import ru.develop_for_android.movies.data_structures.Movie;
import ru.develop_for_android.movies.data_structures.Review;
import ru.develop_for_android.movies.data_structures.YoutubeVideo;

public class MovieDetailsTabAdapter extends FragmentPagerAdapter {
    private MovieDetailsFragment detailsFragment;
    private TrailersFragment trailersFragment;
    private ReviewsFragment reviewsFragment;

    MovieDetailsTabAdapter(FragmentManager fm, Movie movie) {
        super(fm);
        detailsFragment = MovieDetailsFragment.newInstance(movie);
        trailersFragment = new TrailersFragment();
        reviewsFragment = new ReviewsFragment();
    }

    public MovieDetailsTabAdapter(FragmentManager fm, Movie movie, YoutubeVideo[] trailersList,
                                  ArrayList<Review> reviews) {
        super(fm);
        detailsFragment = MovieDetailsFragment.newInstance(movie);
        trailersFragment = TrailersFragment.newInstance(trailersList);
        reviewsFragment = ReviewsFragment.newInstance(reviews);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                 return detailsFragment;
            case 1:
                return trailersFragment;
            case 2:
                return reviewsFragment;
        }
        return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Info";
            case 1:
                return "Trailers";
            case 2:
                return "Reviews";
        }
        return super.getPageTitle(position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    public void updateInfo(YoutubeVideo[] trailersList, ArrayList<Review> reviews) {
        trailersFragment.setTrailers(trailersList);
        reviewsFragment.setReviews(reviews);
    }
}
