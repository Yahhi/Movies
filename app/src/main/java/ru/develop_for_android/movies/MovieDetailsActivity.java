package ru.develop_for_android.movies;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.develop_for_android.movies.databinding.ActivityMovieDetailsBinding;

public class MovieDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {

    public static final String ARGS_MOVIE = "movie";

    ActivityMovieDetailsBinding binding;
    Movie movie;
    private final int MOVIE_LOADER_ID = 301;
    private final String KEY_MOVIE_ID = "movieId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);
        setSupportActionBar(binding.toolbar);

        movie = getIntent().getParcelableExtra(ARGS_MOVIE);
        if (movie != null) {
            binding.setVariable(BR.movie, movie);
            setTitle(movie.title);
            //Picasso.with(getBaseContext()).load(movie.getPosterPath()).into(binding.contentMovieDetails.detailsPoster);
            loadMovieData(movie.id);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        int movieId = args.getInt(KEY_MOVIE_ID);
        return new MoviesDetailLoader<>(this, movieId);
    }

    @Override
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
        String backdropPath = Movie.getBackdropPath(data);
        Log.i("PICASSO", backdropPath);
        Picasso.with(getBaseContext()).load(backdropPath).into(binding.detailsBackdrop);

        try {
            JSONObject videos = data.getJSONObject("videos");
            JSONArray trailersJson = videos.getJSONArray("results");
            if (trailersJson != null && trailersJson.length() > 0) {
                ArrayList<YoutubeVideo> trailersList = new ArrayList<>();
                for (int i = 0; i < trailersJson.length(); i++) {
                    JSONObject trailerJson = trailersJson.getJSONObject(i);
                    trailersList.add(new YoutubeVideo(trailerJson));
                }
                TrailerAdapter adapter = new TrailerAdapter(trailersList);
                RecyclerView recyclerView = binding.contentMovieDetails.trailersList;
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {

    }
}
