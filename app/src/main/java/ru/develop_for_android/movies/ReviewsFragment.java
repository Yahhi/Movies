package ru.develop_for_android.movies;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ru.develop_for_android.movies.data_structures.Review;
import ru.develop_for_android.movies.databinding.FragmentTrailersBinding;
import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReviewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReviewsFragment extends Fragment implements TrailerPresenter {
    private static final String ARG_PARAM = "trailers_array";

    private ReviewsAdapter adapter;
    private FragmentTrailersBinding binding;
    private static final int REQUEST_VIDEO_PLAYBACK = 1234;
    private ArrayList<Review> reviews;

    public ReviewsFragment() {
        // Required empty public constructor
    }

    public static ReviewsFragment newInstance(ArrayList<Review> reviews) {
        ReviewsFragment fragment = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM, reviews);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            if (savedInstanceState != null) {
                reviews = savedInstanceState.getParcelableArrayList(ARG_PARAM);
            }
        } else {
           reviews =  getArguments().getParcelableArrayList(ARG_PARAM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_trailers, container, false);
        binding.trailersList.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReviewsAdapter(reviews);
        binding.trailersList.setAdapter(adapter);
        return binding.getRoot();
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
        if (adapter != null) {
            adapter.setReviews(reviews);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (reviews != null) {
            outState.putParcelableArrayList(ARG_PARAM, reviews);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onTrailerClick(String key) {
        Timber.i("VIDEO is starting with key %s", key);
        Intent videoClient = new Intent(Intent.ACTION_VIEW);
        videoClient.setData(Uri.parse("http://m.youtube.com/watch?v=" + key));
        videoClient.putExtra("finish_on_ended", true);
        //videoClient.putExtra("force_fullscreen", true);
        startActivityForResult(videoClient, REQUEST_VIDEO_PLAYBACK);
    }
}
