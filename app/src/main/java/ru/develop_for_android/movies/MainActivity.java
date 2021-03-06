package ru.develop_for_android.movies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Date;

import ru.develop_for_android.movies.data_structures.Movie;
import ru.develop_for_android.movies.data_structures.MovieContract;
import timber.log.Timber;

import static ru.develop_for_android.movies.MoviesListLoader.KEY_LAST_HIGHEST_UPDATED_PAGE;
import static ru.develop_for_android.movies.MoviesListLoader.KEY_LAST_HIGHEST_UPDATE_TIME;
import static ru.develop_for_android.movies.MoviesListLoader.KEY_LAST_POPULAR_UPDATED_PAGE;
import static ru.develop_for_android.movies.MoviesListLoader.KEY_LAST_POPULAR_UPDATE_TIME;
import static ru.develop_for_android.movies.MoviesListLoader.SORT_BY_POPULARITY;
import static ru.develop_for_android.movies.MoviesListLoader.SORT_BY_RATE;


public class MainActivity extends AppCompatActivity
        implements MovieClick {

    private static final int REQUEST_INVITE = 101;
    private static final int MOVIE_LOADER_ID = 110;
    private static final int REQUEST_DETAILS = 501;

    private static final int REFRESH_INTERVAL = 60 * 60 * 1000;

    private int sortType;
    private int pagePopularity = 1;
    private int pageHighest = 1;
    Parcelable listState;
    private static final String KEY_SORT_TYPE = "sort_type";
    private static final String KEY_LIST_STATE = "list_state";

    ProgressBar loadingIndicator;
    MovieLocalListAdapter adapter;
    FirebaseRemoteConfig config;
    GridLayoutManager recyclerViewLayoutManager;

    MovieScrollListener listener;
    //private MyObserver myObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        config = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        config.setConfigSettings(configSettings);
        config.setDefaults(R.xml.remote_config_defaults);
        fetchSettings();

        loadingIndicator = findViewById(R.id.movies_loading_progress);
        if (savedInstanceState == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            sortType = preferences.getInt(KEY_SORT_TYPE, MoviesListLoader.SORT_BY_POPULARITY);
            Timber.i("instance state missing");
        } else {
            sortType = savedInstanceState.getInt(KEY_SORT_TYPE);
            Timber.i("instance state loaded");
        }

        if (getLastUpdateTimeDifference(sortType) > REFRESH_INTERVAL) {
            pageHighest = 1;
            pagePopularity = 1;
            initNetworkRequest(SORT_BY_POPULARITY);
            initNetworkRequest(SORT_BY_RATE);
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            pagePopularity = preferences.getInt(KEY_LAST_HIGHEST_UPDATED_PAGE, 1);
            pageHighest = preferences.getInt(KEY_LAST_POPULAR_UPDATED_PAGE, 1);
        }

        RecyclerView moviesList = findViewById(R.id.movies_recycleview);
        adapter = new MovieLocalListAdapter(this, null);
        moviesList.setAdapter(adapter);
        int columnsCount = getColumnsByDisplay();
        recyclerViewLayoutManager = new GridLayoutManager(this, columnsCount);
        moviesList.setLayoutManager(recyclerViewLayoutManager);
        listener = new MovieScrollListener();
        moviesList.addOnScrollListener(listener);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        switch (sortType) {
            case MoviesListLoader.SORT_BY_POPULARITY:
                bottomNavigationView.setSelectedItemId(R.id.action_popular);
                break;
            case MoviesListLoader.SORT_BY_RATE:
                bottomNavigationView.setSelectedItemId(R.id.action_highest);
                break;
            default:
                bottomNavigationView.setSelectedItemId(R.id.action_starred);
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                listener.listIsReloaded();
                switch (item.getItemId()) {
                    case R.id.action_popular:
                        changeOrderTo(MoviesListLoader.SORT_BY_POPULARITY);
                        break;
                    case R.id.action_highest:
                        changeOrderTo(MoviesListLoader.SORT_BY_RATE);
                        break;
                    case R.id.action_starred:
                        changeOrderTo(MoviesListLoader.SORT_STARRED);
                        break;
                }
                return true;
            }
        });

        loadMovieData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
        prefEditor.putInt(KEY_SORT_TYPE, sortType);
        prefEditor.apply();

        outState.putInt(KEY_SORT_TYPE, sortType);
        outState.putParcelable(KEY_LIST_STATE, recyclerViewLayoutManager.onSaveInstanceState());
        Timber.i("instance state saved");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null) {
            Timber.i("another place where instance state is restored");
            listState = savedInstanceState.getParcelable(KEY_LIST_STATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*if (myObserver == null) {
            myObserver = new MyObserver(new Handler());
            getContentResolver().registerContentObserver(MovieContract.MovieEntry.CONTENT_URI, true, myObserver);
        }*/

        if (listState != null) {
            recyclerViewLayoutManager.onRestoreInstanceState(listState);
        }
    }

    @Override protected void onPause() {
        super.onPause();
        //getContentResolver().unregisterContentObserver(myObserver);
    }

    private void fetchSettings() {
        long cacheExpiration = 3600; // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (config.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        // [START fetch_config_with_callback]
        // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
        // will use fetch data from the Remote Config service, rather than cached parameter values,
        // if cached parameter values are more than cacheExpiration seconds old.
        // See Best Practices in the README for more information.
        config.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            config.activateFetched();
                        } else {
                            Timber.i("Config fetch Failed");
                        }
                    }
                });
        // [END fetch_config_with_callback]
    }

    private void loadMovieData() {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<Cursor> moviesLoader = loaderManager.getLoader(MOVIE_LOADER_ID);
        Bundle loadingBundle = new Bundle();
        loadingBundle.putInt(KEY_SORT_TYPE, sortType);
        if (moviesLoader == null) {
            loaderManager.initLoader(MOVIE_LOADER_ID, loadingBundle, dataManager);
        } else {
            loaderManager.restartLoader(MOVIE_LOADER_ID, loadingBundle, dataManager);
        }
    }

    LoaderManager.LoaderCallbacks<Cursor> dataManager = new LoaderManager.LoaderCallbacks<Cursor>() {
        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
            loadingIndicator.setVisibility(View.VISIBLE);
            int sortType = args.getInt(KEY_SORT_TYPE);
            Timber.i("OnCreateLoader %d", id);
            return new CursorLoader(MainActivity.this, MovieContract.MovieEntry.CONTENT_URI,
                    MovieContract.columnsArray, MovieContract.getSelectonForSort(sortType),
                    null, MovieContract.getOrderForSort(sortType));
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            loadingIndicator.setVisibility(View.GONE);
            Timber.i("cursor is loaded");
            adapter.updateList(data);
            listener.listIsReloaded();
            if (listState != null) {
                recyclerViewLayoutManager.onRestoreInstanceState(listState);
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {
            Timber.i("cursor should be cleared");
            adapter.updateList(null);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear) {
            getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, null, null);
            pageHighest = 1;
            pagePopularity = 1;
            initNetworkRequest(SORT_BY_POPULARITY);
            initNetworkRequest(SORT_BY_RATE);
            return true;
        } else if (id == R.id.action_invite_people) {
            onInviteClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.i("onActivityResult: requestCode=%s, resultCode=%s", requestCode, resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Timber.i( "onActivityResult: sent invitation %s", id);
                }
            } else {
                Toast.makeText(this, "Failed to send invitation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    private long getLastUpdateTimeDifference(int sort) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        long lastTimeStamp;
        if (sort == MoviesListLoader.SORT_BY_POPULARITY) {
            lastTimeStamp = preferences.getLong(KEY_LAST_POPULAR_UPDATE_TIME, 0);
        } else {
            lastTimeStamp = preferences.getLong(KEY_LAST_HIGHEST_UPDATE_TIME, 0);
        }
        return (new Date()).getTime() - lastTimeStamp;
    }

    @Override
    public void onMovieClick(Movie movie) {
        Intent openDetails = new Intent(this, MovieDetailsActivity.class);
        openDetails.putExtra(MovieDetailsActivity.ARGS_MOVIE, movie);
        startActivityForResult(openDetails, REQUEST_DETAILS);
    }

    public int getColumnsByDisplay() {
        int size = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        int orientation = getResources().getConfiguration().orientation;
        if (size == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return 4;
            } else {
                return 3;
            }
        } else {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return 3;
            } else {
                return 2;
            }
        }
    }

    public void changeOrderTo(int sortType) {
        this.sortType = sortType;
        loadMovieData();
    }

    private void initNetworkRequest(int sortBy) {
        Timber.i("send request to load more");
        Intent intent = new Intent(getApplicationContext(), MoviesListLoader.class);
        intent.putExtra(MoviesListLoader.KEY_SORT, sortBy);
        if (sortBy == MoviesListLoader.SORT_BY_POPULARITY) {
            intent.putExtra(MoviesListLoader.KEY_PAGE, pagePopularity++);
        } else {
            intent.putExtra(MoviesListLoader.KEY_PAGE, pageHighest++);
        }
        startService(intent);
    }

    class MovieScrollListener extends EndlessRecyclerOnScrollListener {
        @Override
        void onLoadMore() {
            if (sortType != MoviesListLoader.SORT_STARRED) {
                if (sortType == MoviesListLoader.SORT_BY_POPULARITY) {
                    Timber.i("popularity. want to load more with %d", pagePopularity);
                } else {
                    Timber.i("highest. want to load more with %d", pageHighest);
                }
                initNetworkRequest(sortType);
            }
        }
    }

    /*class MyObserver extends ContentObserver {

        MyObserver(Handler handler) {
            super(handler);
        }

        @Override public void onChange(boolean selfChange) {
            super.onChange(selfChange, null);
        }

        @Override public void onChange(boolean selfChange, Uri uri) {
            Timber.i("weeeeeee");
            Bundle loadingBundle = new Bundle();
            loadingBundle.putInt(KEY_SORT_TYPE, sortType);
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, loadingBundle, dataManager);
        }
    }*/
}