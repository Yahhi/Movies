package ru.develop_for_android.movies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;


public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieViewHolder> {

    private JSONObject[] movieObjects;
    private MovieClick listener;

    MovieListAdapter(MovieClick listener) {
        this.movieObjects = new JSONObject[0];
        this.listener = listener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        try {
            Movie movie = new Movie(movieObjects[position]);
            holder.moviePoster.setContentDescription(movie.title);
            holder.movieId = movie.id;

            Context context = holder.moviePoster.getContext();
            Picasso.with(context).load(movie.getPosterPath()).into(holder.moviePoster);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return movieObjects.length;
    }

    void updateList(JSONObject[] movieObjects) {
        this.movieObjects = movieObjects;
        notifyDataSetChanged();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView moviePoster;
        int movieId;

        MovieViewHolder(View itemView) {
            super(itemView);
            moviePoster = itemView.findViewById(R.id.movie_poster);
            moviePoster.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onMovieClick(movieId);
        }
    }
}