package ru.develop_for_android.movies;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ru.develop_for_android.movies.databinding.ListItemTrailerBinding;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private YoutubeVideo[] trailers;
    private TrailerPresenter clickListener;

    TrailerAdapter(YoutubeVideo[] initialTrailers, TrailerPresenter presenter) {
        trailers = initialTrailers;
        clickListener = presenter;
    }

    @NonNull
    @Override
    public TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemTrailerBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.list_item_trailer, parent, false);
        return new TrailerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerViewHolder holder, int position) {
        holder.bind(trailers[position]);
    }

    @Override
    public int getItemCount() {
        return trailers.length;
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder {
        private final ListItemTrailerBinding mBinding;


        TrailerViewHolder(ListItemTrailerBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind(@NonNull YoutubeVideo video) {
            mBinding.setTrailer(video);
            mBinding.setVariable(BR.presenter, clickListener);
        }
    }
}
