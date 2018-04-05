package ru.develop_for_android.movies;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Scanner;

public class MoviesDetailLoader<T> extends AsyncTaskLoader<JSONObject> {

    private static final String TAG = "NETWORK";

    private static final String protocol = "https";
    private static final String serverAddress = "api.themoviedb.org";

    private static final String PARAM_API_KEY = "api_key";
    private static final String PARAM_LANG = "language";
    private static final String PARAM_APPEND_TO_RESPONSE = "append_to_response";
    private static final String PARAM_INCLUDE_LANG = "include_image_language";

    private int movieId;

    MoviesDetailLoader(Context context, int movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public JSONObject loadInBackground() {
        String path = "3/movie/" + movieId;
        Uri uri = new Uri.Builder()
                .scheme(protocol)
                .authority(serverAddress)
                .path(path)
                .appendQueryParameter(PARAM_API_KEY, getContext().getString(R.string.mdb_key))
                .appendQueryParameter(PARAM_LANG, Locale.getDefault().getISO3Language().substring(0, 2))
                .appendQueryParameter(PARAM_INCLUDE_LANG, "en,null")
                .appendQueryParameter(PARAM_APPEND_TO_RESPONSE, "images,videos,reviews")
                .build();

        Log.i(TAG, "start loading url: " + uri.toString());
        JSONObject fullObject;
        try {
            String response = getResponseFromHttpUrl(new URL(uri.toString()));
            Log.i(TAG, response);
            try {
                fullObject = new JSONObject(response);
                Log.i("LOAD", "images: " + fullObject.getJSONObject("images").toString());
                Log.i("LOAD", "videos: " + fullObject.getJSONObject("videos").toString());
                Log.i("LOAD", "reviews: " + fullObject.getJSONObject("reviews").toString());
            } catch (JSONException e) {
                e.printStackTrace();
                fullObject = new JSONObject();
            }
        } catch (IOException e) {
            e.printStackTrace();
            fullObject = new JSONObject();
        }
        return fullObject;
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
