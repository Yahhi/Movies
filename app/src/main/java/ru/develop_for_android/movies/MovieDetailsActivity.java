package ru.develop_for_android.movies;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.develop_for_android.movies.data_structures.Movie;
import ru.develop_for_android.movies.data_structures.Review;
import ru.develop_for_android.movies.data_structures.YoutubeVideo;
import ru.develop_for_android.movies.databinding.ActivityMovieDetailsBinding;
import timber.log.Timber;

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
    private boolean wasLoaded = false;
    private final String KEY_MOVIE_ID = "movieId";

    private final String KEY_BACKDROP_PATH = "backdrop";
    private final String KEY_TRAILERS = "trailers";
    private final String KEY_REVIEWS = "reviews";

    private static final String KEY_MOVIE = "movie";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.i("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);
        setSupportActionBar(binding.toolbar);
        reviews = new ArrayList<>();

        if (savedInstanceState == null) {
            Timber.i( "instance state null");
            movie = getIntent().getParcelableExtra(ARGS_MOVIE);
            if (movie != null) {
                loadMovieData(movie.id);
            }
        } else {
            Timber.i("loading state");
            movie = savedInstanceState.getParcelable(KEY_MOVIE);
            if (savedInstanceState.containsKey(KEY_BACKDROP_PATH)) {
                backdropPath = savedInstanceState.getString(KEY_BACKDROP_PATH);
                Picasso.with(getBaseContext()).load(backdropPath).into(binding.detailsBackdrop);
            }
        }
        tabAdapter = new MovieDetailsTabAdapter(getSupportFragmentManager(), movie);
        ViewPager viewPager = binding.contentMovieDetails.tabsView;
        viewPager.setAdapter(tabAdapter);
        this.setTitle(movie.title);

        if (movie.starred) {
            makeFabAccented();
        } else {
            makeFabCommon();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void makeFabAccented() {
        binding.fab.setImageResource(R.drawable.ic_star_accented);
    }

    private void makeFabCommon() {
        binding.fab.setImageResource(R.drawable.ic_star);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_MOVIE, movie);
        if (backdropPath != null) {
            outState.putString(KEY_BACKDROP_PATH, backdropPath);
        }
        super.onSaveInstanceState(outState);
    }

    private void loadMovieData(int movieId) {
        Timber.i("start loading info");
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
        if (movie.starred) {
            makeFabCommon();
            movie.saveUnstarredMovie(getBaseContext());
        } else {
            makeFabAccented();
            movie.saveStarredMovie(getBaseContext());
        }
    }

    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        int movieId = args.getInt(KEY_MOVIE_ID);
        return new MoviesDetailLoader<>(this, movieId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {
        if (!wasLoaded) {
            wasLoaded = true;
            backdropPath = Movie.getBackdropPath(data);
            Timber.i("PICASSO %s", backdropPath);
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
    }
    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }


}
