package ru.develop_for_android.movies.data_structures;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import ru.develop_for_android.movies.MoviesListLoader;

import static ru.develop_for_android.movies.data_structures.MovieContract.MovieEntry;
import static ru.develop_for_android.movies.data_structures.MovieContract.columnsArray;
import static ru.develop_for_android.movies.data_structures.MovieContract.columnsMap;

public class Movie implements Parcelable{
    public int id;
    private String posterPath;
    private boolean adult;
    public String overview;
    private Date releaseDate;
    private String originalTitle;
    private String originalLanguage;
    public String title;
    private double popularity;
    private int voteCount;
    private double voteAverage;
    public boolean starred;

    static final String RANDOM_KEY = "random_backdrop";

    private final static String baseUrl = "http://image.tmdb.org/t/p/";
    private final static String posterUrlPart = "w500";
    private final static String backdropUrlPart = "w780";

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

    private static final String PARAM_IMAGES = "images";
    private static final String PARAM_BACKDROPS = "backdrops";
    private static final String PARAM_IMAGE_PATH = "file_path";

    private static final String PARAM_VIDEOS = "videos";
    private static final String PARAM_REVIEWS = "reviews";
    private static final String PARAM_RESULTS = "results";

    public Movie(JSONObject object) throws JSONException {
        id = object.getInt(PARAM_ID);
        posterPath = object.optString(PARAM_POSTER_PATH, "");
        adult = object.getBoolean(PARAM_ADULT);
        overview = object.getString(PARAM_OVERVIEW);
        releaseDate = convertDate(object.getString(PARAM_RELEASE_DATE));
        originalTitle = object.getString(PARAM_ORIGINAL_TITLE);
        originalLanguage = object.getString(PARAM_ORIGINAL_LANGUAGE);
        title = object.getString(PARAM_TITLE);
        popularity = object.getDouble(PARAM_POPULARITY);
        voteCount = object.getInt(PARAM_VOTE_COUNT);
        voteAverage = object.getDouble(PARAM_VOTE_AVERAGE);
    }

    private Movie() {

    }

    private Date convertDate(String string) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            return format.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getReleaseDateString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return format.format(releaseDate);
    }

    public void saveStarredMovie(Context context) {
        starred = true;
        ContentValues values = getContentValues();
        values.put(MovieEntry.COLUMN_STARRED, 1);
        saveMovie(context, values);
    }

    public void saveUnstarredMovie(Context context) {
        starred = false;
        ContentValues values = getContentValues();
        values.put(MovieEntry.COLUMN_STARRED, 0);
        saveMovie(context, values);
    }

    public void saveMovieInPopularList(Context context, int positionInList) {
        ContentValues values = getContentValues();
        values.put(MovieEntry.COLUMN_ORDER_IN_POPULAR_LIST, positionInList);
        saveMovie(context, values);
    }

    public void saveMovieInHighestList(Context context, int positionInList) {
        ContentValues values = getContentValues();
        values.put(MovieEntry.COLUMN_ORDER_IN_HIGHEST_LIST, positionInList);
        saveMovie(context, values);

    }

    private void saveMovie(Context context, ContentValues values) {
        SQLiteDatabase database = DBHelper.getHelper(context).getWritableDatabase();
        if (movieAlreadySaved(database)) {
            database.update(MovieEntry.TABLE_NAME, values, MovieEntry._ID + "=?",
                    new String[]{String.valueOf(id)});
        } else {
            values.put(MovieEntry._ID, id);
            database.insert(MovieEntry.TABLE_NAME, "", values);
        }
    }

    private ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(MovieEntry.COLUMN_TITLE, title);
        values.put(MovieEntry.COLUMN_ADULT, adult? 1:0);
        values.put(MovieEntry.COLUMN_ORIGINAL_LANGUAGE, originalLanguage);
        values.put(MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
        values.put(MovieEntry.COLUMN_OVERVIEW, overview);
        values.put(MovieEntry.COLUMN_POPULARITY, popularity);
        values.put(MovieEntry.COLUMN_POSTER_PATH, posterPath);
        values.put(MovieEntry.COLUMN_RELEASE_DATE, releaseDate.getTime());
        values.put(MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
        values.put(MovieEntry.COLUMN_VOTE_COUNT, voteCount);
        return values;
    }

    private boolean movieAlreadySaved(SQLiteDatabase database) {
        Cursor cursor = database.query(MovieEntry.TABLE_NAME, columnsArray,
                MovieEntry._ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    public static Movie loadMovieFromCursor(Cursor cursor) {
        Movie movie = new Movie();
        movie.id = cursor.getInt(columnsMap.get(MovieEntry._ID));
        movie.title = cursor.getString(columnsMap.get(MovieEntry.COLUMN_TITLE));
        movie.adult = cursor.getInt(columnsMap.get(MovieEntry.COLUMN_ADULT)) > 0;
        movie.originalLanguage = cursor.getString(columnsMap.get(MovieEntry.COLUMN_ORIGINAL_LANGUAGE));
        movie.originalTitle = cursor.getString(columnsMap.get(MovieEntry.COLUMN_ORIGINAL_TITLE));
        movie.overview = cursor.getString(columnsMap.get(MovieEntry.COLUMN_OVERVIEW));
        movie.popularity = cursor.getDouble(columnsMap.get(MovieEntry.COLUMN_POPULARITY));
        movie.posterPath = cursor.getString(columnsMap.get(MovieEntry.COLUMN_POSTER_PATH));
        movie.releaseDate = new Date(cursor.getLong(columnsMap.get(MovieEntry.COLUMN_RELEASE_DATE)));
        movie.voteAverage = cursor.getDouble(columnsMap.get(MovieEntry.COLUMN_VOTE_AVERAGE));
        movie.voteCount = cursor.getInt(columnsMap.get(MovieEntry.COLUMN_VOTE_COUNT));
        movie.starred = cursor.getInt(columnsMap.get(MovieEntry.COLUMN_STARRED)) > 0;
        return movie;
    }

    protected Movie(Parcel in) {
        id = in.readInt();
        posterPath = in.readString();
        adult = in.readByte() != 0;
        overview = in.readString();
        releaseDate = new Date(in.readLong());
        originalTitle = in.readString();
        originalLanguage = in.readString();
        title = in.readString();
        popularity = in.readDouble();
        voteCount = in.readInt();
        voteAverage = in.readDouble();
        starred = in.readByte() != 0;
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getPosterPath() {
        return baseUrl + posterUrlPart + posterPath;
    }

    public String getOriginalLanguageFullName() {
        Locale locale = new Locale(originalLanguage);
        return originalTitle + " (" + locale.getDisplayLanguage() + ")";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(posterPath);
        parcel.writeByte((byte) (adult ? 1 : 0));
        parcel.writeString(overview);
        parcel.writeLong(releaseDate.getTime());
        parcel.writeString(originalTitle);
        parcel.writeString(originalLanguage);
        parcel.writeString(title);
        parcel.writeDouble(popularity);
        parcel.writeInt(voteCount);
        parcel.writeDouble(voteAverage);
        parcel.writeByte((byte) (starred ? 1:0));
    }

    public String getParamVoteAverage() {
        return String.valueOf(voteAverage);
    }

    public String getParamVoteCount() {
        return String.valueOf(voteCount);
    }

    public static String getBackdropPath(JSONObject object) {
        String path;
        try {
            JSONObject images = object.getJSONObject(PARAM_IMAGES);
            JSONArray backdrops = images.getJSONArray(PARAM_BACKDROPS);

            FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
            int pictureIndex;
            if (config.getBoolean(RANDOM_KEY)) {
                Random random = new Random();
                pictureIndex = random.nextInt(backdrops.length() - 1);
            } else {
                pictureIndex = 0;
            }
            JSONObject backdrop = backdrops.optJSONObject(pictureIndex);
            path = backdrop.optString(PARAM_IMAGE_PATH, "");
        } catch (JSONException e) {
            e.printStackTrace();
            path = "";
        }
        return path.equals("")?"":(baseUrl + backdropUrlPart + path);
    }

    public static YoutubeVideo[] getTrailersList(JSONObject data) throws JSONException {
        JSONObject videos = data.getJSONObject(PARAM_VIDEOS);
        JSONArray trailersJson = videos.getJSONArray(PARAM_RESULTS);
        YoutubeVideo[] trailersList = new YoutubeVideo[trailersJson.length()];
        for (int i = 0; i < trailersJson.length(); i++) {
            JSONObject trailerJson = trailersJson.getJSONObject(i);
            trailersList[i] = new YoutubeVideo(trailerJson);
        }
        return trailersList;
    }

    public static JSONArray getReviewsObject(JSONObject data) throws JSONException {
        JSONObject fullObject = data.getJSONObject(PARAM_REVIEWS);
        return fullObject.getJSONArray(PARAM_RESULTS);
    }

    public static void clearPopularList(Context context) {
        SQLiteDatabase database = DBHelper.getHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MovieEntry.COLUMN_ORDER_IN_POPULAR_LIST, 0);
        database.update(MovieEntry.TABLE_NAME, values,
                MovieEntry.COLUMN_ORDER_IN_POPULAR_LIST + " > 0", null);
    }

    public static void clearHighestList(Context context) {
        SQLiteDatabase database = DBHelper.getHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MovieEntry.COLUMN_ORDER_IN_HIGHEST_LIST, 0);
        database.update(MovieEntry.TABLE_NAME, values,
                MovieEntry.COLUMN_ORDER_IN_HIGHEST_LIST + " > 0", null);

    }

    public static Cursor getMovieListByType(Context context, int sortType) {
        SQLiteDatabase database = DBHelper.getHelper(context).getReadableDatabase();
        String selection, ordering;
        switch (sortType) {
            case MoviesListLoader.SORT_BY_POPULARITY:
                selection = MovieEntry.COLUMN_ORDER_IN_POPULAR_LIST + " > 0";
                ordering = MovieEntry.COLUMN_ORDER_IN_POPULAR_LIST;
                break;
            case MoviesListLoader.SORT_BY_RATE:
                selection = MovieEntry.COLUMN_ORDER_IN_HIGHEST_LIST + " > 0";
                ordering = MovieEntry.COLUMN_ORDER_IN_HIGHEST_LIST;
                break;
            default:
                selection = MovieEntry.COLUMN_STARRED + " > 0";
                ordering = MovieEntry.COLUMN_TITLE;
        }
        return database.query(MovieEntry.TABLE_NAME, MovieContract.columnsArray, selection,
                null, null, null, ordering);
    }
}
