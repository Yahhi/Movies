package ru.develop_for_android.movies;

import org.json.JSONException;
import org.json.JSONObject;

public class Movie {
    int id;
    private String posterPath;
    boolean adult;
    String overview;
    String releaseDate;
    String originalTitle;
    String originalLanguage;
    String title;
    double popularity;
    int voteCount;
    double voteAverage;

    private final static String baseUrl = "http://image.tmdb.org/t/p/";
    private final static String posterUrlPart = "w500/";

    private static final String PARAM_POSTER_PATH = "poster_path";
    private static final String PARAM_ADULT = "adult";
    private static final String PARAM_OVERVIEW = "overview";
    private static final String PARAM_RELEASE_DATE = "release_date";
    private static final String PARAM_ID = "id";
    private static final String PARAM_ORIGINAL_TITLE = "original_title";
    private static final String PARAM_ORIGINAL_LANGUAGE = "original_language";
    private static final String PARAM_TITLE = "title";
    private static final String PARAM_POPULARITY = "popularity";
    private static final String PARAM_VOTE_COUNT = "vote_count";
    private static final String PARAM_VOTE_AVERAGE = "vote_average";

    Movie(JSONObject object) throws JSONException {
        id = object.getInt(PARAM_ID);
        posterPath = object.optString(PARAM_POSTER_PATH, "");
        adult = object.getBoolean(PARAM_ADULT);
        overview = object.getString(PARAM_OVERVIEW);
        releaseDate = object.getString(PARAM_RELEASE_DATE);
        originalTitle = object.getString(PARAM_ORIGINAL_TITLE);
        originalLanguage = object.getString(PARAM_ORIGINAL_LANGUAGE);
        title = object.getString(PARAM_TITLE);
        popularity = object.getDouble(PARAM_POPULARITY);
        voteCount = object.getInt(PARAM_VOTE_COUNT);
        voteAverage = object.getDouble(PARAM_VOTE_AVERAGE);
    }

    String getPosterPath() {
        return baseUrl + posterUrlPart + posterPath;
    }
}
