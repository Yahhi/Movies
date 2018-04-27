package ru.develop_for_android.movies;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import timber.log.Timber;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    /**
     * The total number of items in the dataset after the last load
     */
    private int mPreviousTotal = 0;
    /**
     * True if we are still waiting for the last set of data to load.
     */
    private boolean mLoading = true;

    public void listIsReloaded() {
        mPreviousTotal = 0;
        mLoading = true;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = recyclerView.getLayoutManager().getItemCount();
        int firstVisibleItem = ((GridLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        Timber.i("first visible item position is %d, and all visible count is %d", firstVisibleItem, visibleItemCount);

        if (mLoading) {
            if (totalItemCount > mPreviousTotal) {
                mLoading = false;
                mPreviousTotal = totalItemCount;
                Timber.i("new total item value %d", totalItemCount);
            }
        }
        int visibleThreshold = 6;
        if (!mLoading && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {
            Timber.i("list end found");

            onLoadMore();

            mLoading = true;
        }
    }

    abstract void onLoadMore();
}
