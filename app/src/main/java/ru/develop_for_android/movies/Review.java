package ru.develop_for_android.movies;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Review implements Parcelable {
    private String author;
    private String content;
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_CONTENT = "content";

    Review(JSONObject object) throws JSONException {
        author = object.getString(KEY_AUTHOR);
        content = object.getString(KEY_CONTENT);
    }

    private Review(Parcel in) {
        author = in.readString();
        content = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(author);
        parcel.writeString(content);
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}
