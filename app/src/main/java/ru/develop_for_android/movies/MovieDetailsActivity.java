package ru.develop_for_android.movies;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import ru.develop_for_android.movies.databinding.ActivityMovieDetailsBinding;

public class MovieDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<JSONObject>, TrailerPresenter {

    public static final String ARGS_MOVIE = "movie";

    ActivityMovieDetailsBinding binding;

    private Movie movie;
    private String backdropPath;
    private YoutubeVideo[] trailersList;

    private final int MOVIE_LOADER_ID = 301;
    private final String KEY_MOVIE_ID = "movieId";
    private final String KEY_BACKDROP_PATH = "backdrop";
    private final String KEY_TRAILERS = "trailers";

    private static final String KEY_MOVIE = "movie";

    private static final int REQUEST_VIDEO_PLAYBACK = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);
        setSupportActionBar(binding.toolbar);

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
            if (savedInstanceState.containsKey(KEY_TRAILERS)) {
                trailersList = (YoutubeVideo[]) savedInstanceState.getParcelableArray(KEY_TRAILERS);
                setAdapter();
            }
        }
        showMovieData();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showMovieData() {
        binding.setMovie(movie);
        setTitle(movie.title);
        Picasso.with(getBaseContext()).load(movie.getPosterPath()).into(binding.contentMovieDetails.detailsPoster);
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
            if (trailersList.length > 0) {
                setAdapter();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setAdapter() {
        TrailerAdapter adapter = new TrailerAdapter(trailersList, this);
        RecyclerView recyclerView = binding.contentMovieDetails.trailersList;
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
    }

    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }

    @Override
    public void onTrailerClick(String key) {
        Log.i("VIDEO", "starting with key " + key);
        Intent videoClient = new Intent(Intent.ACTION_VIEW);
        videoClient.setData(Uri.parse("http://m.youtube.com/watch?v=" + key));
        videoClient.putExtra("finish_on_ended", true);
        //videoClient.putExtra("force_fullscreen", true);
        startActivityForResult(videoClient, REQUEST_VIDEO_PLAYBACK);
    }
}
