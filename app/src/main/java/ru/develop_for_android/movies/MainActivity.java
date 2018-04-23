package ru.develop_for_android.movies;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.LoaderManager;
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

import org.json.JSONObject;

import timber.log.Timber;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<JSONObject[]>,
        MovieClick {

    private static final int REQUEST_INVITE = 101;
    private static final String TAG = "FIREBASE";
    private static final int MOVIE_LOADER_ID = 110;

    private int sortType = MoviesListLoader.SORT_BY_POPULARITY;
    private static final String KEY_SORT_TYPE = "sort_type";

    static final String RANDOM_KEY = "random_backdrop";

    ProgressBar loadingIndicator;
    MovieListAdapter adapter;
    FirebaseRemoteConfig config;

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

        RecyclerView moviesList = findViewById(R.id.movies_recycleview);
        adapter = new MovieListAdapter(this);
        moviesList.setAdapter(adapter);
        int columnsCount = getColumnsByDisplay();
        moviesList.setLayoutManager(new GridLayoutManager(this, columnsCount));

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_popular:
                        changeOrderTo(MoviesListLoader.SORT_BY_POPULARITY);
                        break;
                    case R.id.action_highest:
                        changeOrderTo(MoviesListLoader.SORT_BY_RATE);
                        break;
                    case R.id.action_starred:
                        break;
                }
                return true;
            }
        });
        loadMovieData();
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
                            Toast.makeText(MainActivity.this, "Fetch Succeeded",
                                    Toast.LENGTH_SHORT).show();

                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            config.activateFetched();
                        } else {
                            Toast.makeText(MainActivity.this, "Fetch Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END fetch_config_with_callback]
    }

    private void loadMovieData() {
        LoaderManager loaderManager = getSupportLoaderManager();
        android.support.v4.content.Loader<JSONObject[]> moviesLoader = loaderManager.getLoader(MOVIE_LOADER_ID);
        Bundle loadingBundle = new Bundle();
        loadingBundle.putInt(KEY_SORT_TYPE, sortType);
        if (moviesLoader == null) {
            loaderManager.initLoader(MOVIE_LOADER_ID, loadingBundle, this);
        } else {
            loaderManager.restartLoader(MOVIE_LOADER_ID, loadingBundle, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_invite_people) {
            onInviteClicked();
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

    @NonNull
    @Override
    public android.support.v4.content.Loader<JSONObject[]> onCreateLoader(int id, Bundle args) {
        loadingIndicator.setVisibility(View.VISIBLE);
        int sortType = args.getInt(KEY_SORT_TYPE);
        return new MoviesListLoader<JSONObject[]>(this, sortType);
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<JSONObject[]> loader, JSONObject[] data) {
        loadingIndicator.setVisibility(View.GONE);
        adapter.updateList(data);
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<JSONObject[]> loader) {

    }

    @Override
    public void onMovieClick(Movie movie) {
        Intent openDetails = new Intent(this, MovieDetailsActivity.class);
        openDetails.putExtra(MovieDetailsActivity.ARGS_MOVIE, movie);
        startActivity(openDetails);
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
}
