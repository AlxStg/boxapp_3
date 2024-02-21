package com.example.boxapp3.views.fragments.players.tv;

import android.content.SharedPreferences;
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
import androidx.fragment.app.FragmentActivity;

import com.example.boxapp3.R;
import com.example.boxapp3.databinding.FragmentTvPlayerPanelsBinding;
import com.example.boxapp3.databinding.ScrollTvCategoryItemBinding;
import com.example.boxapp3.databinding.ScrollTvChannelItemBinding;
import com.example.boxapp3.databinding.ScrollTvEpgItemBinding;
import com.example.boxapp3.listeners.activities.PlayerTvActivityListener;
import com.example.boxapp3.listeners.fragments.KeyListener;
import com.example.boxapp3.listeners.fragments.PlayerTvPanelsFragmentListener;
import com.example.boxapp3.models.fragments.TvFragmentModel;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.data.models.EpgDb;
import com.example.iptvsdk.data.models.xtream.Category;
import com.example.iptvsdk.data.models.xtream.StreamXc;
import com.example.iptvsdk.services.FavoritesService;
import com.example.iptvsdk.ui.live.IptvLive;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PlayerTvPanelsFragment extends Fragment implements KeyListener, PlayerTvPanelsFragmentListener {

    private FragmentTvPlayerPanelsBinding mBinding;
    private TvFragmentModel mModel;
    private IptvLive mIptvLive;
    private PlayerTvActivityListener mListener;
    private FavoritesService mFavoritesService;

    private SharedPreferences mSharedPreferences;

    private Handler epgHandler = new Handler();
    private Handler categoriesHandler = new Handler();
    private Runnable epgRunnable;
    private Runnable categoriesRunnable;
    private boolean firstCategoryLoaded = false;
    private boolean isAdult;


    public PlayerTvPanelsFragment(SharedPreferences sharedPreferences,
                                  PlayerTvActivityListener listener,
                                  IptvLive iptvLive, boolean isAdult) {
        mListener = listener;
        mIptvLive = iptvLive;
        mSharedPreferences = sharedPreferences;
        this.isAdult = isAdult;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTvPlayerPanelsBinding.inflate(inflater, container, false);

        mModel = new TvFragmentModel();
        mBinding.include.setModel(mModel);
        mBinding.include.include25.setListener(this);

        mFavoritesService = new FavoritesService(getContext());
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        setupCategories();
        setupChannels();
    }

    private void setupCategories() {
        mIptvLive.getCategories(isAdult)
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
                        public void setModelToItem(ScrollTvCategoryItemBinding binding,
                                                   Category item,
                                                   int bindingAdapterPosition,
                                                   GenericAdapter<Category, ScrollTvCategoryItemBinding> adapter) {
                            if(!firstCategoryLoaded){
                                firstCategoryLoaded = true;
                                mIptvLive.setCategoryId(Integer.parseInt(item.getCategoryId()));
                            }
                            binding.setModel(item);
                            binding.getRoot().setOnFocusChangeListener((v, hasFocus) -> {
                                if (categoriesRunnable != null)
                                    categoriesHandler.removeCallbacks(categoriesRunnable);
                                if (hasFocus) {
                                    categoriesRunnable = () -> mIptvLive.setCategoryId(Integer
                                            .parseInt(item.getCategoryId()));
                                    categoriesHandler.postDelayed(categoriesRunnable, 1000);

                                    if (item.getCategoryId() != null && !item.getCategoryId()
                                            .equals(mSharedPreferences.getString("lastCategoryId",
                                                    "nnnnnnnnnnnnnnnnnnnnnnnnnnnn"))) {
                                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                                        editor.putString("lastCategoryId", item.getCategoryId());
                                        editor.remove("lastChannelId");
                                        editor.apply();
                                    }
                                }
                            });
                        }
                    });
                    mBinding.include.include2.listCategories.setAdapter(adapter);
                    adapter.totalItemsObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError(th -> Log.e("TAG", "setupCategories: ", th))
                            .doOnNext(integer -> {
                                String lastCategoryId = mSharedPreferences.getString("lastCategoryId", null);
                                if (lastCategoryId != null) {
                                    getPositionCategory(lastCategoryId, categories)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnError(th -> Log.e("TAG", "setupCategories: ", th))
                                            .doOnSuccess(position -> {
                                                mBinding.include.include2.listCategories
                                                        .getViewTreeObserver()
                                                        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                                            @Override
                                                            public void onGlobalLayout() {
                                                                mBinding.include.include2.listCategories
                                                                        .getViewTreeObserver()
                                                                        .removeOnGlobalLayoutListener(this);
                                                                mBinding.include.include2.listCategories
                                                                        .scrollToPosition(position);
                                                                mBinding.include.include2.listCategories
                                                                        .setSelectedPosition(position);
                                                                mIptvLive.setCategoryId(Integer
                                                                        .parseInt(mSharedPreferences
                                                                                .getString("lastCategoryId",
                                                                                        "-1")));
                                                            }
                                                        });
                                            })
                                            .subscribe();
                                }
                            })
                            .subscribe();
                })
                .subscribe();
    }

    private Single<Integer> getPositionCategory(String lastCategoryId, List<Category> categories) {
        return Single.create(emitter -> {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getCategoryId().equals(lastCategoryId)) {
                    emitter.onSuccess(i);
                    return;
                }
            }
            emitter.onSuccess(0);
        });
    }

    private StreamXc actualStream;
    private Handler channelOptionsHandler = new Handler();
    private Runnable channelOptionsRunnable;

    private void setupChannels() {
        GenericAdapter<StreamXc, ScrollTvChannelItemBinding> adapter = new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<StreamXc, ScrollTvChannelItemBinding>() {
            @Override
            public ScrollTvChannelItemBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                return ScrollTvChannelItemBinding.inflate(inflater, parent, false);
            }

            @Override
            public Single<StreamXc> getItem(int position) {
                Single<StreamXc> channel = mIptvLive.getChannels(position);
                //if (position == 0 && !loadedFirstEpg) {
                //    loadedFirstEpg = true;
                //    channel.subscribeOn(Schedulers.io())
                //            .observeOn(AndroidSchedulers.mainThread())
                //            .doOnError(th -> {
                //                loadedFirstEpg = false;
                //                Log.e("TAG", "getItem: ", th);
                //            })
                //            .doOnSuccess(streamXc -> {
                //                loadEpg(streamXc);
                //            })
                //            .subscribe();
                //}
                return channel;
            }

            @Override
            public Observable<Integer> getTotalItems() {
                return mIptvLive.getTotalChannels();
            }

            @Override
            public void setModelToItem(ScrollTvChannelItemBinding binding,
                                       StreamXc item,
                                       int bindingAdapterPosition,
                                       GenericAdapter<StreamXc, ScrollTvChannelItemBinding> adapter) {
                binding.setModel(item);

                binding.getRoot().setOnFocusChangeListener((v, hasFocus) -> {
                   actualStream = item;
                   if(channelOptionsRunnable != null) {
                        channelOptionsHandler.removeCallbacks(channelOptionsRunnable);
                    }
                    if (hasFocus) {
                        channelOptionsRunnable = () -> {
                            mModel.setShowEpg(false);
                            mModel.setShowChannelOptions(true);
                            mFavoritesService.isFavorite(item)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnError(th -> Log.e("TAG", "setModelToItem: ", th))
                                    .doOnSuccess(isFav -> mBinding.include.include25.setIsFavorite(isFav))
                                    .subscribe();
                        };
                        channelOptionsHandler.postDelayed(channelOptionsRunnable, 1000);
                    }
                });
                binding.getRoot().setOnClickListener(v -> {
                    mListener.onChannelClick(item);
                    mSharedPreferences.edit().putInt("lastChannelId", bindingAdapterPosition).apply();
                });
                binding.getRoot().setOnLongClickListener(v -> {
                    mFavoritesService.toggleFavorite(item)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.computation())
                            .doOnError(th -> Log.e("TAG", "setModelToItem: ", th))
                            .doOnSuccess(item::setFavorite)
                            .subscribe();
                    return true;
                });
            }
        });


        mBinding.include.include3.listChannels.setAdapter(adapter);
        selectChannelPosition(mSharedPreferences.getInt("lastChannelId", 0));
    }

    private void selectChannelPosition(int position) {
        if (mBinding.include.include3.listChannels.getAdapter() == null) return;
        ((GenericAdapter<?, ?>) mBinding.include.include3.listChannels.getAdapter()).totalItemsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(count -> {
                    if (count > 0) {
                        mBinding.include.include3.listChannels.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                mBinding.include.include3.listChannels.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                mBinding.include.include3.listChannels.scrollToPosition(position);
                                mBinding.include.include3.listChannels.setSelectedPosition(position);
                            }
                        });
                    }
                })
                .subscribe();

    }

    private int epgAlreadyLoaded = -1;

    private void loadEpg(StreamXc stream) {
        if (epgAlreadyLoaded == stream.getStreamId()) return;

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

                        //totalEpg
                        //        .subscribeOn(Schedulers.io())
                        //        .observeOn(AndroidSchedulers.mainThread())
                        //        .doOnError(th -> Log.e("TAG", "getTotalItems: ", th))
                        //        .doOnNext(integer -> {
                        //            if (integer == 0) {
                        //                mModel.setShowNoEpgData(true);
                        //            } else {
                        //                mModel.setShowNoEpgData(false);
                        //            }
                        //        })
                        //        .subscribe();

                        return totalEpg;
                    }

                    @Override
                    public void setModelToItem(ScrollTvEpgItemBinding binding,
                                               EpgDb item,
                                               int bindingAdapterPosition,
                                               GenericAdapter<EpgDb, ScrollTvEpgItemBinding> adapter) {
                        binding.setModel(item);
                        binding.setDaysPlayback(stream.getTvArchiveDuration());
                        binding.getRoot().setOnClickListener(v -> {
                            mListener.onEpgClick(item, stream.getTvArchiveDuration());
                        });

                        Single.<Boolean>create(emitter -> {
                                    Date now = new Date();
                                    emitter.onSuccess(item.getStart().before(now) && item.getEnd().after(now));
                                })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(th -> Log.e("TAG", "setModelToItem: ", th))
                                .doOnSuccess(isLive -> {
                                    if (isLive) {
                                        binding.textView44.setText(R.string.live);
                                        binding.textView44.setTextColor(getResources()
                                                .getColor(R.color.live_epg_text));
                                        binding.textView44.setVisibility(View.VISIBLE);
                                    }
                                })
                                .subscribe();
                    }
                });
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> mBinding.include.include4.listEpg.setAdapter(adapter));

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
                                    mBinding.include.include4.listEpg.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                        @Override
                                        public void onGlobalLayout() {
                                            mBinding.include.include4.listEpg.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                            mBinding.include.include4.listEpg.scrollToPosition(position);
                                            mBinding.include.include4.listEpg.setSelectedPosition(position);
                                        }
                                    });
                                })
                                .subscribe();
                    }
                })
                .subscribe();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
        //    if (mBinding.include.include3.listChannels.hasFocus()) {
        //        mBinding.include.include2.listCategories.requestFocus();
        //        return true;
        //    } else if (mBinding.include.include4.listEpg.hasFocus()) {
        //        mBinding.include.include3.listChannels.requestFocus();
        //        return true;
        //    }
        //} else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
        //    if (mBinding.include.include2.listCategories.hasFocus()) {
        //        mBinding.include.include3.listChannels.requestFocus();
        //        return true;
        //    } else if (mBinding.include.include3.listChannels.hasFocus()) {
        //        mBinding.include.include4.listEpg.requestFocus();
        //        return true;
        //    }
        //}
        return false;
    }

    @Override
    public void onChannelOptionsPlay() {
        mListener.onChannelClick(actualStream);
    }

    @Override
    public void onChannelOptionsFavorite() {
        mBinding.include.include25.setIsFavorite(!mBinding.include.include25.getIsFavorite());
        mFavoritesService.toggleFavorite(actualStream)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .doOnError(th -> Log.e("TAG", "onChannelOptionsFavorite: ", th))
                .doOnSuccess(actualStream::setFavorite)
                .subscribe();
    }

    @Override
    public void onChannelOptionsEpg() {
        mModel.setShowEpg(true);
        mModel.setShowChannelOptions(false);
        loadEpg(actualStream);
    }
}
