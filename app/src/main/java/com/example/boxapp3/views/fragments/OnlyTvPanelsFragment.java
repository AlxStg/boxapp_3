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

import com.example.boxapp3.BuildConfig;
import com.example.boxapp3.databinding.FragmentTvBinding;
import com.example.boxapp3.databinding.ScrollTvCategoryItemBinding;
import com.example.boxapp3.databinding.ScrollTvChannelItemBinding;
import com.example.boxapp3.databinding.ScrollTvEpgItemBinding;
import com.example.boxapp3.listeners.activities.OnlyTvActivityListener;
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

public class OnlyTvPanelsFragment extends Fragment implements KeyListener, MainFragmentListener {

    private FragmentTvBinding mBinding;
    private IptvLive mIptvLive;
    private OnlyTvActivityListener listener;
    private TvFragmentModel mModel;
    private Handler categoriesHandler = new Handler();
    private Runnable categoriesRunnable;

    public OnlyTvPanelsFragment(OnlyTvActivityListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTvBinding.inflate(inflater, container, false);
        mIptvLive = new IptvLive(getContext());
        mModel = new TvFragmentModel();

        mBinding.setModel(mModel);

        mModel.setShowEpg(false);
        mModel.setShowChannelOptions(false);


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
                Single<StreamXc> channel = mIptvLive.getChannels(position);
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
                return mIptvLive.getTotalChannels();
            }

            @Override
            public void setModelToItem(ScrollTvChannelItemBinding binding, StreamXc item, int bindingAdapterPosition, GenericAdapter<StreamXc, ScrollTvChannelItemBinding> adapter) {
                binding.setModel(item);
                mIptvLive.getActualEpg(item)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .doOnError(th -> Log.e("OnlyTvSearchFragment", "getItem: ", th))
                        .doOnSuccess(epgDb -> {
                            if(epgDb != null)
                                binding.setActualProgram(epgDb.getTitle());
                        })
                        .subscribe();

                binding.getRoot().setOnFocusChangeListener((v, hasFocus) -> {
                    loadEpg(item);
                    //if (epgRunnable != null)
                    //    epgHandler.removeCallbacks(epgRunnable);
                    //epgRunnable = () -> {
                    //    Log.d("TAG", "setModelToItem: " + item.getTvArchiveDuration());
                    //};
                    //epgHandler.postDelayed(epgRunnable, 1000);
                });

                binding.getRoot().setOnClickListener(v -> {
                 listener.playChannel(item);
                });

                binding.getRoot().setOnKeyListener((v, keyCode, event) -> {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_UP && bindingAdapterPosition == 0) {
                            listener.onGoToMenu();
                            return true;
                        }
                    }
                    return false;
                });
            }
        });
        mBinding.include3.listChannels.setAdapter(adapter);
    }

    private int epgAlreadyLoaded = -1;

    private void loadEpg(StreamXc stream) {
        if(epgAlreadyLoaded == stream.getStreamId()) return;

        epgAlreadyLoaded = stream.getStreamId();

        GenericAdapter<EpgDb, ScrollTvEpgItemBinding> adapter =
                new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<EpgDb, ScrollTvEpgItemBinding>() {
                    @Override
                    public ScrollTvEpgItemBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                        return ScrollTvEpgItemBinding.inflate(inflater, parent, false);
                    }

                    @Override
                    public Single<EpgDb> getItem(int position) {
                        return mIptvLive.getEpg(stream, position);
                    }

                    @Override
                    public Observable<Integer> getTotalItems() {

                        Observable<Integer> totalEpg = mIptvLive.getTotalEpg(stream);

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

                        binding.getRoot().setOnKeyListener((v, keyCode, event) -> {
                            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && bindingAdapterPosition == 0) {
                                    listener.onGoToMenu();
                                    return true;
                                }
                            }
                            return false;
                        });
                    }
                });
        getActivity().runOnUiThread(() -> mBinding.include4.listEpg.setAdapter(adapter));

        adapter.totalItemsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("TAG", "loadEpg: ", th))
                .doOnNext(integer -> {
                    if (integer > 0) {
                        mIptvLive.getActualEpgPosition(stream)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(th -> Log.e("TAG", "loadEpg: ", th))
                                .doOnSuccess(position -> {
                                    mBinding.include4.listEpg.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                        @Override
                                        public void onGlobalLayout() {
                                            mBinding.include4.listEpg.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                            mBinding.include4.listEpg.scrollToPosition(position);
                                            mBinding.include4.listEpg.setSelectedPosition(position);
                                        }
                                    });
                                })
                                .subscribe();
                    }
                })
                .subscribe();
    }

    private void setupCategories() {
        mIptvLive.getCategories(false)
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
                                    categoriesRunnable = () -> mIptvLive.setCategoryId(Integer.parseInt(item.getCategoryId()));
                                    categoriesHandler.postDelayed(categoriesRunnable, 1000);
                                }
                            });

                            binding.getRoot().setOnKeyListener((v, keyCode, event) -> {
                                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                    if(BuildConfig.FLAVOR.equals("boxApp4")) {
                                        if (keyCode == KeyEvent.KEYCODE_DPAD_UP && bindingAdapterPosition == 0) {
                                            listener.onGoToMenu();
                                            return true;
                                        }
                                    }
                                }
                                return false;
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
                showEpgLayout(false);
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
                showEpgLayout(true);
                mBinding.include3.listChannels.requestFocus();
                return true;
            }
        }
        return false;
    }

    private void showEpgLayout(boolean show) {
        mModel.setShowEpg(show);
        listener.onEpgVisibilityChanged(show);
    }

    @Override
    public View firstFocus() {
        return mBinding.include2.listCategories;
    }
}
