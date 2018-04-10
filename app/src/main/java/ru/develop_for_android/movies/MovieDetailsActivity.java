package ru.develop_for_android.movies;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.develop_for_android.movies.databinding.ActivityMovieDetailsBinding;

public class MovieDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<JSONObject> {

    public static final String ARGS_MOVIE = "movie";

    ActivityMovieDetailsBinding binding;

    private Movie movie;
    private String backdropPath;
    private YoutubeVideo[] trailersList;
    private ArrayList<Review> reviews;

    private MovieDetailsTabAdapter tabAdapter;

    private final int MOVIE_LOADER_ID = 301;
    private final String KEY_MOVIE_ID = "movieId";

    private final String KEY_BACKDROP_PATH = "backdrop";
    private final String KEY_TRAILERS = "trailers";
    private final String KEY_REVIEWS = "reviews";

    private static final String KEY_MOVIE = "movie";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);
        setSupportActionBar(binding.toolbar);
        reviews = new ArrayList<>();

        if (savedInstanceState == null) {
            movie = getIntent().getParcelableExtra(ARGS_MOVIE);
            if (movie != null) {
                loadMovieData(movie.id);
            }
        } else {
            movie = savedInstanceState.getParcelable(KEY_MOVIE);
            if (savedInstanceState.containsKey(KEY_BACKDROP_PATH)) {
                backdropPath = savedInstanceState.getString(KEY_BACKDROP_PATH);
                Picasso.with(getBaseContext()).load(backdropPath).into(binding.detailsBackdrop);
            }
        }
        tabAdapter = new MovieDetailsTabAdapter(getSupportFragmentManager(), movie);
        ViewPager viewPager = binding.contentMovieDetails.tabsView;
        viewPager.setAdapter(tabAdapter);
        setTitle(movie.title);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_MOVIE, movie);
        if (backdropPath != null) {
            outState.putString(KEY_BACKDROP_PATH, backdropPath);
        }
        if (trailersList != null) {
            outState.putParcelableArray(KEY_TRAILERS, trailersList);
        }
        if (reviews != null) {
            outState.putParcelableArrayList(KEY_REVIEWS, reviews);
        }
        super.onSaveInstanceState(outState);
    }

    private void loadMovieData(int movieId) {
        LoaderManager loaderManager = getSupportLoaderManager();
        android.support.v4.content.Loader<JSONObject> moviesLoader = loaderManager.getLoader(MOVIE_LOADER_ID);
        Bundle loadingBundle = new Bundle();
        loadingBundle.putInt(KEY_MOVIE_ID, movieId);
        if (moviesLoader == null) {
            loaderManager.initLoader(MOVIE_LOADER_ID, loadingBundle, this);
        } else {
            loaderManager.restartLoader(MOVIE_LOADER_ID, loadingBundle, this);
        }
    }

    public void starMovie(View view) {
        Toast.makeText(getBaseContext(), "Replace with your own action", Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        int movieId = args.getInt(KEY_MOVIE_ID);
        return new MoviesDetailLoader<>(this, movieId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {
        backdropPath = Movie.getBackdropPath(data);
        Log.i("PICASSO", backdropPath);
        Picasso.with(getBaseContext()).load(backdropPath).into(binding.detailsBackdrop);

        try {
            trailersList = Movie.getTrailersList(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray reviewsJson = Movie.getReviewsObject(data);
            for (int i = 0; i < reviewsJson.length(); i++) {
                reviews.add(new Review(reviewsJson.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        tabAdapter.updateInfo(trailersList, reviews);
    }
    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }
}
