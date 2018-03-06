package ru.develop_for_android.movies;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import ru.develop_for_android.movies.databinding.ActivityMovieDetailsBinding;

public class MovieDetailsActivity extends AppCompatActivity {

    public static final String ARGS_MOVIE = "movie";

    Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ActivityMovieDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);
        setSupportActionBar(binding.toolbar);

        Movie movie = getIntent().getParcelableExtra(ARGS_MOVIE);
        if (movie != null) {
            binding.setVariable(BR.movie, movie);
            setTitle(movie.title);
            Picasso.with(getBaseContext()).load(movie.getPosterPath()).into(binding.contentMovieDetails.detailsPoster);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void starMovie(View view) {
        Toast.makeText(getBaseContext(), "Replace with your own action", Toast.LENGTH_SHORT).show();

    }
}
