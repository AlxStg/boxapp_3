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
import androidx.fragment.app.Fragment;

import com.example.boxapp3.databinding.FragmentTvBinding;
import com.example.boxapp3.databinding.ScrollTvCategoryItemBinding;
import com.example.boxapp3.databinding.ScrollTvChannelItemBinding;
import com.example.boxapp3.databinding.ScrollTvEpgItemBinding;
import com.example.boxapp3.listeners.activities.MainActivityListener;
import com.example.boxapp3.listeners.fragments.KeyListener;
import com.example.boxapp3.listeners.fragments.MainFragmentListener;
import com.example.boxapp3.models.fragments.TvFragmentModel;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.data.models.EpgDb;
import com.example.iptvsdk.data.models.xtream.Category;
import com.example.iptvsdk.data.models.xtream.StreamXc;
import com.example.iptvsdk.ui.live.IptvLive;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TvFragment extends Fragment implements KeyListener, MainFragmentListener {

    private FragmentTvBinding mBinding;
    private IptvLive iptvLive;
    private MainActivityListener listener;
    private TvFragmentModel mModel;
    private Handler epgHandler = new Handler();
    private Handler categoriesHandler = new Handler();
    private Runnable epgRunnable;
    private Runnable categoriesRunnable;

    public TvFragment(MainActivityListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTvBinding.inflate(inflater, container, false);
        iptvLive = new IptvLive(getContext());
        mModel = new TvFragmentModel();

        mBinding.setModel(mModel);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCategories();
        setupChannels();
    }

    private boolean loadedFirstEpg = false;

    private void setupChannels() {
        GenericAdapter<StreamXc, ScrollTvChannelItemBinding> adapter = new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<StreamXc, ScrollTvChannelItemBinding>() {
            @Override
            public ScrollTvChannelItemBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                return ScrollTvChannelItemBinding.inflate(inflater, parent, false);
            }

            @Override
            public Single<StreamXc> getItem(int position) {
                Single<StreamXc> channel = iptvLive.getChannels(position);
                if (position == 0 && !loadedFirstEpg) {
                    loadedFirstEpg = true;
                    channel.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError(th -> {
                                loadedFirstEpg = false;
                                Log.e("TAG", "getItem: ", th);
                            })
                            .doOnSuccess(streamXc -> {
                                loadEpg(streamXc);
                            })
                            .subscribe();
                }
                return channel;
            }

            @Override
            public Observable<Integer> getTotalItems() {
                return iptvLive.getTotalChannels();
            }

            @Override
            public void setModelToItem(ScrollTvChannelItemBinding binding, StreamXc item, int bindingAdapterPosition, GenericAdapter<StreamXc, ScrollTvChannelItemBinding> adapter) {
                binding.setModel(item);

                binding.getRoot().setOnFocusChangeListener((v, hasFocus) -> {
                    if (epgRunnable != null)
                        epgHandler.removeCallbacks(epgRunnable);
                    epgRunnable = () -> {
                        loadEpg(item);
                        Log.d("TAG", "setModelToItem: " + item.getTvArchiveDuration());
                    };
                    epgHandler.postDelayed(epgRunnable, 1000);
                });

                binding.getRoot().setOnClickListener(v -> {
                    listener.openDetails(item.getStreamId(), StreamXc.TYPE_STREAM_LIVE);
                });
            }
        });
        mBinding.include3.listChannels.setAdapter(adapter);
    }

    private void loadEpg(StreamXc stream) {
        GenericAdapter<EpgDb, ScrollTvEpgItemBinding> adapter =
                new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<EpgDb, ScrollTvEpgItemBinding>() {
                    @Override
                    public ScrollTvEpgItemBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                        return ScrollTvEpgItemBinding.inflate(inflater, parent, false);
                    }

                    @Override
                    public Single<EpgDb> getItem(int position) {
                        return iptvLive.getEpg(stream, position);
                    }

                    @Override
                    public Observable<Integer> getTotalItems() {

                        Observable<Integer> totalEpg = iptvLive.getTotalEpg(stream);

                        totalEpg
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(th -> Log.e("TAG", "getTotalItems: ", th))
                                .doOnNext(integer -> {
                                    if (integer == 0) {
                                        mModel.setShowNoEpgData(true);
                                    } else {
                                        mModel.setShowNoEpgData(false);
                                    }
                                })
                                .subscribe();

                        return totalEpg;
                    }

                    @Override
                    public void setModelToItem(ScrollTvEpgItemBinding binding, EpgDb item, int bindingAdapterPosition, GenericAdapter<EpgDb, ScrollTvEpgItemBinding> adapter) {
                        binding.setModel(item);
                        binding.setDaysPlayback(stream.getTvArchiveDuration());
                    }
                });
         getActivity().runOnUiThread(() -> mBinding.include4.listEpg.setAdapter(adapter));
    }

    private void setupCategories() {
        iptvLive.getCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("TAG", "setupCategories: ", th))
                .doOnSuccess(categories -> {
                    GenericAdapter<Category, ScrollTvCategoryItemBinding> adapter = new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<Category, ScrollTvCategoryItemBinding>() {
                        @Override
                        public ScrollTvCategoryItemBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                            return ScrollTvCategoryItemBinding.inflate(inflater, parent, false);
                        }

                        @Override
                        public Single<Category> getItem(int position) {
                            return Single.just(categories.get(position));
                        }

                        @Override
                        public Observable<Integer> getTotalItems() {
                            return Observable.just(categories.size());
                        }

                        @Override
                        public void setModelToItem(ScrollTvCategoryItemBinding binding, Category item, int bindingAdapterPosition, GenericAdapter<Category, ScrollTvCategoryItemBinding> adapter) {
                            binding.setModel(item);
                            binding.getRoot().setOnFocusChangeListener((v, hasFocus) -> {
                                if (categoriesRunnable != null)
                                    categoriesHandler.removeCallbacks(categoriesRunnable);
                                if (hasFocus) {
                                    categoriesRunnable = () -> iptvLive.setCategoryId(Integer.parseInt(item.getCategoryId()));
                                    categoriesHandler.postDelayed(categoriesRunnable, 1000);
                                }
                            });
                        }
                    });
                    mBinding.include2.listCategories.setAdapter(adapter);
                })
                .subscribe();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (mBinding.include3.listChannels.hasFocus()) {
                mBinding.include2.listCategories.requestFocus();
                return true;
            } else if (mBinding.include4.listEpg.hasFocus()) {
                mBinding.include3.listChannels.requestFocus();
                return true;
            }
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (mBinding.include2.listCategories.hasFocus()) {
                mBinding.include3.listChannels.requestFocus();
                return true;
            } else if (mBinding.include3.listChannels.hasFocus()) {
                mBinding.include4.listEpg.requestFocus();
                return true;
            }
        }
        return false;
    }

    @Override
    public View firstFocus() {
        return mBinding.include2.listCategories;
    }
}
