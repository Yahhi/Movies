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

import ru.develop_for_android.movies.data_structures.YoutubeVideo;
import ru.develop_for_android.movies.databinding.FragmentTrailersBinding;
import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrailersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrailersFragment extends Fragment implements TrailerPresenter {
    private static final String ARG_PARAM = "trailers_array";

    private YoutubeVideo[] trailers;
    private TrailerAdapter adapter;
    FragmentTrailersBinding binding;
    private static final int REQUEST_VIDEO_PLAYBACK = 1234;

    public TrailersFragment() {
        // Required empty public constructor
    }

    public static TrailersFragment newInstance(YoutubeVideo[] trailers) {
        TrailersFragment fragment = new TrailersFragment();
        Bundle args = new Bundle();
        args.putParcelableArray(ARG_PARAM, trailers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate in fragment");
        if (getArguments() == null) {
            if (savedInstanceState == null) {
                adapter = new TrailerAdapter(this);
            } else {
                trailers = (YoutubeVideo[]) savedInstanceState.getParcelableArray(ARG_PARAM);
                adapter = new TrailerAdapter(trailers, this);
            }
        } else {
            trailers = (YoutubeVideo[]) getArguments().getParcelableArray(ARG_PARAM);
            adapter = new TrailerAdapter(trailers, this);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Timber.i( "onCreateView in fragment");
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_trailers, container, false);
        binding.trailersList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.trailersList.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onTrailerClick(String key) {
        Timber.i("video is starting with key %s", key);
        Intent videoClient = new Intent(Intent.ACTION_VIEW);
        videoClient.setData(Uri.parse("http://m.youtube.com/watch?v=" + key));
        videoClient.putExtra("finish_on_ended", true);
        //videoClient.putExtra("force_fullscreen", true);
        startActivityForResult(videoClient, REQUEST_VIDEO_PLAYBACK);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (trailers != null) {
            outState.putParcelableArray(ARG_PARAM, trailers);
        }
        super.onSaveInstanceState(outState);
    }

    public void setTrailers(YoutubeVideo[] trailers) {
        this.trailers = trailers;
        if (adapter != null) {
            adapter.setTrailers(trailers);
        }
    }
}
