package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.common.ListCategoriesAndStream;
import com.example.boxapp3.databinding.FragmentMoviesBinding;
import com.example.boxapp3.databinding.ItemListCategoriesBinding;
import com.example.boxapp3.databinding.ItemListStreamBinding;
import com.example.iptvsdk.ui.list_streams_categories.ListStreamsCategories;
import com.example.iptvsdk.ui.movies.IptvMovies;

public class MoviesFragment extends Fragment {

    private FragmentMoviesBinding binding;
    private IptvMovies iptvMovies;
    private ListStreamsCategories<ItemListCategoriesBinding, ItemListStreamBinding> listStreamsCategories;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMoviesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iptvMovies = new IptvMovies(getContext());
        binding.setBackgroundVodSeriesDataModel(iptvMovies.getBackgroundVodSeriesDataModel());
        listStreamsCategories = ListCategoriesAndStream.setupLists(getContext(),
                iptvMovies,
                binding.listCategories,
                ListStreamsCategories.TYPE_MOVIE);

        listStreamsCategories.setOnItemFocusListener((item, position) -> {
            iptvMovies.loadDetails(item.getId(), true);
        });
    }



}
