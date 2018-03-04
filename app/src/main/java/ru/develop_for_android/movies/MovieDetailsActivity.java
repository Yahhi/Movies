package ru.develop_for_android.movies;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailsActivity extends AppCompatActivity {

    public static final String ARGS_MOVIE = "movie";

    TextView descriptionView, ratingView, allVotesView,
            releaseDateView, originalTitleView, languageView;
    ImageView poster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        descriptionView = findViewById(R.id.details_overview);
        ratingView = findViewById(R.id.details_rate);
        allVotesView = findViewById(R.id.details_counts_number);
        releaseDateView = findViewById(R.id.details_release_date);
        originalTitleView = findViewById(R.id.details_original_title);
        languageView = findViewById(R.id.details_language);
        poster = findViewById(R.id.details_poster);

        Movie movie = getIntent().getParcelableExtra(ARGS_MOVIE);
        if (movie != null) {
            showMovieData(movie);
        }
    }

    private void showMovieData(Movie movie) {
        this.setTitle(movie.title);
        descriptionView.setText(movie.overview);
        ratingView.setText(String.valueOf(movie.voteAverage));
        allVotesView.setText(String.valueOf(movie.voteCount));
        releaseDateView.setText(getString(R.string.release_date, movie.releaseDate));
        originalTitleView.setText(getString(R.string.original_title, movie.originalTitle));
        languageView.setText(getString(R.string.language, movie.originalLanguage));
        Picasso.with(getBaseContext()).load(movie.getPosterPath()).into(poster);
    }
}
