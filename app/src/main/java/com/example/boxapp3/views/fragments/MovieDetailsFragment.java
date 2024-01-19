package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.R;
import com.example.boxapp3.databinding.FragmentMovieDetailsBinding;
import com.example.boxapp3.databinding.ItemListStreamBinding;
import com.example.boxapp3.listeners.activities.MainActivityListener;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.data.models.xtream.StreamXc;
import com.example.iptvsdk.ui.list_stream.IptvListStream;
import com.example.iptvsdk.ui.list_streams_categories.model.ListStreamsModel;
import com.example.iptvsdk.ui.movie_details.IptvMovieDetails;
import com.example.iptvsdk.ui.movie_details.MoviesDetailsListener;
import com.example.iptvsdk.utils.DialogErrorUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MovieDetailsFragment extends Fragment {
    FragmentMovieDetailsBinding binding;
    IptvMovieDetails iptvMovieDetails;
    MainActivityListener mainActivityListener;
    int id;

    public MovieDetailsFragment(int id, MainActivityListener mainActivityListener) {
        this.id = id;
        this.mainActivityListener = mainActivityListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMovieDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        iptvMovieDetails = new IptvMovieDetails(getContext());
        iptvMovieDetails.loadLogos();
        iptvMovieDetails.setMoviesDetailsListener(new MoviesDetailsListener() {
            @Override
            public void onChangeFavorite(boolean isFavorite) {
                binding.imageView243.setSelected(isFavorite);
            }

            @Override
            public void loadAlsoWatch(boolean isKids) {
                alsoWatch(isKids);
            }
        });
        iptvMovieDetails.getModel(id)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(model -> {
                    binding.setModel(model);
                })
                .doOnError(throwable -> {
                    DialogErrorUtil.showError(getActivity(), throwable,
                            getString(R.string.error_to_load_movie_details),
                            getString(R.string.occurred_an_error_to_load_movie_details));
                })
                .subscribe();
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                binding.favButton.requestFocus();
            }
        });
    }



    private void alsoWatch(boolean isKids) {
        List<Integer> idsAlreadyUsed = new ArrayList<>();
        IptvListStream iptvListStream = new IptvListStream(getContext(), false,
                StreamXc.TYPE_STREAM_VOD, -1);
//        iptvListStream.setRandom(true);

        iptvListStream.loadKids(isKids);


        GenericAdapter<ListStreamsModel, ItemListStreamBinding> adapter = new
                GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<ListStreamsModel, ItemListStreamBinding>(){

            @Override
            public ItemListStreamBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                return ItemListStreamBinding.inflate(inflater, parent, false);
            }

            @Override
            public Single<ListStreamsModel> getItem(int position) {
                return isKids ? iptvListStream.getModel(position) :
                        iptvListStream.getModel(idsAlreadyUsed);
            }

            @Override
            public Observable<Integer> getTotalItems() {
                return iptvListStream.getTotalItems();
            }

            @Override
            public void setModelToItem(ItemListStreamBinding binding,
                                       ListStreamsModel item,
                                       int bindingAdapterPosition,
                                       GenericAdapter<ListStreamsModel, ItemListStreamBinding> adapter) {
                idsAlreadyUsed.add(item.getId());
                binding.setModel(item);
                binding.getRoot().setOnClickListener(v -> {
                   mainActivityListener.openDetails(item.getId(), item.getType());
                });

                binding.getRoot().setOnKeyListener((v, keyCode, event) -> {
                    if(event.getAction() == KeyEvent.ACTION_DOWN) {
                        if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                            if(bindingAdapterPosition == 0) {
                                mainActivityListener.onGoToMenu();
                                return true;
                            }
                        }
                    }
                    return false;
                });
            }
        });
        binding.horizontalGridView.setAdapter(adapter);
    }
}
