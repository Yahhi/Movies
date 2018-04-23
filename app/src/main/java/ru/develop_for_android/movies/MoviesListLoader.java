package ru.develop_for_android.movies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Scanner;

import ru.develop_for_android.movies.data_structures.Movie;
import timber.log.Timber;

public class MoviesListLoader<T> extends AsyncTaskLoader<Cursor> {

    public static final int SORT_BY_POPULARITY = 1;
    public static final int SORT_BY_RATE = 2;
    public static final int SORT_STARRED = 3;

    private static final String TAG = "NETWORK";

    private static final String protocol = "https";
    private static final String serverAddress = "api.themoviedb.org";

    private static final String PARAM_API_KEY = "api_key";
    private static final String PARAM_LANG = "language";
    private static final String PARAM_REGION = "region";

    private static final String PARAM_RESULTS = "results";

    private int sortType;

    MoviesListLoader(Context context, int sortType) {
        super(context);
        this.sortType = sortType;
        Timber.i("class instantiated");
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Cursor loadInBackground() {
        String path;
        if (sortType == SORT_BY_POPULARITY) {
            path = "3/movie/popular";
        } else {
            path = "3/movie/top_rated";
        }
        Uri uri = new Uri.Builder()
                .scheme(protocol)
                .authority(serverAddress)
                .path(path)
                .appendQueryParameter(PARAM_API_KEY, getContext().getString(R.string.mdb_key))
                .appendQueryParameter(PARAM_LANG, Locale.getDefault().getISO3Language().substring(0, 2))
                .appendQueryParameter(PARAM_REGION, Locale.getDefault().getISO3Country().substring(0, 2))
                .build();

        Timber.i("start loading url: %s", uri.toString());
        try {
            String response = getResponseFromHttpUrl(new URL(uri.toString()));
            if (sortType == SORT_BY_POPULARITY) {
                Movie.clearPopularList(getContext());
            } else {
                Movie.clearHighestList(getContext());
            }
            try {
                JSONObject fullObject = new JSONObject(response);
                JSONArray results = fullObject.getJSONArray(PARAM_RESULTS);
                for (int i = 0; i < results.length(); i++) {
                    JSONObject movieObject = results.getJSONObject(i);
                    Movie movie = new Movie(movieObject);
                    if (sortType == SORT_BY_POPULARITY) {
                        movie.saveMovieInPopularList(getContext(), i + 1);
                    } else {
                        movie.saveMovieInHighestList(getContext(), i + 1);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Movie.getMovieListByType(getContext(), sortType);
    }

    private String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
