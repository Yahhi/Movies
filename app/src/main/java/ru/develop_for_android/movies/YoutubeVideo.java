package ru.develop_for_android.movies;

import org.json.JSONException;
import org.json.JSONObject;

public class YoutubeVideo {
    String id;
    String language;
    String name;
    private String key;

    private static final String PARAM_ID = "id";
    private static final String PARAM_LANG = "iso_639_1";
    private static final String PARAM_KEY = "key";
    private static final String PARAM_NAME = "name";

    YoutubeVideo(JSONObject object) throws JSONException {
        id = object.getString(PARAM_ID);
        language = object.optString(PARAM_LANG, "en");
        key = object.getString(PARAM_KEY);
        name = object.getString(PARAM_NAME);
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }
}
