package ru.develop_for_android.movies;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import uk.co.markormesher.android_fab.SpeedDialMenuAdapter;
import uk.co.markormesher.android_fab.SpeedDialMenuItem;

public class FabAdapter extends SpeedDialMenuAdapter {

    private final CanChangeSortOrder listener;

    private int itemColor;

    FabAdapter(Context context, CanChangeSortOrder listener) {
        this.listener = listener;
        itemColor = ContextCompat.getColor(context, R.color.colorSecondary);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @NotNull
    @Override
    public SpeedDialMenuItem getMenuItem(Context context, int i) {
        switch (i) {
            case 0:
                return new SpeedDialMenuItem(context, R.drawable.ic_people, "Most popular");
            case 1:
                return new SpeedDialMenuItem(context, R.drawable.ic_star, "Highest rated");
            default:
                return new SpeedDialMenuItem(context);
        }
    }

    @Override
    public int getBackgroundColour(int position) {
        return itemColor;
    }

    @Override
    public boolean onMenuItemClick(int position) {
        if (position == 0) {
            listener.changeOrderTo(MoviesListLoader.SORT_BY_POPULARITY);
            return true;
        } else if (position == 1) {
            listener.changeOrderTo(MoviesListLoader.SORT_BY_RATE);
            return true;
        } else {
            return super.onMenuItemClick(position);
        }
    }

    interface CanChangeSortOrder {
        void changeOrderTo(int sortType);
    }
}
