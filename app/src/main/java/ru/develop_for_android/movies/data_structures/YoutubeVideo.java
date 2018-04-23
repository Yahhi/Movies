package ru.develop_for_android.movies.data_structures;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class YoutubeVideo implements Parcelable {
    private String id;
    private String language;
    private String name;
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

    private YoutubeVideo(Parcel in) {
        id = in.readString();
        language = in.readString();
        name = in.readString();
        key = in.readString();
    }

    public static final Creator<YoutubeVideo> CREATOR = new Creator<YoutubeVideo>() {
        @Override
        public YoutubeVideo createFromParcel(Parcel in) {
            return new YoutubeVideo(in);
        }

        @Override
        public YoutubeVideo[] newArray(int size) {
            return new YoutubeVideo[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(language);
        parcel.writeString(name);
        parcel.writeString(key);
    }

}
