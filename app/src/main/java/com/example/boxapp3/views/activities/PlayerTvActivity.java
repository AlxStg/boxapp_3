package com.example.boxapp3.views.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;
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
import com.example.iptvsdk.data.models.xtream.StreamXc;
import com.example.iptvsdk.player.exo.IptvExoPlayer;
import com.example.iptvsdk.ui.live.IptvLive;

public class PlayerTvActivity extends AppCompatActivity implements PlayerTvActivityListener {

    private ActivityPlayerTvBinding mBinding;
    private IptvExoPlayer mIptvExoPlayer;
    private IptvLive mIptvLive;
    private IptvSettings mIptvSettings;
    private CenterContent mCenterContent;

    private int streamId;
    private StreamXc stream;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_player_tv);
        sharedPreferences = getSharedPreferences("app", MODE_PRIVATE);

        mIptvExoPlayer = new IptvExoPlayer(this, mBinding.playerView);
        mIptvLive = new IptvLive(this);
        mIptvSettings = new IptvSettings(this);

        streamId = getIntent().getIntExtra("streamId", -1);
        if (streamId == -1) {
            streamId = sharedPreferences.getInt("streamId",
                    -1);
        }


        mCenterContent = new CenterContent(this, R.id.content,
                new PlayerTvChannelInfoFragment(streamId, mIptvLive, this));
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
                mIptvLive));
        mCenterContent.addFragment("channelInfo", new PlayerTvChannelInfoFragment(streamId,
                mIptvLive, this));
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
        mCenterContent.changeFragement(new PlayerTvChannelInfoFragment(streamId, mIptvLive, this));
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
                sharedPreferences,this, mIptvLive));
        Log.d("PlayerTvActivity", "onShowPanels: ");
    }

    @Override
    public void onRewind() {

    }

    @Override
    public void onFastForward() {

    }
}