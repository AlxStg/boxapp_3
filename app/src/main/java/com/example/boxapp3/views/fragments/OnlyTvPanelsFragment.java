package com.example.boxapp3.views.fragments;

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

import com.example.boxapp3.BuildConfig;
import com.example.boxapp3.databinding.EpgItemDateBinding;
import com.example.boxapp3.databinding.FragmentTvBinding;
import com.example.boxapp3.databinding.ScrollTvCategoryItemBinding;
import com.example.boxapp3.databinding.ScrollTvChannelItemBinding;
import com.example.boxapp3.databinding.ScrollTvEpgItemBinding;
import com.example.boxapp3.listeners.activities.OnlyTvActivityListener;
import com.example.boxapp3.listeners.fragments.KeyListener;
import com.example.boxapp3.listeners.fragments.OnlyTvPanelsFragmentListener;
import com.example.boxapp3.listeners.models.fragments.TvFragmentModelListener;
import com.example.boxapp3.models.adapters.ItemEpgDateModel;
import com.example.boxapp3.models.fragments.TvFragmentModel;
import com.example.boxapp3.models.fragments.players.tv.EpgPanelModel;
import com.example.boxapp3.service.ReminderIntentService;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.data.models.EpgDb;
import com.example.iptvsdk.data.models.Reminders;
import com.example.iptvsdk.data.models.xtream.Category;
import com.example.iptvsdk.data.models.xtream.StreamXc;
import com.example.iptvsdk.ui.favorite.IptvFavorite;
import com.example.iptvsdk.ui.live.IptvLive;
import com.example.iptvsdk.ui.reminder.IptvReminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class OnlyTvPanelsFragment extends Fragment implements KeyListener, OnlyTvPanelsFragmentListener {

    private FragmentTvBinding mBinding;
    private IptvLive mIptvLive;
    private IptvReminder mIptvReminder;
    private IptvFavorite mIptvFavorite;
    private OnlyTvActivityListener listener;
    private TvFragmentModel mModel;
    private Handler categoriesHandler = new Handler();
    private Runnable categoriesRunnable;
    private String epgChannelId = "";
    private SharedPreferences mSharedPreferences;
    private boolean showEpg = false;

    public OnlyTvPanelsFragment(OnlyTvActivityListener listener, IptvLive iptvLive) {
        this.listener = listener;
        this.mIptvLive = iptvLive;
    }

    public OnlyTvPanelsFragment(OnlyTvActivityListener listener, IptvLive iptvLive, boolean showEpg) {
        this.listener = listener;
        this.mIptvLive = iptvLive;
        this.showEpg = showEpg;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTvBinding.inflate(inflater, container, false);
        mIptvReminder = new IptvReminder(getContext());
        mIptvFavorite = new IptvFavorite(getContext());
        mModel = new TvFragmentModel(new TvFragmentModelListener() {
            @Override
            public void onShowEpg(boolean showEpg) {
//                changeStartConstraintChannel(showEpg);
            }
        });

        mBinding.setModel(mModel);

        mModel.setShowEpg(false);
        mModel.setShowChannelOptions(false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSharedPreferences = getContext().getSharedPreferences("app", 0);
        setupCategories();
        setupChannels();

        listener.onChangeMenuEnabled(false);

        mBinding.include3.listChannels.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mBinding.include3.listChannels.getAdapter() != null && mBinding.include3.listChannels.getAdapter().getItemCount() > 0)
                    mBinding.include3.listChannels.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                mBinding.include3.listChannels.requestFocus();
            }
        });
    }

    private boolean loadedFirstEpg = false;
    private final Handler loadEpgHandler = new Handler();
    private Runnable loadEpgRunnable;

    private void setupChannels() {
        GenericAdapter<StreamXc, ScrollTvChannelItemBinding> adapter = new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<StreamXc, ScrollTvChannelItemBinding>() {
            @Override
            public ScrollTvChannelItemBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                return ScrollTvChannelItemBinding.inflate(inflater, parent, false);
            }

            @Override
            public Single<StreamXc> getItem(int position) {
                Single<StreamXc> channel = mIptvLive.getChannels(position);
                if (position == 0) {
                    //selectChannelPosition();
                    if (!loadedFirstEpg) {
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
                }
                return channel;
            }

            @Override
            public Observable<Integer> getTotalItems() {
                return mIptvLive.getTotalChannels();
            }

            @Override
            public void setModelToItem(ScrollTvChannelItemBinding binding, StreamXc item,
                                       int bindingAdapterPosition,
                                       GenericAdapter<StreamXc, ScrollTvChannelItemBinding> adapter) {
                Log.d("TAG", "position: " + bindingAdapterPosition);
                binding.setModel(item);
                mIptvLive.getActualEpg(item)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .doOnError(th -> Log.e("OnlyTvSearchFragment", "getItem: ", th))
                        .doOnSuccess(epgDb -> {
                            if (epgDb != null)
                                binding.setActualProgram(epgDb.getTitle());
                        })
                        .subscribe();

                binding.getRoot().setOnFocusChangeListener((v, hasFocus) -> {
                    mBinding.include4.setModel(new EpgPanelModel());
                    binding.setFocused(hasFocus);
                    if (loadEpgRunnable != null)
                        loadEpgHandler.removeCallbacks(loadEpgRunnable);

                    loadEpgRunnable = () -> loadEpg(item);

                    loadEpgHandler.postDelayed(loadEpgRunnable, 1000);
                });

                binding.getRoot().setOnClickListener(v -> {
                    listener.playChannel(item.getStreamId());
                    listener.onZapStreamChanged(item);
                    if (!item.isAdult())
                        mSharedPreferences.edit().putInt("streamId", item.getStreamId()).apply();
                    saveListPostion("listChannels", bindingAdapterPosition, item.getName());
                });

                binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        binding.getModel().setFavorite(!binding.getModel().isFavorite());
                        mIptvFavorite.toggleFavorite(item)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(th -> Log.e("TAG", "onLongClick: ", th))
                                .doOnSuccess(aBoolean -> {
                                    binding.getModel().setFavorite(aBoolean);
                                })
                                .subscribe();
                        return true;
                    }
                });

                binding.getRoot().setOnKeyListener((v, keyCode, event) -> {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                      if (keyCode == KeyEvent.KEYCODE_DPAD_UP && bindingAdapterPosition == 0
                      && BuildConfig.FLAVOR.equals("tiger1")) {
                          listener.onGoToChannelTopBar();
                          return true;
                      }
                        //if (keyCode == KeyEvent.KEYCODE_DPAD_UP && bindingAdapterPosition == 0) {
                        //    listener.onGoToMenu();
                        //    return true;
                        //}
                    }
                    return false;
                });
            }
        });
        mBinding.include3.listChannels.setAdapter(adapter);

        selectChannelPosition();
        mIptvLive.getTotalChannels()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("TAG", "setupChannels: ", th))
                .doOnNext(i -> selectChannelPosition())
                .subscribe();
    }

    private void selectChannelPosition() {
        int streamId = mSharedPreferences.getInt("streamId", -1);
        if (streamId != -1) {
            mIptvLive.getPositionByStreamId(streamId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(th -> Log.e("TAG", "setupChannels: ", th))
                    .doOnSuccess(position -> {
                        if (position == -1) {
                            selectFirstChannel(-1, streamId);
                            return;
                        }
                        Log.d("TAG", "selectChannelPosition: " + position);
                        selectFirstChannel(position, streamId);
                    })
                    .subscribe();
        } else {
            selectFirstChannel(-1, streamId);
        }
    }

    private void selectFirstChannel(int position, int streamId) {
        boolean selectPosition = position != -1;
        if (!selectPosition)
            position = 0;
        mBinding.include3.listChannels.scrollToPosition(position);
        mBinding.include3.listChannels.setSelectedPosition(position);
        if (selectPosition)
            ((GenericAdapter<StreamXc, ScrollTvChannelItemBinding>) mBinding.include3.listChannels.getAdapter()).handleSelection(position);
        openEpgIfRequested(streamId);

        int finalPosition = position;
        mBinding.include3.listChannels.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.include3.listChannels.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mBinding.include3.listChannels.scrollToPosition(finalPosition);
                mBinding.include3.listChannels.setSelectedPosition(finalPosition);
                if (selectPosition)
                    ((GenericAdapter<StreamXc, ScrollTvChannelItemBinding>) mBinding.include3.listChannels.getAdapter()).handleSelection(finalPosition);
                openEpgIfRequested(streamId);
            }
        });
    }

    private void openEpgIfRequested(int streamId) {
        if (showEpg) {
            mIptvLive.getChannel(streamId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(th -> Log.e("TAG", "openEpgIfRequested: ", th))
                    .doOnSuccess(streamXc -> {
                        loadEpg(streamXc);
                        showEpgLayout(true);
                        mBinding.include4.listEpg.requestFocus();
                        showEpg = false;
                    })
                    .subscribe();
        }
    }

    private int epgAlreadyLoaded = -1;

    private void loadEpg(StreamXc stream) {
        if (stream == null || epgAlreadyLoaded == stream.getStreamId()) return;


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
                                    mModel.setShowNoEpgData(integer == 0);
                                })
                                .subscribe();

                        return totalEpg;
                    }

                    @Override
                    public void setModelToItem(ScrollTvEpgItemBinding binding, EpgDb item, int bindingAdapterPosition, GenericAdapter<EpgDb, ScrollTvEpgItemBinding> adapter) {
                        if (item == null) return;
                        binding.setModel(item);
                        binding.setDaysPlayback(stream.getTvArchiveDuration());

                        mIptvReminder.getReminder(item.getTitle(), item.getStart().getTime())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(th -> Log.e("TAG", "loadEpg: ", th))
                                .doOnSuccess(reminders -> {
                                    binding.getModel().setHasReminder(reminders != null &&
                                            reminders.isActive());
                                })
                                .subscribe();

                        binding.getRoot().setOnKeyListener((v, keyCode, event) -> {
                            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && bindingAdapterPosition == 0) {
                                    listener.onGoToMenu();
                                    return true;
                                }
                            }
                            return false;
                        });

                        binding.getRoot().setOnFocusChangeListener((v, hasFocus) -> {
                            if (hasFocus) {
                                selectTodayEpgDateList(item.getStart());
                                showEpgInfo(item);
                            }
                        });

                        binding.getRoot().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (item.isPlayback(stream.getTvArchiveDuration())) {
                                    listener.onPlayback(stream.getStreamId(), item.getStart());
                                    return;
                                }

                                if (item.endAfterNow())
                                    mIptvReminder.getReminder(item.getTitle(), item.getStart().getTime())
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnError(th -> {
                                                Log.e("TAG", "loadEpg: ", th);
                                                toggleFavortes(null, binding, stream, item);
                                            })
                                            .doOnSuccess(reminders -> {
                                                toggleFavortes(reminders, binding, stream, item);
                                            })
                                            .subscribe();
                            }
                        });
                    }
                });
        FragmentActivity activity = getActivity();
        if (activity != null)
            activity.runOnUiThread(() -> mBinding.include4.listEpg.setAdapter(adapter));

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
                                            showEpgInfo(stream, position);
                                        }
                                    });
                                })
                                .subscribe();
                    }
                })
                .subscribe();
        setupListDays(stream.getEpgChannelId());
    }

    private void toggleFavortes(Reminders reminders, ScrollTvEpgItemBinding binding,
                                StreamXc stream, EpgDb item) {
        binding.getModel().setHasReminder(reminders == null
                || !reminders.isActive());
        mIptvReminder.addReminderProgramme(ReminderIntentService.class,
                stream.getStreamId(),
                stream.getStreamIcon(),
                item.getTitle(),
                item.getStart().getTime(),
                item.getEnd().getTime(),
                reminders == null || !reminders.isActive());
    }

    private void showEpgInfo(StreamXc stream, int position) {
        mIptvLive.getEpg(stream, position)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("TAG", "showEpgInfo: ", th))
                .doOnSuccess(this::showEpgInfo)
                .subscribe();
    }

    private void showEpgInfo(EpgDb epg) {
        EpgPanelModel model = new EpgPanelModel(epg.getStart(), epg.getEnd(), epg.getTitle(), epg.getDescription());
        mBinding.include4.setModel(model);
    }

    private void setupListDays(String epgChannelId) {
        this.epgChannelId = epgChannelId;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        mIptvLive.getDatesEpg(epgChannelId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("TAG", "setupListDays: ", th))
                .doOnSuccess(dates -> {
                    GenericAdapter<ItemEpgDateModel, EpgItemDateBinding> adapter =
                            new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<ItemEpgDateModel, EpgItemDateBinding>() {
                                @Override
                                public EpgItemDateBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                                    return EpgItemDateBinding.inflate(inflater, parent, false);
                                }

                                @Override
                                public Single<ItemEpgDateModel> getItem(int position) {
                                    try {
                                        Date date = sdf.parse(dates.get(position));
                                        return Single.just(new ItemEpgDateModel(date));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return Single.just(new ItemEpgDateModel(new Date()));
                                }

                                @Override
                                public Observable<Integer> getTotalItems() {
                                    return dates.size() > 0 ? Observable.just(dates.size()) : Observable.just(0);
                                }

                                @Override
                                public void setModelToItem(EpgItemDateBinding binding, ItemEpgDateModel item, int bindingAdapterPosition, GenericAdapter<ItemEpgDateModel, EpgItemDateBinding> adapter) {
                                    binding.setModel(item);
                                    binding.getRoot().setOnFocusChangeListener((v, hasFocus) -> {
                                        if (hasFocus) {
                                            try {
                                                selectTodayEpgDateList(sdf.parse(dates.get(bindingAdapterPosition)));
                                            } catch (ParseException e) {
                                                Log.e("TAG", "setModelToItem: ", e);
                                            }
                                        }
                                    });
                                }
                            });
                    mBinding.include4.vgListEpgDate.setAdapter(adapter);
                    selectTodayEpgDateList(new Date());

                })
                .subscribe();
    }

    private void selectTodayEpgDateList(Date date) {
        Log.d("TAG", "selectTodayEpgDateList: " + date);
        Single.<Integer>create(emitter -> {
                    mIptvLive.getDatesEpg(epgChannelId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError(th -> Log.e("TAG", "setupListDays: ", th))
                            .doOnSuccess(dates -> {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                                String today = sdf.format(date);
                                Log.d("TAG", "selectTodayEpgDateList: " + today);
                                for (int i = 0; i < dates.size(); i++) {
                                    if (sdf.format(dates.get(i)).equals(today)) {
                                        emitter.onSuccess(i);
                                        return;
                                    }
                                }

                                emitter.onSuccess(dates.size() == 0 ? 0 : dates.size() - 1);
                            })
                            .subscribe();
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("TAG", "selectTodayEpgDateList: ", th))
                .doOnSuccess(position -> {
                    Log.d("TAG", "selectTodayEpgDateList: " + position);
                    mBinding.include4.vgListEpgDate.scrollToPosition(position);
                    mBinding.include4.vgListEpgDate.setSelectedPosition(position);
                    mBinding.include4.vgListEpgDate.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mBinding.include4.vgListEpgDate.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            mBinding.include4.vgListEpgDate.scrollToPosition(position);
                            mBinding.include4.vgListEpgDate.setSelectedPosition(position);
                        }
                    });
                    ((GenericAdapter<ItemEpgDateModel, EpgItemDateBinding>) mBinding.include4.vgListEpgDate.getAdapter()).handleSelection(position);
                })
                .subscribe();
    }

    private int mSelectedCategoryId = -1;

    private void setupCategories() {
        mIptvLive.getAllCategories()
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
                                    categoriesRunnable = () -> {
                                        listener.onCategorySelected(item);
                                        mSelectedCategoryId = Integer.parseInt(item.getCategoryId());
                                        if (!item.isAdult())
                                            mIptvLive.setCategoryId(mSelectedCategoryId);
                                        mBinding.include2.listCategories.setSelectedPosition(bindingAdapterPosition);
                                        saveListPostion("listCategories", bindingAdapterPosition, item.getCategoryName());
                                        adapter.handleSelection(bindingAdapterPosition);
                                    };
                                    categoriesHandler.postDelayed(categoriesRunnable, 1000);
                                }
                            });

                            binding.getRoot().setOnKeyListener((v, keyCode, event) -> {
                                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                    if (BuildConfig.FLAVOR.equals("boxApp4")) {
                                        if (keyCode == KeyEvent.KEYCODE_DPAD_UP && bindingAdapterPosition == 0) {
                                            listener.onGoToMenu();
                                            return true;
                                        }
                                    } else if(BuildConfig.ONLY_TV_MENU_TOP) {
                                        if (keyCode == KeyEvent.KEYCODE_DPAD_UP && bindingAdapterPosition == 0) {
                                            listener.onGoToChannelTopBar();
                                            return true;
                                        }
                                    }
                                }
                                return false;
                            });
                        }
                    });
                    mBinding.include2.listCategories.setAdapter(adapter);
                    mBinding.include2.listCategories.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mBinding.include2.listCategories.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            if (getListPosition("listCategories") != -1) {
                                mIptvLive.isCategoryAdultByName(getLastItem("listCategories"))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .doOnError(th -> Log.e("TAG", "setupCategories: ", th))
                                        .doOnSuccess(isAdult -> {
                                            if (isAdult) {
                                                if (isLimitAdult()) {
                                                    mIptvLive.setCategoryByName(getLastItem("listCategories"));
                                                    mBinding.include2.listCategories.scrollToPosition(getListPosition("listCategories"));
                                                    mBinding.include2.listCategories.setSelectedPosition(getListPosition("listCategories"));
                                                    ((GenericAdapter<Category, ScrollTvCategoryItemBinding>) mBinding.include2.listCategories.getAdapter()).handleSelection(getListPosition("listCategories"));
                                                } else {
                                                    mIptvLive.setCategoryByName(getContext().getString(com.example.iptvsdk.R.string.all));
                                                    mBinding.include2.listCategories.scrollToPosition(getListPosition("listCategories"));
                                                    mBinding.include2.listCategories.setSelectedPosition(getListPosition("listCategories"));
                                                    ((GenericAdapter<Category, ScrollTvCategoryItemBinding>) mBinding.include2.listCategories.getAdapter()).handleSelection(getListPosition("listCategories"));
                                                }
                                            } else {
                                                mIptvLive.setCategoryByName(getLastItem("listCategories"));
                                                mBinding.include2.listCategories.scrollToPosition(getListPosition("listCategories"));
                                                mBinding.include2.listCategories.setSelectedPosition(getListPosition("listCategories"));
                                                ((GenericAdapter<Category, ScrollTvCategoryItemBinding>) mBinding.include2.listCategories.getAdapter()).handleSelection(getListPosition("listCategories"));
                                            }
                                        })
                                        .subscribe();
                            } else {

                                mBinding.include2.listCategories.scrollToPosition(0);
                                mBinding.include2.listCategories.setSelectedPosition(0);
                            }
                        }
                    });
                })
                .subscribe();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (mBinding.include3.listChannels.hasFocus() && mModel.isShowEpg()) {
                showEpgLayout(false);
                mBinding.include3.listChannels.requestFocus();
                return true;
            } else if (mBinding.include3.listChannels.hasFocus() && !mModel.isShowEpg()) {
                mBinding.include2.listCategories.requestFocus();
                return true;
            } else if (mBinding.include4.listEpg.hasFocus()) {
                mBinding.include4.vgListEpgDate.requestFocus();
                return true;
            } else if (mBinding.include4.vgListEpgDate.hasFocus()) {
                mBinding.include3.listChannels.requestFocus();
                return true;
            }
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (mBinding.include2.listCategories.hasFocus()) {
                mBinding.include3.listChannels.requestFocus();
                return true;
            } else if (mBinding.include3.listChannels.hasFocus() && !mModel.isShowEpg()) {
                showEpgLayout(true);
                mBinding.include3.listChannels.requestFocus();
                return true;
            } else if (mBinding.include3.listChannels.hasFocus() && mModel.isShowEpg()) {
                mBinding.include4.vgListEpgDate.requestFocus();
                return true;

            } else if (mBinding.include4.vgListEpgDate.hasFocus()) {
                mBinding.include4.listEpg.requestFocus();
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

    @Override
    public void onCategorySelected(int categoryId) {
        mIptvLive.setCategoryId(categoryId);
    }

    private void saveListPostion(String list, int position, String itemName) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(list, position);
        editor.putString(list + "lastItem", itemName);
        editor.apply();
    }

    private int getListPosition(String list) {
        return mSharedPreferences.getInt(list, -1);
    }

    private String getLastItem(String list) {
        return mSharedPreferences.getString(list + "lastItem", "");
    }

    private void saveLimitAdult() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 10);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String date = sdf.format(calendar.getTime());
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("limitAdult", date);
        editor.apply();
    }

    private boolean isLimitAdult() {
        String limit = mSharedPreferences.getString("limitAdult", "");
        if (limit.isEmpty()) return false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = sdf.parse(limit);
            return date.after(new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
