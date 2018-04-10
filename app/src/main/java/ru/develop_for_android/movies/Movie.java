package ru.develop_for_android.movies;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Random;

public class Movie implements Parcelable{
    int id;
    private String posterPath;
    private boolean adult;
    public String overview;
    public String releaseDate;
    private String originalTitle;
    private String originalLanguage;
    public String title;
    private double popularity;
    private int voteCount;
    private double voteAverage;

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

    protected Movie(Parcel in) {
        id = in.readInt();
        posterPath = in.readString();
        adult = in.readByte() != 0;
        overview = in.readString();
        releaseDate = in.readString();
        originalTitle = in.readString();
        originalLanguage = in.readString();
        title = in.readString();
        popularity = in.readDouble();
        voteCount = in.readInt();
        voteAverage = in.readDouble();
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

    String getPosterPath() {
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
        parcel.writeString(releaseDate);
        parcel.writeString(originalTitle);
        parcel.writeString(originalLanguage);
        parcel.writeString(title);
        parcel.writeDouble(popularity);
        parcel.writeInt(voteCount);
        parcel.writeDouble(voteAverage);
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
            if (config.getBoolean(MainActivity.RANDOM_KEY)) {
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
}
