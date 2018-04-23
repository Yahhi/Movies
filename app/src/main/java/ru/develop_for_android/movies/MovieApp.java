package ru.develop_for_android.movies;

import android.app.Application;

import timber.log.Timber;

public class MovieApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

}
