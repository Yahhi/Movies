package ru.develop_for_android.movies;

import android.content.Context;
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

import timber.log.Timber;

public class MoviesListLoader<T> extends AsyncTaskLoader<JSONObject[]> {

    static final int SORT_BY_POPULARITY = 1;
    static final int SORT_BY_RATE = 2;

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
    public JSONObject[] loadInBackground() {
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
        JSONObject[] movieObjects;
        try {
            String response = getResponseFromHttpUrl(new URL(uri.toString()));
            try {
                JSONObject fullObject = new JSONObject(response);
                JSONArray results = fullObject.getJSONArray(PARAM_RESULTS);
                movieObjects = new JSONObject[results.length()];
                for (int i = 0; i < movieObjects.length; i++) {
                    movieObjects[i] = results.getJSONObject(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                movieObjects = new JSONObject[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
            movieObjects = new JSONObject[0];
        }
        return movieObjects;
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
