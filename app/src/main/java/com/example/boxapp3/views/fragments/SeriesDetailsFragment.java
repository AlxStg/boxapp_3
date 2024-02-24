package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.R;
import com.example.boxapp3.databinding.FragmentSeriesDetailsBinding;
import com.example.boxapp3.databinding.ItemSeasonBinding;
import com.example.boxapp3.databinding.ThumbEpisodeBinding;
import com.example.boxapp3.listeners.activities.MainActivityListener;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.ui.series_details.EpisodeModel;
import com.example.iptvsdk.ui.series_details.IptvSeriesDetails;
import com.example.iptvsdk.ui.series_details.SeasonModel;
import com.example.iptvsdk.ui.series_details.SeriesDetailsListener;
import com.example.iptvsdk.ui.series_details.SeriesDetailsModel;

import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class SeriesDetailsFragment extends Fragment {
    private FragmentSeriesDetailsBinding mBinding;
    private SeriesDetailsModel model;
    private IptvSeriesDetails iptvSeriesDetails;
    private MainActivityListener mainActivityListener;
    private int id, mEpisode, mSeason;

    public SeriesDetailsFragment(int id, MainActivityListener mainActivityListener) {
        this.id = id;
        this.mainActivityListener = mainActivityListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentSeriesDetailsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
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

            @Override
            public void episodes(List<EpisodeModel> episodes) {
                showEpisodes(episodes);
            }

            @Override
            public void onAlreadyWatched(int season, int episode) {
                super.onAlreadyWatched(season, episode);
                mEpisode = episode;
                mSeason = season;
                mBinding.episodes.setSelectedPosition(episode);
                mBinding.episodes.scrollToPosition(episode);

                mBinding.episodes.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mBinding.episodes.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mBinding.episodes.setSelectedPosition(episode);
                        mBinding.episodes.scrollToPosition(episode);
                    }
                });

                mBinding.seasons.setSelectedPosition(season);
                mBinding.seasons.scrollToPosition(season);

            }
        });
        iptvSeriesDetails.getModel(id)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .doOnSuccess(model -> {
                    mBinding.setModel(model);
                })
                .doOnError(throwable -> {
                    Log.e("SeriesDetailsFragment", "onViewCreated: ", throwable);
                })
                .subscribe();
        setupSeasons();

        mBinding.episodes.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.episodes.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mBinding.episodes.requestFocus();
            }
        });
    }

    private void showEpisodes(List<EpisodeModel> episodes) {
        GenericAdapter<EpisodeModel, ThumbEpisodeBinding> adapter = new GenericAdapter<>(getContext(),
                new GenericAdapter.GenericAdapterHelper<EpisodeModel, ThumbEpisodeBinding>() {
                    @Override
                    public ThumbEpisodeBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                        return ThumbEpisodeBinding.inflate(inflater, parent, false);
                    }

                    @Override
                    public Single<EpisodeModel> getItem(int position) {
                        EpisodeModel ep = episodes.get(position);
                        ep.setEpisodeName(String.format(Locale.getDefault(),
                                getString(R.string.episode_d), ep.getEpisodeNumber()));
                        return Single.just(ep);
                    }

                    @Override
                    public Observable<Integer> getTotalItems() {
                        return episodes.size() == 0 ? Observable.just(0) : Observable.just(episodes.size());
                    }

                    @Override
                    public void setModelToItem(ThumbEpisodeBinding binding, EpisodeModel item, int bindingAdapterPosition, GenericAdapter<EpisodeModel, ThumbEpisodeBinding> adapter) {
                        binding.setModel(item);
                        binding.getRoot().setOnKeyListener((v, keyCode, event) -> {
                            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                                    if (bindingAdapterPosition == 0) {
                                        mainActivityListener.onGoToMenu();
                                        return true;
                                    }
                                }
                            }
                            return false;
                        });
                        binding.getRoot().setOnClickListener(v -> {
                            mainActivityListener.onPlayEpisode(id, Integer.parseInt(item.getId()));
                            mEpisode = bindingAdapterPosition;
                            iptvSeriesDetails.saveSeasonAndEpisode(mSeason, mEpisode);
                        });
                    }
                });
        getActivity().runOnUiThread(() -> {
            mBinding.episodes.setAdapter(adapter);
        });
    }

    private void setupSeasons() {
        iptvSeriesDetails.getSeasons()
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .doOnSuccess(seasons -> {
                    GenericAdapter<SeasonModel, ItemSeasonBinding> adapter =
                            new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<SeasonModel, ItemSeasonBinding>() {
                                int selectedPosition = 1;
                                @Override
                                public ItemSeasonBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                                    return ItemSeasonBinding.inflate(inflater, parent, false);
                                }

                                @Override
                                public Single<SeasonModel> getItem(int position) {
                                    SeasonModel seasonModel = new SeasonModel(seasons.get(position));
                                    return Single.just(seasonModel);
                                }

                                @Override
                                public Observable<Integer> getTotalItems() {
                                    return seasons.size() == 0 ? Observable.just(0) : Observable.just(seasons.size());
                                }

                                @Override
                                public void setModelToItem(ItemSeasonBinding binding, SeasonModel item, int bindingAdapterPosition, GenericAdapter<SeasonModel, ItemSeasonBinding> adapter) {
                                    if(selectedPosition == item.getSeasonNumber())
                                        binding.getRoot().setSelected(true);

                                    binding.setModel(item);
                                    binding.getRoot().setOnClickListener(v -> {
                                        iptvSeriesDetails.loadSeason(item.getSeasonNumber());
                                        mSeason = bindingAdapterPosition;
                                        mEpisode = 0;
                                        iptvSeriesDetails.saveSeasonAndEpisode(mSeason, mEpisode);
                                        selectedPosition = item.getSeasonNumber();
                                        adapter.notifyDataSetChanged();
                                    });
                                    binding.getRoot().setOnKeyListener((v, keyCode, event) -> {
                                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                                                if (bindingAdapterPosition == 0) {
                                                    mainActivityListener.onGoToMenu();
                                                    return true;
                                                }
                                            }
                                        }
                                        return false;
                                    });
                                }
                            });
                    getActivity().runOnUiThread(() -> {
                        mBinding.seasons.setAdapter(adapter);
                    });
                    mBinding.seasons.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mBinding.seasons.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            new Handler().postDelayed(() -> {
                                mBinding.seasons.scrollToPosition(mSeason);
                                mBinding.seasons.setSelectedPosition(mSeason);
                            }, 1000);
                        }
                    });
                })
                .doOnError(throwable -> {
                    Log.e("SeriesDetailsFragment", "setupSeasons: ", throwable);
                })
                .subscribe();
    }
}
