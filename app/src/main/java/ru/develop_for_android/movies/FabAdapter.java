package ru.develop_for_android.movies;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import uk.co.markormesher.android_fab.SpeedDialMenuAdapter;
import uk.co.markormesher.android_fab.SpeedDialMenuItem;

public class FabAdapter extends SpeedDialMenuAdapter {
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
    public boolean onMenuItemClick(int position) {
        return super.onMenuItemClick(position);
    }
}
