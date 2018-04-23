package ru.develop_for_android.movies;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import ru.develop_for_android.movies.data_structures.Movie;


public class MovieLocalListAdapter extends RecyclerView.Adapter<MovieLocalListAdapter.MovieViewHolder> {

    private Cursor movies;
    private MovieClick listener;

    MovieLocalListAdapter(MovieClick listener, Cursor cursor) {
        this.movies = cursor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        movies.moveToPosition(position);
        Movie movie = Movie.loadMovieFromCursor(movies);

        holder.moviePoster.setContentDescription(movie.title);
        holder.movie = movie;

        Context context = holder.moviePoster.getContext();
        Picasso.with(context).load(movie.getPosterPath()).into(holder.moviePoster);
    }

    @Override
    public int getItemCount() {
        return movies.getCount();
    }

    void updateList(Cursor cursor) {
        if (movies != null) {
            movies.close();
        }
        this.movies = cursor;
        notifyDataSetChanged();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView moviePoster;
        Movie movie;

        MovieViewHolder(View itemView) {
            super(itemView);
            moviePoster = itemView.findViewById(R.id.movie_poster);
            moviePoster.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onMovieClick(movie);
        }
    }
}
