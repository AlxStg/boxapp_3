package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.databinding.FragmentSeriesDetailsBinding;
import com.example.iptvsdk.ui.series_details.IptvSeriesDetails;
import com.example.iptvsdk.ui.series_details.SeriesDetailsListener;
import com.example.iptvsdk.ui.series_details.SeriesDetailsModel;

import io.reactivex.schedulers.Schedulers;

public class SeriesDetailsFragment extends Fragment {
    private FragmentSeriesDetailsBinding binding;
    private SeriesDetailsModel model;
    private IptvSeriesDetails iptvSeriesDetails;
    private int id;

    public SeriesDetailsFragment(int id) {
        this.id = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSeriesDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = new SeriesDetailsModel(getContext());
        iptvSeriesDetails = new IptvSeriesDetails(getContext());
        iptvSeriesDetails.setSeriesDetailsListener(new SeriesDetailsListener() {
            @Override
            public void onProviderLogo(String url) {
                model.setStreamService(url);
            }
        });
        iptvSeriesDetails.getModel(id)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .doOnSuccess(model -> {
                    binding.setModel(model);
                })
                .doOnError(throwable -> {
                    Log.e("SeriesDetailsFragment", "onViewCreated: ", throwable);
                })
                .subscribe();
    }
}
