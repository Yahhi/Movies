package ru.develop_for_android.movies;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import ru.develop_for_android.movies.data_structures.Review;
import ru.develop_for_android.movies.databinding.ListItemReviewBinding;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private ArrayList<Review> reviews;

    ReviewsAdapter(ArrayList<Review> reviews) {
        this.reviews = new ArrayList<>();
        if (reviews != null) {
            this.reviews.addAll(reviews);
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemReviewBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.list_item_review, parent, false);
        return new ReviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews.addAll(reviews);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final ListItemReviewBinding mBinding;


        ReviewViewHolder(ListItemReviewBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind(@NonNull Review review) {
            mBinding.setReview(review);
            //mBinding.setVariable(BR.presenter, clickListener);
        }
    }
}
