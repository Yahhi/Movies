package ru.develop_for_android.movies;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import ru.develop_for_android.movies.data_structures.Movie;
import ru.develop_for_android.movies.data_structures.MovieContract;
import timber.log.Timber;

public class MoviesListLoader extends IntentService {

    public static final int SORT_BY_POPULARITY = 1;
    public static final int SORT_BY_RATE = 2;
    public static final int SORT_STARRED = 3;

    private static final String protocol = "https";
    private static final String serverAddress = "api.themoviedb.org";

    private static final String PARAM_API_KEY = "api_key";
    private static final String PARAM_LANG = "language";
    private static final String PARAM_REGION = "region";
    private static final String PARAM_PAGE = "page";

    private static final String PARAM_RESULTS = "results";

    public static final String KEY_SORT = "sort";
    public static final String KEY_PAGE = "page";
    static final String KEY_LAST_HIGHEST_UPDATE_TIME = "last_highest_update_time";
    static final String KEY_LAST_POPULAR_UPDATE_TIME = "last_popular_update_time";
    static final String KEY_LAST_HIGHEST_UPDATED_PAGE = "highest_last_page";
    static final String KEY_LAST_POPULAR_UPDATED_PAGE = "popular_last_page";

    public MoviesListLoader() {
        super("listLoader");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            updateMoviesList(SORT_BY_POPULARITY, 1);
            updateMoviesList(SORT_BY_RATE, 1);
        } else {
            int sortType = intent.getIntExtra(KEY_SORT, SORT_BY_POPULARITY);
            int page = intent.getIntExtra(KEY_PAGE, 1);
            updateMoviesList(sortType, page);
        }
    }

    public void updateMoviesList(int sortType, int page) {
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
                .appendQueryParameter(PARAM_API_KEY, getString(R.string.mdb_key))
                .appendQueryParameter(PARAM_LANG, Locale.getDefault().getISO3Language().substring(0, 2))
                .appendQueryParameter(PARAM_REGION, Locale.getDefault().getISO3Country().substring(0, 2))
                .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                .build();

        Timber.i("start loading url: %s", uri.toString());
        try {
            ContentValues values = new ContentValues();
            String whereString;
            int firstLoadedPosition = (page - 1) * 20 + 1;
            if (sortType == SORT_BY_POPULARITY) {
                values.put(MovieContract.MovieEntry.COLUMN_ORDER_IN_POPULAR_LIST, 0);
                whereString = MovieContract.MovieEntry.COLUMN_ORDER_IN_POPULAR_LIST + " >= "
                        + firstLoadedPosition;
            } else {
                values.put(MovieContract.MovieEntry.COLUMN_ORDER_IN_HIGHEST_LIST, 0);
                whereString = MovieContract.MovieEntry.COLUMN_ORDER_IN_HIGHEST_LIST + " >= "
                        + firstLoadedPosition;
            }
            getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI, values,
                    whereString, null);

            String response = getResponseFromHttpUrl(new URL(uri.toString()));
            try {
                JSONObject fullObject = new JSONObject(response);
                JSONArray results = fullObject.getJSONArray(PARAM_RESULTS);
                ContentValues[] valuesToInsert = new ContentValues[results.length()];
                for (int i = 0; i < results.length(); i++) {
                    JSONObject movieObject = results.getJSONObject(i);
                    Movie movie = new Movie(movieObject);
                    valuesToInsert[i] = movie.getContentValues();
                    int positionInList = firstLoadedPosition + i;
                    if (sortType == SORT_BY_POPULARITY) {
                        valuesToInsert[i].put(MovieContract.MovieEntry.COLUMN_ORDER_IN_POPULAR_LIST,
                                positionInList);
                    } else {
                        valuesToInsert[i].put(MovieContract.MovieEntry.COLUMN_ORDER_IN_HIGHEST_LIST,
                                positionInList);
                    }
                }
                int result = getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, valuesToInsert);

                SharedPreferences.Editor preferenceEditor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
                if (sortType == SORT_BY_POPULARITY) {
                    if (page == 1) {
                        preferenceEditor.putLong(KEY_LAST_POPULAR_UPDATE_TIME, new Date().getTime());
                    }
                    preferenceEditor.putInt(KEY_LAST_POPULAR_UPDATED_PAGE, page);
                } else {
                    if (page == 1) {
                        preferenceEditor.putLong(KEY_LAST_HIGHEST_UPDATE_TIME, new Date().getTime());
                    }
                    preferenceEditor.putInt(KEY_LAST_POPULAR_UPDATED_PAGE, page);
                }
                preferenceEditor.apply();

                Timber.i("loading finished with %d inserted or updated rows", result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
