package ru.develop_for_android.movies.data_structures;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import timber.log.Timber;

import static ru.develop_for_android.movies.data_structures.MovieContract.columnsArray;

public class MovieProvider extends ContentProvider {

    public static final int CODE_MOVIES = 100;
    private static final UriMatcher matcher = buildUriMatcher();
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIES, CODE_MOVIES);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = DBHelper.getHelper(getContext()).getReadableDatabase();
        Cursor cursor;
        switch (matcher.match(uri)) {
            case CODE_MOVIES:
                Timber.i("request to select with args: selection='%s' and sortOrder='%s'", selection, sortOrder);
                cursor = database.query(MovieContract.MovieEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, null);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                Timber.i("cursor size %d", cursor.getCount());
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("We are not implementing getType");
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase database = DBHelper.getHelper(getContext()).getWritableDatabase();
        int rawsProcessed = 0;
        switch (matcher.match(uri)) {
            case CODE_MOVIES:
                for (ContentValues values1 : values) {
                    long result;
                    long innerId = values1.getAsLong(MovieContract.MovieEntry._ID);
                    if (movieAlreadySaved(database, innerId)) {
                        result = database.update(MovieContract.MovieEntry.TABLE_NAME, values1,
                                MovieContract.MovieEntry._ID + " = ?",
                                new String[]{values1.getAsString(MovieContract.MovieEntry._ID)});
                    } else {
                        result = database.insert(MovieContract.MovieEntry.TABLE_NAME, "", values1);
                    }
                    if (result > 0) {
                        rawsProcessed++;
                    }
                }
                if (rawsProcessed > 0 ) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    Timber.i("bulkInsert finished with %d raws", rawsProcessed);
                }
                return rawsProcessed;
            default:
                return super.bulkInsert(uri, values);
        }
    }


    private boolean movieAlreadySaved(SQLiteDatabase database, long id) {
        Cursor cursor = database.query(MovieContract.MovieEntry.TABLE_NAME, columnsArray,
                MovieContract.MovieEntry._ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new RuntimeException("We are not implementing insert");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = DBHelper.getHelper(getContext()).getWritableDatabase();
        int deletedRawsCount = database.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
        if (deletedRawsCount > 0 ) {
            getContext().getContentResolver().notifyChange(uri, null);
            Timber.i("Delete request finished with %d", deletedRawsCount);
        }
        return deletedRawsCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = DBHelper.getHelper(getContext()).getWritableDatabase();
        switch (matcher.match(uri)) {
            case CODE_MOVIES:
                int updatedRaws = database.update(MovieContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                Timber.i("Update request finished with %d", updatedRaws);
                return updatedRaws;
            default:
                return 0;

        }
    }
}
