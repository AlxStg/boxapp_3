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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SeriesDetailsFragment extends Fragment {
    private FragmentSeriesDetailsBinding mBinding;
    private SeriesDetailsModel model;
    private IptvSeriesDetails iptvSeriesDetails;
    private MainActivityListener mainActivityListener;
    private GenericAdapter<EpisodeModel, ThumbEpisodeBinding> mEpisodeAdapter;
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
        mEpisodeAdapter = new GenericAdapter<>(getContext(),
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
            mBinding.episodes.setAdapter(mEpisodeAdapter);
        });

        mEpisodeAdapter.totalItemsObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(integer -> {
                    if (integer > 0) {
                        mBinding.episodes.getViewTreeObserver()
                                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                    @Override
                                    public void onGlobalLayout() {
                                        mBinding.episodes.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                        new Handler().postDelayed(() -> {
                                            mBinding.episodes.setSelectedPosition(mEpisode);
                                            mBinding.episodes.scrollToPosition(mEpisode);
//                                            mEpisodeAdapter.handleSelection(mEpisode);
                                        }, 1000);
                                    }
                                });
                    }
                })
                .doOnError(throwable -> {
                    Log.e("SeriesDetailsFragment", "setupSeasons: ", throwable);
                })
                .subscribe();
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
                                    if (selectedPosition == item.getSeasonNumber())
                                        binding.getRoot().setSelected(true);

                                    binding.setModel(item);
                                    binding.getRoot().setOnClickListener(v -> {
                                        loadEpisodes(item, bindingAdapterPosition, adapter);
                                    });
                                    binding.getRoot().setOnFocusChangeListener((v, hasFocus) -> {
                                        if (hasFocus) {
                                            loadEpisodes(item, bindingAdapterPosition, adapter);
                                        }
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

                                private void loadEpisodes(SeasonModel item, int bindingAdapterPosition, GenericAdapter<SeasonModel, ItemSeasonBinding> adapter) {
                                    iptvSeriesDetails.loadSeason(item.getSeasonNumber());
                                    mSeason = bindingAdapterPosition;
                                    //mEpisode = 0;
                                    //iptvSeriesDetails.saveSeasonAndEpisode(mSeason, mEpisode);
                                    selectedPosition = item.getSeasonNumber();
                                    // adapter.notifyDataSetChanged();
                                }
                            });
                    getActivity().runOnUiThread(() -> {
                        mBinding.seasons.setAdapter(adapter);
                    });
                    adapter.totalItemsObservable
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(integer -> {
                                if (integer > 0) {
                                    mBinding.seasons.getViewTreeObserver()
                                            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                                @Override
                                                public void onGlobalLayout() {
                                                    mBinding.seasons.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                                    new Handler().postDelayed(() -> {
                                                        mBinding.seasons.setSelectedPosition(mSeason);
                                                        mBinding.seasons.scrollToPosition(mSeason);
                                                        adapter.handleSelection(mSeason);
                                                        mBinding.seasons.requestFocus();
                                                        iptvSeriesDetails.loadSeason(mSeason);
                                                    }, 1000);
                                                }
                                            });
                                }
                            })
                            .doOnError(throwable -> {
                                Log.e("SeriesDetailsFragment", "setupSeasons: ", throwable);
                            })
                            .subscribe();
                })
                .doOnError(throwable -> {
                    Log.e("SeriesDetailsFragment", "setupSeasons: ", throwable);
                })
                .subscribe();
    }
}
