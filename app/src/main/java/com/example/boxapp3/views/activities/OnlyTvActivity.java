package com.example.boxapp3.views.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.media3.common.Player;

import com.example.boxapp3.BuildConfig;
import com.example.boxapp3.R;
import com.example.boxapp3.databinding.ActivityOnlyTvBinding;
import com.example.boxapp3.databinding.ModalSairBinding;
import com.example.boxapp3.listeners.activities.OnlyTvActivityListener;
import com.example.boxapp3.listeners.fragments.KeyListener;
import com.example.boxapp3.listeners.fragments.MainFragmentListener;
import com.example.boxapp3.listeners.fragments.OnlyTvPanelsFragmentListener;
import com.example.boxapp3.listeners.models.activities.OnlyTvActivityModelListener;
import com.example.boxapp3.models.activities.OnlyTvActivityModel;
import com.example.boxapp3.views.actvities.CustomOnlyTvActivity;
import com.example.boxapp3.views.fragments.MobileAppFragment;
import com.example.boxapp3.views.fragments.OnlyTvChannelInfoFragment;
import com.example.boxapp3.views.fragments.OnlyTvPanelsFragment;
import com.example.boxapp3.views.fragments.OnlyTvSearchFragment;
import com.example.boxapp3.views.fragments.ParentalFragment;
import com.example.boxapp3.views.fragments.RemindersListFragment;
import com.example.boxapp3.views.fragments.SportFragment;
import com.example.iptvsdk.common.IptvSettings;
import com.example.iptvsdk.common.centerContent.CenterContent;
import com.example.iptvsdk.common.centerContent.EmptyFragment;
import com.example.iptvsdk.common.menu.IptvMenu;
import com.example.iptvsdk.common.menu.IptvMenuListener;
import com.example.iptvsdk.data.models.xtream.Category;
import com.example.iptvsdk.data.models.xtream.StreamXc;
import com.example.iptvsdk.player.exo.ExoPlayerListener;
import com.example.iptvsdk.player.exo.ExoTracks;
import com.example.iptvsdk.player.exo.IptvExoPlayer;
import com.example.iptvsdk.services.StreamPlayedDurationService;
import com.example.iptvsdk.ui.live.IptvLive;
import com.example.iptvsdk.ui.mobile.IptvMobile;
import com.example.iptvsdk.ui.parental.IptvParental;
import com.example.iptvsdk.ui.reminder.IptvReminder;
import com.example.iptvsdk.utils.ViewUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class OnlyTvActivity extends CustomOnlyTvActivity implements OnlyTvActivityListener,
        OnlyTvActivityModelListener {

    private ActivityOnlyTvBinding mBinding;
    private OnlyTvActivityModel mModel;
    private CenterContent mCenterContent;
    private IptvMenu mIptvMenu;
    private IptvParental mIptvParental;
    private IptvReminder mIpTvReminder;
    private IptvMobile mIptvMobile;
    private IptvExoPlayer mIptvExoPlayer;
    private IptvLive mIptvLive;
    private IptvSettings mIptvSettings;
    private StreamPlayedDurationService mStreamPlayedDurationService;

    private String activeMenu = "home";
    private SharedPreferences sharedPreferences;

    private boolean adultAccessibile = false;
    private boolean isZapping = false;

    private int streamId;

    private Category selectedAdultCategory;

    TranslateAnimation animation;
    TranslateAnimation animation2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_only_tv);

        sharedPreferences = getSharedPreferences("app", MODE_PRIVATE);

        mIptvExoPlayer = new IptvExoPlayer(this, mBinding.playerView);
        mIptvLive = new IptvLive(this);
        mIptvSettings = new IptvSettings(this);
        mIpTvReminder = new IptvReminder(this);
        mStreamPlayedDurationService = new StreamPlayedDurationService(this);
        mIptvParental = new IptvParental(this);

        mIptvLive.getCategoryIdByPosition(sharedPreferences.getInt("listCategories", -1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> {
                    Log.e("OnlyTvActivity", "onCreate: ", throwable);
                })
                .doOnSuccess(id -> mIptvLive.setCategoryId(id))
                .subscribe();

        mModel = new OnlyTvActivityModel(this);

        mBinding.setModel(mModel);
        mBinding.setListener(this);

        super.setBinding(mBinding, mModel);


        mIptvMenu = new IptvMenu(new IptvMenuListener() {
            @Override
            public void onMenuColapse(boolean colapse) {
                super.onMenuColapse(colapse);
                mModel.setShowMenuLabels(!colapse);
            }
        });


        ViewUtils.listenFocus(this, new ViewUtils.FocusListener() {
            @Override
            public void onFocus(View view, String viewName) {
                int viewId = view.getId();
                if (viewName == null) {
                    mModel.setShowMenuLabels(false);
                    return;
                }
                mModel.setShowMenuLabels(viewName.equals("btn_home_menu")
                        || viewName.equals("btn_tv_menu")
                        || viewName.equals("btn_movies_menu")
                        || viewName.equals("btn_series_menu")
                        || viewName.equals("btn_kids_menu")
                        || viewName.equals("btn_sports_menu")
                        || viewName.equals("btn_sair")
                        || viewName.equals("btn_adult_menu"));

                if (mModel.getShowModalRemember()) {
                    if (!viewName.equals("btn_yes_remember") && !viewName.equals("btn_no_remember")) {
                        mBinding.includeModalRemember.btnYesRemember.requestFocus();
                        return;
                    }
                }
                if (mModel.getShowModalRememberSport()) {
                    if (!viewName.equals("btn_yes_modal_soccer")) {
                        mBinding.includeModalRememberSport.btnYesModalSoccer.requestFocus();
                        return;
                    }
                }
            }
        });

        mIptvExoPlayer.setExoPlayerListener(new ExoPlayerListener() {
            @Override
            public void isPlaying(boolean isPlaying) {

            }

            @Override
            public void changeTrack(List<ExoTracks> tracks) {

            }

            @Override
            public void totalDuration(long duration) {

            }

            @Override
            public void currentDuration(long duration) {

            }

            @Override
            public void endOfStream() {

            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY)
                    showLoadingBalls(false);
            }

            @Override
            public void onBandwithSpeedChanged(long speed) {
                mModel.setSpeed(mIptvExoPlayer.getSpeedFormatted(speed));
            }
        });
        showLoadingBalls(true);

        checkReminder();
//        setupMenu();
        setupContent();
        playPreviousStream();
    }

    private void playPreviousStream() {
        streamId = sharedPreferences.getInt("streamId", -1);

        if (streamId != -1) {
            mModel.setShowMenu(false);
            mIptvExoPlayer.play(streamId, mIptvSettings.getStreamExtension(), StreamXc.TYPE_STREAM_LIVE);
            showChannelInfo();
        } else {
            if (!BuildConfig.ONLY_TV_MENU_TOP) {
                mModel.setShowMenu(true);
            } else {
                mModel.setShowTopBar(true);
                mModel.setShowMenu(false);
            }
            mCenterContent.changeFragement(new OnlyTvPanelsFragment(this, mIptvLive));
        }
    }

    private void checkReminder() {
        mIpTvReminder.checkReminder(this, getIntent())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("MainActivity", "checkReminder: ", th))
                .doOnSuccess(reminderCallback -> {
                    if (reminderCallback != null && (reminderCallback.isReminder || reminderCallback.isSport))
                        if (!reminderCallback.isSport) {
                            mCenterContent.removeAllFragments();
                            mModel.setShowMenu(false);
                            mModel.setShowTopBar(false);
                            mBinding.includeModalRemember.setModel(reminderCallback);
                            mModel.setShowModalRemember(true);
                            mIpTvReminder.removeReminder(reminderCallback.epgTitle,
                                    reminderCallback.epgStart);
                        } else {
                            mCenterContent.removeAllFragments();
                            mModel.setShowMenu(false);
                            mModel.setShowTopBar(false);
                            mBinding.includeModalRememberSport.setModel(reminderCallback);
                            mModel.setShowModalRememberSport(true);
                            mIpTvReminder.removeReminder(reminderCallback.sportName,
                                    reminderCallback.epgStart);
                        }
                })
                .subscribe();
    }

    private Handler channelInfoHandler = new Handler();
    private Runnable channelInfoRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCenterContent.getCurrentFragment() instanceof OnlyTvChannelInfoFragment) {
                mCenterContent.removeAllFragments();
                mModel.setShowSpeed(false);
            }
        }
    };

    private void showChannelInfo() {
        channelInfoHandler.removeCallbacks(channelInfoRunnable);
        mModel.setShowMenu(false);
        mModel.setShowSpeed(true);
        mCenterContent.changeFragement(new OnlyTvChannelInfoFragment(zappedStreamId != -1 ?
                zappedStreamId : streamId, mIptvLive, isZapping,
                this));
        channelInfoHandler.postDelayed(channelInfoRunnable, 5000);
    }

    private void showChannelInfo(int streamId) {
        channelInfoHandler.removeCallbacks(channelInfoRunnable);
        mModel.setShowMenu(false);
        mModel.setShowSpeed(true);
        mCenterContent.changeFragement(new OnlyTvChannelInfoFragment(streamId, mIptvLive, isZapping,
                this));
        channelInfoHandler.postDelayed(channelInfoRunnable, 5000);
    }

    private Handler zappingClearHandler = new Handler();
    private Runnable zappingClearRunnable;
    private int zappedStreamId = -1;

    private void zapChannel(boolean isUp) {
        isZapping = true;
        if (zappingClearRunnable != null)
            zappingClearHandler.removeCallbacks(zappingClearRunnable);
        mIptvLive.zapChannel(isUp, zappedStreamId != -1 ? zappedStreamId : streamId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> {
                    Log.e("PlayerTvActivity", "zapChannel: ", throwable);
                })
                .doOnSuccess(id -> {
                    if (id != null) {
                        Log.d("PlayerTvActivity", "zapChannel: " + id);
                        zappedStreamId = id;
                        showChannelInfo(id);
                        zappingClearRunnable = new Runnable() {
                            @Override
                            public void run() {
                                isZapping = false;
                                if (zappedStreamId != -1)
                                    playChannel(zappedStreamId, false);
                            }
                        };
                        zappingClearHandler.postDelayed(zappingClearRunnable, 500);
                    }

                })
                .subscribe();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (!mModel.getMenuEnabled())
                mModel.setMenuEnabled(true);
            if (mCenterContent.getCurrentFragment() instanceof KeyListener) {
                if (((KeyListener) mCenterContent.getCurrentFragment()).dispatchKeyEvent(event))
                    return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (mModel.getShowMenuLabels() && mCenterContent.getCurrentFragment() instanceof MainFragmentListener) {
                    ((MainFragmentListener) mCenterContent.getCurrentFragment()).firstFocus()
                            .requestFocus();
                    return true;
                }
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (mModel.getShowModalExit() || mModel.getShowModalMobile() || mModel.getShowModalRemember() || mModel.getShowModalRememberSport())
                    return super.dispatchKeyEvent(event);
                if (mModel.getShowModalAdult()) {
                    if (mBinding.include.editTextText2.hasFocus()) {
                        if (!mIptvParental.hasPassword())
                            mBinding.include.editTextText3.requestFocus();
                        else
                            mBinding.include.textView42.requestFocus();
                        return true;
                    }
                    if (mBinding.include.editTextText3.hasFocus()) {
                        mBinding.include.textView42.requestFocus();
                        return true;
                    }
                    if (mBinding.include
                            .textView42.hasFocus()) {
                        mBinding.include.editTextText2.requestFocus();
                        return true;
                    }
                    if (mBinding.include.btnCancelModalAdultEnter.hasFocus()) {
                        mBinding.include.editTextText2.requestFocus();
                        return true;
                    }
                }

                if (isZapping || (!mModel.getShowMenu() && !(mCenterContent.getCurrentFragment()
                        instanceof OnlyTvPanelsFragment) && !(mCenterContent.getCurrentFragment()
                        instanceof SportFragment) && !(mCenterContent.getCurrentFragment()
                        instanceof MobileAppFragment) && !(mCenterContent.getCurrentFragment()
                        instanceof OnlyTvChannelInfoFragment &&
                        !((OnlyTvChannelInfoFragment) mCenterContent.getCurrentFragment()).canMoveDown())
                        && !(mCenterContent.getCurrentFragment()
                        instanceof ParentalFragment))) {
                    zapChannel(false);
                    return true;
                }
            }

            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                if (mModel.getShowModalExit() || mModel.getShowModalMobile() || mModel.getShowModalRemember() || mModel.getShowModalRememberSport())
                    return super.dispatchKeyEvent(event);
                if (mModel.getShowModalAdult()) {
                    if (mBinding.include.editTextText3.hasFocus()) {
                        mBinding.include.editTextText2.requestFocus();
                        return true;
                    }
                    if (mBinding.include.textView42.hasFocus()) {
                        if (!mIptvParental.hasPassword())
                            mBinding.include.editTextText3.requestFocus();
                        else
                            mBinding.include.editTextText2.requestFocus();
                        return true;
                    }
                }

                if (isZapping || (!mModel.getShowMenu() && !(mCenterContent.getCurrentFragment()
                        instanceof OnlyTvPanelsFragment) && !(mCenterContent.getCurrentFragment()
                        instanceof SportFragment) && !(mCenterContent.getCurrentFragment()
                        instanceof MobileAppFragment) && !(mCenterContent.getCurrentFragment()
                        instanceof OnlyTvChannelInfoFragment &&
                        !((OnlyTvChannelInfoFragment) mCenterContent.getCurrentFragment()).canMoveUp())
                        && !(mCenterContent.getCurrentFragment()
                        instanceof ParentalFragment))) {
                    zapChannel(true);
                    return true;
                }
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) {
                if (mCenterContent.getCurrentFragment() instanceof OnlyTvPanelsFragmentListener) {
                    mCenterContent.removeAllFragments();
                    mModel.setShowMenu(false);
                    return true;
                }
                if (mCenterContent.getCurrentFragment() instanceof OnlyTvSearchFragment) {
                    mCenterContent.removeAllFragments();
                    mModel.setShowMenu(false);
                    return true;
                }
                if (mCenterContent.getCurrentFragment() instanceof OnlyTvChannelInfoFragment) {
                    mCenterContent.removeAllFragments();
                    mModel.setShowMenu(false);
                    return true;
                }
                if (mModel.getShowModalAdult()) {
                    mBinding.include.editTextText2.setText("");
                    mBinding.include.editTextText3.setText("");
                    mModel.setShowModalAdult(false);
                    return true;
                }

                if (mModel.getShowModalExit()) {
                    mModel.setShowModalExit(false);
                    return true;
                }

                if (mModel.getShowModalMobile()) {
                    mModel.setShowModalMobile(false);
                    return true;
                }

                if (mModel.getShowModalRemember()) {
                    mModel.setShowModalRemember(false);
                    return true;
                }

                if (mModel.getShowModalRememberSport()) {
                    mModel.setShowModalRememberSport(false);
                    return true;
                }

                mModel.setShowModalExit(true);
                new Handler().postDelayed(() -> {
                    if (mModel.getShowModalExit())
                        ((ModalSairBinding) mBinding.modalExit).btnYes.requestFocus();
                }, 1000);
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                isZapping = false;
                if (mModel.getShowModalExit() || mModel.getShowModalMobile() || mModel.getShowModalRemember() || mModel.getShowModalRememberSport())
                    return super.dispatchKeyEvent(event);
                if (mCenterContent.getCurrentFragment() instanceof EmptyFragment) {
                    showChannelInfo();
                    return true;
                } else if (mCenterContent.getCurrentFragment() instanceof OnlyTvChannelInfoFragment) {
                    mCenterContent.changeFragement(new OnlyTvPanelsFragment(this, mIptvLive));
                    if (!BuildConfig.ONLY_TV_MENU_TOP)
                        mModel.setShowMenu(true);
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    private void setupContent() {
        mCenterContent = new CenterContent(this,
                R.id.main_active_fragment,
                new EmptyFragment());
//        mCenterContent.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);

        mCenterContent.addFragment("search", new OnlyTvSearchFragment(this));
        mCenterContent.addFragment("live", new OnlyTvPanelsFragment(this, mIptvLive));
        mCenterContent.addFragment("sports", new SportFragment());
        mCenterContent.addFragment("mobile", new MobileAppFragment());
        mCenterContent.addFragment("remember", new RemindersListFragment());

        if (BuildConfig.ONLY_TV_MENU_TOP) {
            setCenterContent(mBinding, mModel, mCenterContent);
        }

    }

    @Override
    public void onMenuClicked(String menu) {
        mModel.setActualMenu(menu);
        activeMenu = menu;

        if (menu.equals("adults")) {
            if (!adultAccessibile) {
                mBinding.include.setRegisterPassword(!mIptvParental.hasPassword());
                mModel.setShowModalAdult(true);
                mBinding.include.editTextText2.requestFocus();
                return;
            }
        } else if (menu.equals("exit")) {
            mModel.setShowModalExit(true);
        } else adultAccessibile = false;
        mCenterContent.showFragment(menu);
    }

    @Override
    public void onParentalPasswordSet() {
        if (mIptvParental.hasPassword()) {
            adultAccessibile = mIptvParental.checkPassword(mBinding.include.editTextText2.getText().toString());
            if (!adultAccessibile) {
                mBinding.include.textView41.setText(R.string.wrong_password);
                return;
            }
            mModel.setShowModalAdult(false);
            saveLimitAdult();
            if (mCenterContent.getCurrentFragment() instanceof OnlyTvPanelsFragmentListener) {
                ((OnlyTvPanelsFragmentListener) mCenterContent.getCurrentFragment())
                        .onCategorySelected(Integer.parseInt(selectedAdultCategory.getCategoryId()));
            }

            //mCenterContent.showFragment("adults");
        } else {
            adultAccessibile = mIptvParental.setPassword(mBinding.include.editTextText2.getText().toString(),
                    mBinding.include.editTextText3.getText().toString());
            if (!adultAccessibile) {
                mBinding.include.textView41.setText(R.string.password_not_match);
                return;
            }
            mModel.setShowModalAdult(false);
            saveLimitAdult();

            if (mCenterContent.getCurrentFragment() instanceof OnlyTvPanelsFragmentListener) {
                ((OnlyTvPanelsFragmentListener) mCenterContent.getCurrentFragment())
                        .onCategorySelected(Integer.parseInt(selectedAdultCategory.getCategoryId()));
            }
            //mCenterContent.showFragment("adults");
        }
        mBinding.include.editTextText2.setText("");
        mBinding.include.editTextText3.setText("");
    }

    @Override
    public void onModalExitCancel() {
        mModel.setShowModalExit(false);
        onGoToMenu();
    }

    @Override
    public void onModalExitConfirm() {
        finish();
        System.exit(0);
    }

    @Override
    public void onModalMobileConfirm() {
        mModel.setShowModalMobile(false);
    }

    @Override
    public void onSearchIconClicked() {

    }

    @Override
    public void onGoToMenu() {
        switch (activeMenu) {
            //case "home":
            //    mBinding.includeMenu.btnHomeMenu.requestFocus();
            //    break;
            //case "live":
            //    mBinding.includeMenu.btnTvMenu.requestFocus();
            //    break;
            //case "movies":
            //    mBinding.includeMenu.btnMoviesMenu.requestFocus();
            //    break;
            //case "series":
            //    mBinding.includeMenu.btnSeriesMenu.requestFocus();
            //    break;
            case "kids":
                mBinding.includeMenu.btnKidsMenu.requestFocus();
                break;
            case "sports":
                mBinding.includeMenu.btnSportsMenu.requestFocus();
                break;
            case "adult":
                mBinding.includeMenu.btnAdultMenu.requestFocus();
                break;
        }
    }

    @Override
    public void playChannel(int streamId) {
        playChannel(streamId, true);
    }

    public void playChannel(int streamId, boolean showChannelInfo) {
        if (streamId <= 0)
            return;
        mModel.setShowMenu(false);
        mCenterContent.removeAllFragments();

        showLoadingBalls(true);

        mIptvExoPlayer.play(streamId, mIptvSettings.getStreamExtension(), StreamXc.TYPE_STREAM_LIVE);
        mIptvLive.getChannel(streamId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(streamXc -> {
                    if (!streamXc.isAdult()) {
                        mStreamPlayedDurationService.insertOrUpdate(streamId, mIptvExoPlayer.getCurrentPosition(), StreamXc.TYPE_STREAM_LIVE);
                        sharedPreferences.edit().putInt("streamId", streamId).apply();
                    }
                })
                .doOnError(throwable -> {
                    Log.e("PlayerTvActivity", "playChannel: ", throwable);
                })
                .subscribe();

        if (showChannelInfo)
            showChannelInfo(streamId);
    }

    @Override
    public void onEpgVisibilityChanged(boolean visible) {
        if (!BuildConfig.ONLY_TV_MENU_TOP)
            mModel.setShowMenu(!visible);
    }

    @Override
    public void onCategorySelected(Category category) {
        selectedAdultCategory = category;
        if (!category.isAdult()) {
            adultAccessibile = false;
            return;
        } else {
            if (!adultAccessibile) {
                mModel.setMenuEnabled(false);
                mCenterContent.changeFragement(new ParentalFragment(this, Integer
                        .parseInt(category.getCategoryId())));
                //mBinding.include.setRegisterPassword(!mIptvParental.hasPassword());
                //mModel.setShowModalAdult(true);
                //mBinding.include.editTextText2.requestFocus();
                return;
            } else {
                mModel.setShowModalAdult(false);

                if (mCenterContent.getCurrentFragment() instanceof OnlyTvPanelsFragmentListener) {
                    ((OnlyTvPanelsFragmentListener) mCenterContent.getCurrentFragment())
                            .onCategorySelected(Integer.parseInt(selectedAdultCategory.getCategoryId()));
                }
            }
        }
    }

    @Override
    public void onPlayback(int streamId, Date dateStart) {
        mModel.setShowMenu(false);
        mCenterContent.removeAllFragments();

        String urlPlayback = mIptvLive.generatePlaybacklUrl(this, String.valueOf(streamId), dateStart);
        mIptvExoPlayer.play(urlPlayback);
    }

    @Override
    public void onShowPanels() {
        if (!BuildConfig.ONLY_TV_MENU_TOP)
            mModel.setShowMenu(true);
        mCenterContent.changeFragement(new OnlyTvPanelsFragment(this, mIptvLive));
    }

    @Override
    public void onChangeMenuEnabled(boolean enabled) {
        mModel.setMenuEnabled(enabled);
    }

    @Override
    public void onShowEpg() {
        mCenterContent.changeFragement(new OnlyTvPanelsFragment(this, mIptvLive, true));
    }

    @Override
    public void onBtnFocused() {
        if (channelInfoHandler == null || channelInfoRunnable == null)
            return;
        channelInfoHandler.removeCallbacks(channelInfoRunnable);
        channelInfoHandler.postDelayed(channelInfoRunnable, 5000);
    }

    @Override
    public void onAllowAdultContent() {
        mModel.setShowModalAdult(false);
        saveLimitAdult();
        mCenterContent.changeFragement(new OnlyTvPanelsFragment(this, mIptvLive, false));
        if (mCenterContent.getCurrentFragment() instanceof OnlyTvPanelsFragmentListener) {
            ((OnlyTvPanelsFragmentListener) mCenterContent.getCurrentFragment())
                    .onCategorySelected(Integer.parseInt(selectedAdultCategory.getCategoryId()));
        }
    }

    @Override
    public void onShowChannelPanels() {
        if (!BuildConfig.FLAVOR.equals("tiger1"))
            mModel.setShowMenu(true);
        mCenterContent.changeFragement(new OnlyTvPanelsFragment(this, mIptvLive));
    }

    @Override
    public void onZapStreamChanged(StreamXc stream) {
        zappedStreamId = stream.getStreamId();
    }

    @Override
    public void onGoToChannelTopBar() {
        super.onGoToChannelTopBar(mBinding);
    }

    @Override
    public void onGoToSportTopBar() {
        super.onGoToSportTopBar(mBinding);
    }

    @Override
    public void onGoToMenuTopBar() {
        super.onGoToMenuTopBar(mBinding);
    }

    @Override
    public void modalReminderWatch(int streamId) {
        mModel.setShowModalRemember(false);
        playChannel(streamId);
    }

    @Override
    public void modalReminderClose() {
        mModel.setShowModalRemember(false);
    }

    @Override
    public void modalReminderSportClose() {
        mModel.setShowModalRememberSport(false);
    }

    private Handler menuFocusHandler = new Handler();
    private Runnable menuFocusRunnable;

    @Override
    public void onCanMenuFocused(String menuName) {
        if (!mModel.getMenuEnabled())
            return;
        if (menuFocusRunnable != null)
            menuFocusHandler.removeCallbacks(menuFocusRunnable);

        menuFocusRunnable = () -> {
            onMenuClicked(menuName);
        };

        menuFocusHandler.postDelayed(menuFocusRunnable, 1000);
    }

    @Override
    public void onToggleMenu() {
        mModel.setShowMenu(!mModel.getShowMenu());
    }

    @Override
    public void onTopbarChannelClicked() {
        onMenuClicked("live");
        mModel.setShowMenu(false);
    }

    @Override
    public void onTopbarSoccerClicked() {
        onMenuClicked("sports");
        mModel.setShowMenu(false);
    }

    @Override
    public void onModalMobileOpened() {
        if (mIptvMobile == null)
            mIptvMobile = new IptvMobile(this, BuildConfig.IS_MOBILE);
        mBinding.includeModalMobile.getRoot()
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mBinding.includeModalMobile.getRoot()
                                .getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                        mBinding.includeModalMobile.btnYes.requestFocus();
                    }
                });
        mIptvMobile.getAccessCode()
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .doOnSuccess(s -> {
                    mModel.setMobileCode(s);
                })
                .doOnError(th -> Log.e("MainActivity", "onModalMobileOpened: ", th))
                .subscribe();
    }

    private void showLoadingBalls(boolean show) {
        mModel.setShowLoadingPlayer(show);
        mBinding.loading.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.loading.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                ImageView bounceBallImageBlue = mBinding.loading.bounceBallBlue;
                ImageView bounceBallImageOrange = mBinding.loading.bounceBallOrange;
                int parentWidth = ((View) bounceBallImageBlue.getParent()).getWidth();

                if (show) {
                    // Verifica se a animação já está em andamento
                    if (animation != null && !animation.hasEnded()) {
                        // Se sim, reinicie a animação
                        animation.cancel();
                        animation2.cancel();
                    }

                    // Crie as animações apenas se estiverem nulas ou terminadas
                    if (animation == null || animation.hasEnded()) {
                        animation = new TranslateAnimation(-1 * (parentWidth / 2 - bounceBallImageBlue.getWidth()), parentWidth / 2 - bounceBallImageBlue.getWidth(), 0, 0);
                        animation.setDuration(1000);
                        animation.setRepeatCount(Animation.INFINITE);
                        animation.setRepeatMode(Animation.REVERSE);
                    }
                    if (animation2 == null || animation2.hasEnded()) {
                        animation2 = new TranslateAnimation(parentWidth / 2 - bounceBallImageBlue.getWidth(), -1 * (parentWidth / 2 - bounceBallImageBlue.getWidth()), 0, 0);
                        animation2.setDuration(1000);
                        animation2.setRepeatCount(Animation.INFINITE);
                        animation2.setRepeatMode(Animation.REVERSE);
                    }

                    bounceBallImageBlue.startAnimation(animation);
                    bounceBallImageOrange.startAnimation(animation2);
                } else {
                    // Se show for falso, cancelar a animação
                    if (animation != null) {
                        animation.cancel();
                    }
                    if (animation2 != null) {
                        animation2.cancel();
                    }
                }
            }
        });
    }

    private void saveLimitAdult() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 2);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String date = sdf.format(calendar.getTime());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("limitAdult", date);
        editor.apply();
    }

    private void clearAdultLimit() {
        zappedStreamId = -1;
        mIptvLive.setCategoryId(-1);
        if (sharedPreferences == null)
            sharedPreferences = getSharedPreferences("app", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("limitAdult");
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mIptvExoPlayer != null)
            mIptvExoPlayer.onStart();
        clearAdultLimit();
        playPreviousStream();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIptvExoPlayer != null)
            mIptvExoPlayer.onResume();
        clearAdultLimit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIptvExoPlayer != null)
            mIptvExoPlayer.onPause();
        clearAdultLimit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mIptvExoPlayer != null)
            mIptvExoPlayer.onStop();
        clearAdultLimit();
    }
}
