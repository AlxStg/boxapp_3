package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.example.boxapp3.R;
import com.example.boxapp3.databinding.FragmentHomeBinding;
import com.example.boxapp3.databinding.HomeSliderBinding;
import com.example.boxapp3.databinding.ThumbChannelBinding;
import com.example.boxapp3.databinding.ThumbMovieBinding;
import com.example.boxapp3.listeners.activities.MainActivityListener;
import com.example.boxapp3.models.fragments.HomeFragmentModel;
import com.example.iptvsdk.IptvApplication;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.data.models.xtream.Info;
import com.example.iptvsdk.data.models.xtream.Info_;
import com.example.iptvsdk.data.models.xtream.StreamXc;
import com.example.iptvsdk.ui.home.IptvHome;
import com.example.iptvsdk.ui.home.listener.IptvHomeListener;
import com.example.iptvsdk.ui.home.models.IptvHomeStreamsModel;
import com.example.iptvsdk.ui.slider.vod.SliderVod;
import com.example.iptvsdk.ui.slider.vod.SliderVodListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding mBinding;
    private HomeFragmentModel mModel;
    private MainActivityListener mMainActivityListener;
    private IptvHome mIptvHome;

    public HomeFragment(MainActivityListener mainActivityListener) {
        mMainActivityListener = mainActivityListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_home,
                container,
                false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mModel = new HomeFragmentModel();
        mBinding.setModel(mModel);

        setupContent();
        setupSliderMovies();
    }

    private void setupContent() {
        mIptvHome = new IptvHome(((IptvApplication) getActivity().getApplication()),
                getContext(),
                mModel,
                new IptvHomeListener() {
                    @Override
                    public void onChannelClick(int id) {

                    }

                    @Override
                    public void showChannels(List<IptvHomeStreamsModel> streams) {
                        GenericAdapter<IptvHomeStreamsModel, ThumbChannelBinding> adapter =
                                new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<IptvHomeStreamsModel, ThumbChannelBinding>() {
                                    @Override
                                    public ThumbChannelBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                                        return DataBindingUtil.inflate(inflater, R.layout.thumb_channel, parent, false);
                                    }

                                    @Override
                                    public Single<IptvHomeStreamsModel> getItem(int position) {
                                        return Single.just(streams.get(position));
                                    }

                                    @Override
                                    public Observable<Integer> getTotalItems() {
                                        return Observable.just(streams.size());
                                    }

                                    @Override
                                    public void setModelToItem(ThumbChannelBinding binding,
                                                               IptvHomeStreamsModel item,
                                                               int bindingAdapterPosition,
                                                               GenericAdapter<IptvHomeStreamsModel, ThumbChannelBinding> adapter) {
                                        binding.setModel(item);
                                    }
                                });
                        getActivity().runOnUiThread(() -> {
                            mBinding.include2.channelsFavoriteVgv.setAdapter(adapter);
                        });
                    }

                    @Override
                    public void showVod(List<IptvHomeStreamsModel> streams) {
                        GenericAdapter<IptvHomeStreamsModel, ThumbMovieBinding> adapter =
                                new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<IptvHomeStreamsModel, ThumbMovieBinding>() {
                                    @Override
                                    public ThumbMovieBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                                        return DataBindingUtil.inflate(inflater, R.layout.thumb_movie, parent, false);
                                    }

                                    @Override
                                    public Single<IptvHomeStreamsModel> getItem(int position) {
                                        return Single.just(streams.get(position));
                                    }

                                    @Override
                                    public Observable<Integer> getTotalItems() {
                                        return Observable.just(streams.size());
                                    }

                                    @Override
                                    public void setModelToItem(ThumbMovieBinding binding,
                                                               IptvHomeStreamsModel item,
                                                               int bindingAdapterPosition,
                                                               GenericAdapter<IptvHomeStreamsModel, ThumbMovieBinding> adapter) {
                                        binding.setModel(item);
                                        binding.getRoot().setOnKeyListener(new View.OnKeyListener() {
                                            @Override
                                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN && bindingAdapterPosition == 0) {
                                                    mBinding.include2.channelsFavoriteVgv.requestFocus();
                                                    return true;
                                                }
                                                return false;
                                            }
                                        });
                                        binding.getRoot().setOnClickListener(v -> {
                                            mMainActivityListener.openDetails(item.getId(), item.getType());
                                        });
                                    }
                                });
                        getActivity().runOnUiThread(() -> {
                            mBinding.include.vodFacvritesHgv.setAdapter(adapter);
                        });
                    }
                });

        mIptvHome.loadChannelsFavorites();
        mIptvHome.loadVodFavorites(true);
    }

    private List<Info_> moviesInfo = new ArrayList<>();
    private Handler moviesInfoHandler = new Handler();
    private Runnable moviesInfoRunnable;

    private void setupSliderMovies() {
        List<HomeSliderBinding> sliderBindings = generateSliderBinding();
        AtomicInteger position = new AtomicInteger();
        new SliderVod(getContext(), mBinding.slider, new SliderVodListener() {
            @Override
            public View getItem(StreamXc stream, SliderVod sliderVod) {
                if (moviesInfoRunnable != null) {
                    moviesInfoHandler.removeCallbacks(moviesInfoRunnable);
                }
                HomeSliderBinding mainHomeSliderBinding = sliderBindings.get(position.get());
                if (moviesInfo.size() > 0) {
                    Optional<Info_> optInfo_ = Stream.of(moviesInfo).filter(x -> x.getStreamId() == stream
                            .getStreamId()).findFirst();
                    if (optInfo_.isPresent()) {
                        Info info = new Info();
                        info.setInfo(optInfo_.get());
                        stream.setInfo(info);
                        mainHomeSliderBinding.setModel(stream);
                    }
                } else {
                    sliderVod.getMovieInfo(stream.getStreamId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSuccess(info_ -> {
                                moviesInfo.add(info_);
                                Info info = new Info();
                                info.setInfo(info_);
                                stream.setInfo(info);
                                mainHomeSliderBinding.setModel(stream);
                            })
                            .doOnError(throwable -> {
                                Log.e("SliderVod", "Error to get movie info", throwable);
                                moviesInfoHandler.postDelayed(moviesInfoRunnable, 1000);
                            })
                            .subscribe();
                }
                mainHomeSliderBinding.setModel(stream);
                //mainHomeSliderBinding.sliderButtonPlay.setOnClickListener(v -> {
                //    openDetails(stream.getStreamType(), stream.getStreamId());
                //});
                //mainHomeSliderBinding.sliderButtonPlay.setOnFocusChangeListener((v, hasFocus) -> {
                //    if (hasFocus) {
                //        sliderVod.stopAutomaticSlide();
                //    } else {
                //        sliderVod.automaticSlide();
                //    }
                //});
                mainHomeSliderBinding.btnPlayMovie.setOnKeyListener((v, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        mBinding.include.vodFacvritesHgv.requestFocus();
                        return true;
                    }
                    return false;
                });
                mainHomeSliderBinding.favIcon.setOnKeyListener((v, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        mBinding.include.vodFacvritesHgv.requestFocus();
                        return true;
                    }
                    return false;
                });

                sliderBindings.set(position.get(), mainHomeSliderBinding);
                position.getAndIncrement();
                return mainHomeSliderBinding.getRoot();
            }
        });
    }

    private List<HomeSliderBinding> generateSliderBinding() {
        List<HomeSliderBinding> mainHomeSliderBindings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            HomeSliderBinding mainHomeSliderBinding = DataBindingUtil.inflate(LayoutInflater
                    .from(getContext()), R.layout.home_slider, mBinding.slider, false);
            mainHomeSliderBinding.setModel(null);
            mainHomeSliderBindings.add(mainHomeSliderBinding);
        }
        return mainHomeSliderBindings;
    }
}
