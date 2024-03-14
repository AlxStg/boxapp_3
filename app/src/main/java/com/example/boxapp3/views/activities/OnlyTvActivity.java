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
import com.example.boxapp3.views.fragments.MobileAppFragment;
import com.example.boxapp3.views.fragments.OnlyTvChannelInfoFragment;
import com.example.boxapp3.views.fragments.OnlyTvPanelsFragment;
import com.example.boxapp3.views.fragments.OnlyTvSearchFragment;
import com.example.boxapp3.views.fragments.SportFragment;
import com.example.boxapp3.views.fragments.TvFragment;
import com.example.boxapp3.views.fragments.players.tv.PlayerTvChannelInfoFragment;
import com.example.boxapp3.views.fragments.players.tv.PlayerTvPanelsFragment;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class OnlyTvActivity extends BaseActivity implements OnlyTvActivityListener, OnlyTvActivityModelListener {

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
    private StreamXc stream;

    private Category selectedAdultCategory;

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


        mModel = new OnlyTvActivityModel(this);

        mBinding.setModel(mModel);
        mBinding.setListener(this);


        checkReminder();

//        setupMenu();
        setupContent();
        mIptvMenu = new IptvMenu(new IptvMenuListener() {
            @Override
            public void onMenuColapse(boolean colapse) {
                super.onMenuColapse(colapse);
                mModel.setShowMenuLabels(!colapse);
            }
        });


        streamId = sharedPreferences.getInt("streamId", -1);

        if (streamId != -1) {
            mIptvExoPlayer.play(streamId, mIptvSettings.getStreamExtension(), StreamXc.TYPE_STREAM_LIVE);
            showChannelInfo();
        }
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
    }

    private void checkReminder() {
        mIpTvReminder.checkReminder(this, getIntent())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("MainActivity", "checkReminder: ", th))
                .doOnSuccess(reminderCallback -> {
                    Log.d("MainActivity", "checkReminder: " + reminderCallback);
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
        mModel.setShowMenu(false);
        mModel.setShowSpeed(true);
        mCenterContent.changeFragement(new OnlyTvChannelInfoFragment(streamId, mIptvLive, isZapping,
                this));
        channelInfoHandler.removeCallbacks(channelInfoRunnable);
        channelInfoHandler.postDelayed(channelInfoRunnable, 5000);
    }

    private Handler zappingClearHandler = new Handler();
    private Runnable zappingClearRunnable = new Runnable() {
        @Override
        public void run() {
            isZapping = false;
        }
    };

    private void zapChannel(boolean isUp) {
        isZapping = true;
        zappingClearHandler.removeCallbacks(zappingClearRunnable);
        mIptvLive.zapChannel(isUp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> {
                    Log.e("PlayerTvActivity", "zapChannel: ", throwable);
                })
                .doOnSuccess(this::playChannel)
                .subscribe();
        zappingClearHandler.postDelayed(zappingClearRunnable, 1000);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
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

                if (isZapping || !mModel.getShowMenu()) {
                    zapChannel(false);
                    return true;
                }
            }

            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
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

                if (isZapping || !mModel.getShowMenu()) {
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

                mModel.setShowModalExit(true);
                new Handler().postDelayed(() -> {
                    if (mModel.getShowModalExit())
                        ((ModalSairBinding) mBinding.modalExit).btnYes.requestFocus();
                }, 1000);
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (mCenterContent.getCurrentFragment() instanceof EmptyFragment) {
                    showChannelInfo();
                    return true;
                } else if (mCenterContent.getCurrentFragment() instanceof OnlyTvChannelInfoFragment) {
                    mCenterContent.changeFragement(new OnlyTvPanelsFragment(this));
                    mModel.setShowMenu(true);
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    private void setupContent() {
        mCenterContent = new CenterContent(this,
                R.id.main_active_fragment,
                new OnlyTvPanelsFragment(this));
//        mCenterContent.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);

        mCenterContent.addFragment("search", new OnlyTvSearchFragment(this));
        mCenterContent.addFragment("live", new OnlyTvPanelsFragment(this));
        mCenterContent.addFragment("sports", new SportFragment());
        mCenterContent.addFragment("mobile", new MobileAppFragment());
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
    public void playChannel(StreamXc stream) {
        mModel.setShowMenu(false);
        mCenterContent.removeAllFragments();

        showLoadingBalls(true);

        this.stream = stream;
        this.streamId = stream.getStreamId();
        mIptvExoPlayer.play(streamId, mIptvSettings.getStreamExtension(), StreamXc.TYPE_STREAM_LIVE);
        sharedPreferences.edit().putInt("streamId", streamId).apply();
        mStreamPlayedDurationService.insertOrUpdate(streamId, mIptvExoPlayer.getCurrentPosition(), StreamXc.TYPE_STREAM_LIVE);
        showChannelInfo();
    }

    @Override
    public void onEpgVisibilityChanged(boolean visible) {
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
                mBinding.include.setRegisterPassword(!mIptvParental.hasPassword());
                mModel.setShowModalAdult(true);
                mBinding.include.editTextText2.requestFocus();
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
        mModel.setShowMenu(true);
        mCenterContent.changeFragement(new OnlyTvPanelsFragment(this));
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

    TranslateAnimation animation;
    TranslateAnimation animation2;

    private void showLoadingBalls(boolean show) {
        mModel.setShowLoadingPlayer(show);
        ImageView bounceBallImageBlue = mBinding.loading.bounceBallBlue;
        ImageView bounceBallImageOrange = mBinding.loading.bounceBallOrange;
        if (!show && animation != null && animation2 != null) {
            animation.cancel();
            animation2.cancel();
            bounceBallImageBlue.setVisibility(View.GONE);
            bounceBallImageOrange.setVisibility(View.GONE);
            return;
        }
        int parentWidth = ((View) bounceBallImageBlue.getParent()).getWidth();
        // Create a TranslateAnimation
        // The parameters are (fromXDelta, toXDelta, fromYDelta, toYDelta)
        // We want to move the ball from the left side of the screen (0) to the middle of the screen (half of the parent view's width)
        // The Y position remains constant, so both fromYDelta and toYDelta are 0
        animation = new TranslateAnimation(-1 * (parentWidth / 2 - bounceBallImageBlue.getWidth()), parentWidth / 2 - bounceBallImageBlue.getWidth(), 0, 0);
        animation2 = new TranslateAnimation(parentWidth / 2 - bounceBallImageBlue.getWidth(), -1 * (parentWidth / 2 - bounceBallImageBlue.getWidth()), 0, 0);
        // Set the duration of the animation
        animation.setDuration(1000); // 1000 milliseconds = 1 second
        // Set the animation to repeat indefinitely
        animation.setRepeatCount(Animation.INFINITE);
        // Set the animation to repeat in reverse so the ball goes back to its original position
        animation.setRepeatMode(Animation.REVERSE);
        animation2.setDuration(1000); // 1000 milliseconds = 1 second
        // Set the animation to repeat indefinitely
        animation2.setRepeatCount(Animation.INFINITE);
        // Set the animation to repeat in reverse so the ball goes back to its original position
        animation2.setRepeatMode(Animation.REVERSE);
        // Start the animation
        bounceBallImageBlue.startAnimation(animation);
        bounceBallImageOrange.startAnimation(animation2);
    }

}
