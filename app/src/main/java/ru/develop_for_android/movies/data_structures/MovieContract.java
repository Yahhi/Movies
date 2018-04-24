package ru.develop_for_android.movies.data_structures;

import android.net.Uri;
import android.provider.BaseColumns;

import java.util.HashMap;

import ru.develop_for_android.movies.MoviesListLoader;

public class MovieContract {
    public static final String CONTENT_AUTHORITY = "ru.develop-for-android.movies";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";

    public static String getSelectonForSort(int sortType) {
        String selection;
        switch (sortType) {
            case MoviesListLoader.SORT_BY_POPULARITY:
                selection = MovieEntry.COLUMN_ORDER_IN_POPULAR_LIST + " > 0";
                break;
            case MoviesListLoader.SORT_BY_RATE:
                selection = MovieEntry.COLUMN_ORDER_IN_HIGHEST_LIST + " > 0";
                break;
            default:
                selection = MovieEntry.COLUMN_STARRED + " > 0";
        }
        return selection;
    }

    public static String getOrderForSort(int sortType) {
        String ordering;
        switch (sortType) {
            case MoviesListLoader.SORT_BY_POPULARITY:
                ordering = MovieEntry.COLUMN_ORDER_IN_POPULAR_LIST;
                break;
            case MoviesListLoader.SORT_BY_RATE:
                ordering = MovieEntry.COLUMN_ORDER_IN_HIGHEST_LIST;
                break;
            default:
                ordering = MovieEntry.COLUMN_TITLE;
        }
        return ordering;
    }



    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .build();
        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_ADULT = "adult";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_STARRED = "starred";
        public static final String COLUMN_ORDER_IN_POPULAR_LIST = "order_in_popular_list";
        public static final String COLUMN_ORDER_IN_HIGHEST_LIST = "order_in_highest_list";
    }

    public static final String[] columnsArray = new String[] {
            MovieEntry._ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieEntry.COLUMN_ORIGINAL_LANGUAGE,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_ADULT,
            MovieEntry.COLUMN_OVERVIEW,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_POPULARITY,
            MovieEntry.COLUMN_VOTE_COUNT,
            MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_STARRED,
            MovieEntry.COLUMN_ORDER_IN_POPULAR_LIST,
            MovieEntry.COLUMN_ORDER_IN_HIGHEST_LIST
    };

    public static final HashMap<String, Integer> columnsMap = createColumnsMap();

    private static HashMap<String, Integer> createColumnsMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(MovieEntry._ID, 0);
        map.put(MovieEntry.COLUMN_TITLE, 1);
        map.put(MovieEntry.COLUMN_ORIGINAL_TITLE, 2);
        map.put(MovieEntry.COLUMN_ORIGINAL_LANGUAGE, 3);
        map.put(MovieEntry.COLUMN_POSTER_PATH, 4);
        map.put(MovieEntry.COLUMN_ADULT, 5);
        map.put(MovieEntry.COLUMN_OVERVIEW, 6);
        map.put(MovieEntry.COLUMN_RELEASE_DATE, 7);
        map.put(MovieEntry.COLUMN_POPULARITY, 8);
        map.put(MovieEntry.COLUMN_VOTE_COUNT, 9);
        map.put(MovieEntry.COLUMN_VOTE_AVERAGE, 10);
        map.put(MovieEntry.COLUMN_STARRED, 11);
        map.put(MovieEntry.COLUMN_ORDER_IN_POPULAR_LIST, 12);
        map.put(MovieEntry.COLUMN_ORDER_IN_HIGHEST_LIST, 13);
        return map;
    }

    static final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
            MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MovieEntry.COLUMN_TITLE + " TEXT, " +
            MovieEntry.COLUMN_ADULT + " INTEGER DEFAULT 0, " +
            MovieEntry.COLUMN_OVERVIEW + " TEXT, " +
            MovieEntry.COLUMN_POSTER_PATH + " TEXT, " +
            MovieEntry.COLUMN_RELEASE_DATE + " INTEGER, " +
            MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT, " +
            MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT, " +
            MovieEntry.COLUMN_POPULARITY + " REAL, " +
            MovieEntry.COLUMN_VOTE_COUNT + " INTEGER, " +
            MovieEntry.COLUMN_VOTE_AVERAGE + " REAL, " +
            MovieEntry.COLUMN_STARRED + " INTEGER DEFAULT 0, " +
            MovieEntry.COLUMN_ORDER_IN_POPULAR_LIST + " INTEGER DEFAULT 0, " +
            MovieEntry.COLUMN_ORDER_IN_HIGHEST_LIST + " INTEGER DEFAULT 0)";
}
