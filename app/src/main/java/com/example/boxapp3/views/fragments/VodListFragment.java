package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.common.ListCategoriesAndStream;
import com.example.boxapp3.databinding.FragmentVodListBinding;
import com.example.boxapp3.databinding.ItemListCategoriesBinding;
import com.example.boxapp3.databinding.ItemListStreamBinding;
import com.example.boxapp3.listeners.activities.MainActivityListener;
import com.example.iptvsdk.ui.list_streams_categories.ListStreamsCategories;
import com.example.iptvsdk.ui.movies.IptvMovies;

public class VodListFragment extends Fragment {

    private FragmentVodListBinding binding;
    private IptvMovies iptvMovies;
    private ListStreamsCategories<ItemListCategoriesBinding, ItemListStreamBinding> listStreamsCategories;
    private MainActivityListener mainActivityListener;
    private String type;

    public VodListFragment(String type, MainActivityListener mainActivityListener) {
        this.type = type;
        this.mainActivityListener = mainActivityListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVodListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iptvMovies = new IptvMovies(getContext());
        binding.setBackgroundVodSeriesDataModel(iptvMovies.getBackgroundVodSeriesDataModel());
        setTypeInUi();
        listStreamsCategories = ListCategoriesAndStream.setupLists(getContext(),
                iptvMovies,
                binding.listCategories,
                type);

        listStreamsCategories.setOnItemFocusListener((item, position) -> {
            iptvMovies.loadDetails(item.getId(), item.getType(), true);
        });

        listStreamsCategories.setOnItemClickListener((item, position) -> {
            mainActivityListener.openDetails(item.getId(), item.getType());
        });
        listStreamsCategories.setOnKeyClickListener((keyCode, event, item, position, adapterPosition) -> {
            if(event.getAction() == KeyEvent.ACTION_DOWN) {
                if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if(adapterPosition == 0) {
                        mainActivityListener.onGoToMenu();
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private void setTypeInUi() {
        String title = "";

        switch (type) {
            case "movie":
                title = getString(com.example.iptvsdk.R.string.movies);
                break;
            case "series":
                title = getString(com.example.iptvsdk.R.string.series);
                break;
            case "kids":
                title = getString(com.example.iptvsdk.R.string.kids);
                break;
        }
        iptvMovies.getBackgroundVodSeriesDataModel().setType(title);
    }


}
