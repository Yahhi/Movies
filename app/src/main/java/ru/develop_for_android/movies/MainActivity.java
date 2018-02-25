package ru.develop_for_android.movies;

import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;

import org.json.JSONObject;

import uk.co.markormesher.android_fab.FloatingActionButton;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<JSONObject[]>,
        MovieClick {

    private static final int REQUEST_INVITE = 101;
    private static final String TAG = "FIREBASE";
    private static final int MOVIE_LOADER_ID = 110;

    private int sortType = MoviesListLoader.SORT_BY_POPULARITY;
    private static final String KEY_SORT_TYPE = "sort_type";

    ProgressBar loadingIndicator;
    MovieListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setSpeedDialMenuAdapter(new FabAdapter());

        loadingIndicator = findViewById(R.id.movies_loading_progress);

        RecyclerView moviesList = findViewById(R.id.movies_recycleview);
        adapter = new MovieListAdapter(this);
        moviesList.setAdapter(adapter);
        int columnsCount = getColumnsByDisplay();
        moviesList.setLayoutManager(new GridLayoutManager(this, columnsCount));

        loadMovieData();
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
        Log.i(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.i(TAG, "onActivityResult: sent invitation " + id);
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

    @Override
    public android.support.v4.content.Loader<JSONObject[]> onCreateLoader(int id, Bundle args) {
        int sortType = args.getInt(KEY_SORT_TYPE);
        return new MoviesListLoader<JSONObject[]>(this, sortType);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<JSONObject[]> loader, JSONObject[] data) {
        adapter.updateList(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<JSONObject[]> loader) {

    }

    @Override
    public void onMovieClick(int id) {
        Log.i("CLICK", String.valueOf(id));
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
}
