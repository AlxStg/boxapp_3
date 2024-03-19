package com.example.boxapp3.views.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import androidx.databinding.DataBindingUtil;

import com.example.boxapp3.R;
import com.example.boxapp3.databinding.ActivityPlayerTvBinding;
import com.example.boxapp3.listeners.activities.PlayerTvActivityListener;
import com.example.boxapp3.listeners.fragments.KeyListener;
import com.example.boxapp3.views.fragments.players.tv.PlayerTvChannelInfoFragment;
import com.example.boxapp3.views.fragments.players.tv.PlayerTvPanelsFragment;
import com.example.iptvsdk.common.IptvSettings;
import com.example.iptvsdk.common.centerContent.CenterContent;
import com.example.iptvsdk.common.centerContent.EmptyFragment;
import com.example.iptvsdk.data.models.EpgDb;
import com.example.iptvsdk.data.models.xtream.StreamXc;
import com.example.iptvsdk.player.exo.IptvExoPlayer;
import com.example.iptvsdk.services.StreamPlayedDurationService;
import com.example.iptvsdk.ui.live.IptvLive;

import java.util.Calendar;
import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PlayerTvActivity extends BaseActivity implements PlayerTvActivityListener {

    private ActivityPlayerTvBinding mBinding;
    private IptvExoPlayer mIptvExoPlayer;
    private IptvLive mIptvLive;
    private IptvSettings mIptvSettings;
    private CenterContent mCenterContent;
    private StreamPlayedDurationService mStreamPlayedDurationService;

    private int streamId;
    private StreamXc stream;
    private SharedPreferences sharedPreferences;
    private boolean isAdult = false;
    private boolean isZapping = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_player_tv);
        sharedPreferences = getSharedPreferences("app", MODE_PRIVATE);

        mIptvExoPlayer = new IptvExoPlayer(this, mBinding.playerView);
        mIptvLive = new IptvLive(this);
        mIptvSettings = new IptvSettings(this);
        mStreamPlayedDurationService = new StreamPlayedDurationService(this);

        streamId = getIntent().getIntExtra("streamId", -1);
        if (streamId == -1) {
            streamId = sharedPreferences.getInt("streamId",
                    -1);
        } else
            mIptvLive.getPositionById(streamId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .doOnSuccess(this::setActualStreamPosition)
                    .subscribe();
        isAdult = getIntent().getBooleanExtra("isAdult", false);


        mCenterContent = new CenterContent(this, R.id.content,
                new PlayerTvChannelInfoFragment(streamId, mIptvLive, isZapping, this));
        mCenterContent.removeAllFragments();

        registerFragments();


        if (streamId != -1) {
            mIptvExoPlayer.play(streamId, mIptvSettings.getStreamExtension(), StreamXc.TYPE_STREAM_LIVE);
            showChannelInfo();
        } else {
            mCenterContent.showFragment("panels");
        }

    }

    private void registerFragments() {
        mCenterContent.addFragment("panels", new PlayerTvPanelsFragment(
                sharedPreferences,this,
                mIptvLive, isAdult));
        mCenterContent.addFragment("channelInfo", new PlayerTvChannelInfoFragment(streamId,
                mIptvLive, isZapping, this));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mCenterContent.getCurrentFragment() instanceof KeyListener) {
                if (((KeyListener) mCenterContent.getCurrentFragment()).dispatchKeyEvent(event)) {
                    return true;
                }
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) {
                Log.d("PlayerTvActivity", "dispatchKeyEvent: " + mCenterContent.getCurrentFragment());
                if (mCenterContent.getCurrentFragment() instanceof PlayerTvPanelsFragment) {
                    mCenterContent.removeAllFragments();
                    return true;
                }
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (mCenterContent.getCurrentFragment() instanceof EmptyFragment) {
                    showChannelInfo();
                    return true;
                }
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_CHANNEL_UP) {
                if(isZapping || (!(mCenterContent.getCurrentFragment() instanceof
                        PlayerTvPanelsFragment) && !(mCenterContent.getCurrentFragment() instanceof
                        PlayerTvChannelInfoFragment))) {
                    zapChannel(true);
                    return true;
                }
            }

            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_CHANNEL_DOWN) {
                if(isZapping || (!(mCenterContent.getCurrentFragment() instanceof
                        PlayerTvPanelsFragment) && !(mCenterContent.getCurrentFragment() instanceof
                        PlayerTvChannelInfoFragment))) {
                    zapChannel(false);
                    return true;
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    private Handler channelInfoHandler = new Handler();
    private Runnable channelInfoRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCenterContent.getCurrentFragment() instanceof PlayerTvChannelInfoFragment)
                mCenterContent.removeAllFragments();
        }
    };

    private void showChannelInfo() {
        mCenterContent.changeFragement(new PlayerTvChannelInfoFragment(streamId, mIptvLive,
                isZapping, this));
        channelInfoHandler.removeCallbacks(channelInfoRunnable);
        channelInfoHandler.postDelayed(channelInfoRunnable, 5000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIptvExoPlayer.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIptvExoPlayer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIptvExoPlayer.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIptvExoPlayer.onStop();
    }

    @Override
    public void onChannelClick(StreamXc stream) {
        this.stream = stream;
        this.streamId = stream.getStreamId();
        mIptvExoPlayer.play(streamId, mIptvSettings.getStreamExtension(), StreamXc.TYPE_STREAM_LIVE);
        sharedPreferences.edit().putInt("streamId", streamId).apply();
        mStreamPlayedDurationService.insertOrUpdate(streamId, mIptvExoPlayer.getCurrentPosition(), StreamXc.TYPE_STREAM_LIVE);
        showChannelInfo();
    }

    @Override
    public void onChannelInfoNavigate() {
        channelInfoHandler.removeCallbacks(channelInfoRunnable);
        channelInfoHandler.postDelayed(channelInfoRunnable, 10000);
        channelInfoHandler.postDelayed(channelInfoRunnable, 10000);
    }

    @Override
    public void onShowPanels() {
        mCenterContent.changeFragement(new PlayerTvPanelsFragment(
                sharedPreferences,this, mIptvLive, isAdult));
        Log.d("PlayerTvActivity", "onShowPanels: ");
    }

    @Override
    public void onRewind() {

    }

    @Override
    public void onFastForward() {

    }

    @Override
    public void onEpgClick(EpgDb item, int daysPlayback) {
        if (daysPlayback > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -daysPlayback);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 1);
            Date date = calendar.getTime();
            Date dateStart = item.getStart();

            if (dateStart.after(date) && dateStart.before(new Date())) {
                String urlPlayback = mIptvLive.generatePlaybacklUrl(this, String.valueOf(streamId), dateStart);
                mIptvExoPlayer.play(urlPlayback);
                return;
            }
        }
    }

    @Override
    public void setActualStreamPosition(int actualStreamPosition) {
        mIptvLive.setActualStreamPosition(actualStreamPosition);
    }

    private Handler zappingClearHandler = new Handler();
    private Runnable zappingClearRunnable = new Runnable() {
        @Override
        public void run() {
            isZapping = false;
        }
    };

    private void zapChannel(boolean isUp){
        isZapping = true;
        zappingClearHandler.removeCallbacks(zappingClearRunnable);
        mIptvLive.zapChannel(sharedPreferences.getInt("streamId", -1), isUp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> {
            Log.e("PlayerTvActivity", "zapChannel: ", throwable);
        })
                .doOnSuccess(this::onChannelClick)
                .subscribe();
        zappingClearHandler.postDelayed(zappingClearRunnable, 1000);
    }
}