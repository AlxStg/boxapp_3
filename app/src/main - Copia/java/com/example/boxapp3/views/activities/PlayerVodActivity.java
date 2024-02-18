package com.example.boxapp3.views.activities;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.media3.common.C;

import com.example.boxapp3.R;
import com.example.boxapp3.databinding.ItemTracksPlayerBinding;
import com.example.boxapp3.databinding.PlayerMoviesBinding;
import com.example.boxapp3.listeners.activities.PlayerVodActivityListener;
import com.example.boxapp3.models.activities.PlayerVodActivityModel;
import com.example.boxapp3.models.adapters.ItemTracksPlayerModel;
import com.example.iptvsdk.common.centerContent.CenterContent;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.player.exo.ExoTracks;
import com.example.iptvsdk.player.exo.IptvExoPlayer;
import com.example.iptvsdk.ui.player_vod.IptvPlayerVod;
import com.example.iptvsdk.ui.player_vod.IptvPlayerVodListener;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public class PlayerVodActivity extends BaseActivity implements PlayerVodActivityListener {

    private IptvExoPlayer mIptvExoPlayer;
    private CenterContent mCenterContent;
    private SharedPreferences sharedPreferences;

    private IptvPlayerVod iptvPlayerVod;
    private PlayerVodActivityModel mModel;

    private int streamId;

    private PlayerMoviesBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        mBinding = DataBindingUtil.setContentView(this, R.layout.player_movies);



        sharedPreferences = getSharedPreferences("app", MODE_PRIVATE);

        mIptvExoPlayer = new IptvExoPlayer(this, mBinding.playerView);

        IptvPlayerVodListener iptvPlayerVodListener = new IptvPlayerVodListener() {
            @Override
            public void onTotalDurationReceived(long duration) {
                super.onTotalDurationReceived(duration);
                mModel.setDuration(iptvPlayerVod.formatTime(duration));
            }

            @Override
            public void finishActivity() {
                finish();
            }

            @Override
            public void onChangeTracksVideo(List<ExoTracks> tracks) {
                super.onChangeTracksVideo(tracks);
                setupTracksist(tracks, C.TRACK_TYPE_VIDEO);
            }

            @Override
            public void onChangeTracksAudio(List<ExoTracks> tracks) {
                super.onChangeTracksAudio(tracks);
                setupTracksist(tracks, C.TRACK_TYPE_AUDIO);
            }

            @Override
            public void onChangeTracksSubtitles(List<ExoTracks> tracks) {
                super.onChangeTracksSubtitles(tracks);
                setupTracksist(tracks, C.TRACK_TYPE_TEXT);
            }

            @Override
            public void alreadyWatched(boolean isAlreadyWatched) {
                super.alreadyWatched(isAlreadyWatched);
                mModel.setShowResumeModal(isAlreadyWatched);
                if (isAlreadyWatched) {
                    mBinding.modalResume.btnYes.requestFocus();
                    mBinding.modalResume.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mBinding.modalResume.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            new Handler(Looper.getMainLooper())
                                    .postDelayed(() -> mBinding.modalResume.btnYes.requestFocus(), 500);
                        }
                    });
                }
            }

            @Override
            public void onTracksShow() {
                super.onTracksShow();
                mBinding.includeModalSubtLang.audioTracks.requestFocus();
                mBinding.includeModalSubtLang.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                    mBinding.includeModalSubtLang.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(() -> {
                        new Handler().postDelayed(() -> mBinding.includeModalSubtLang.audioTracks.requestFocus(), 500);
                    });
                });
            }
        };

        mModel = new PlayerVodActivityModel(iptvPlayerVodListener);
        mBinding.setModel(mModel);
        mBinding.setListener(this);
        iptvPlayerVod = new IptvPlayerVod(this,
                getIntent(),
                PlayerVodActivity.class,
                mModel,
                mBinding.playerView, iptvPlayerVodListener);


        mModel.setShowController(true, false);

        iptvPlayerVod.setSeekbar(mBinding.playerMoviesControl.seekBarMovies);







        ImageView imageView = findViewById(R.id.imageView17);

        ObjectAnimator rotate = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        rotate.setDuration(2000);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setRepeatCount(ObjectAnimator.INFINITE);
        rotate.start();


    }

    @Override
    public void onStart() {
        super.onStart();
        iptvPlayerVod.exoplayerRelease(IptvPlayerVod.ON_START);
    }

    @Override
    public void onResume() {
        super.onResume();
        iptvPlayerVod.exoplayerRelease(IptvPlayerVod.ON_RESUME);
    }

    @Override
    public void onPause() {
        super.onPause();
        iptvPlayerVod.exoplayerRelease(IptvPlayerVod.ON_PAUSE);
    }

    @Override
    public void onStop() {
        super.onStop();
        iptvPlayerVod.exoplayerRelease(IptvPlayerVod.ON_STOP);
    }

    private void setupTracksist(List<ExoTracks> tracks, int type) {
        if (tracks == null || tracks.size() == 0)
            return;
        GenericAdapter<ItemTracksPlayerModel, ItemTracksPlayerBinding> adapter =
                new GenericAdapter<>(this, new GenericAdapter.GenericAdapterHelper<ItemTracksPlayerModel, ItemTracksPlayerBinding>() {
                    @Override
                    public ItemTracksPlayerBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                        return ItemTracksPlayerBinding.inflate(inflater, parent, false);
                    }

                    @Override
                    public Single<ItemTracksPlayerModel> getItem(int position) {
                        ExoTracks exoTracks = tracks.get(position);
                        return Single.just(new ItemTracksPlayerModel(exoTracks.trackName, exoTracks));
                    }

                    @Override
                    public Observable<Integer> getTotalItems() {
                        return tracks.size() > 0 ? Observable.just(tracks.size())
                                : Observable.just(0);
                    }

                    @Override
                    public void setModelToItem(ItemTracksPlayerBinding binding,
                                               ItemTracksPlayerModel item,
                                               int bindingAdapterPosition,
                                               GenericAdapter<ItemTracksPlayerModel, ItemTracksPlayerBinding> adapter) {
                        binding.setModel(item);
                        binding.getRoot().setOnClickListener(v -> {
                            iptvPlayerVod.changeTrackPlayer(item.getExoTracks());
                            mModel.setShowTracks(false);
                        });


                        // binding.getRoot().setOnKeyListener((v, keyCode, event) -> {
                        //     if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction()
                        //             == KeyEvent.ACTION_DOWN && bindingAdapterPosition == tracks
                        //             .size() - 1) {
                        //         if (type == C.TRACK_TYPE_VIDEO)
                        //             mBinding.tracksSubtitle.requestFocus();
                        //         else if (type == C.TRACK_TYPE_TEXT)
                        //             mBinding.tracksAudio.requestFocus();
                        //         return true;
                        //     }
                        //     if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction()
                        //             == KeyEvent.ACTION_DOWN && bindingAdapterPosition == 0) {
                        //         if (type == C.TRACK_TYPE_TEXT)
                        //             mBinding.tracksResolution.requestFocus();
                        //         else if (type == C.TRACK_TYPE_AUDIO)
                        //             mBinding.tracksSubtitle.requestFocus();
                        //         return true;
                        //     }
                        //     if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                        //         mBinding.linearLayout2.requestFocus();
                        //         return true;

                        //     }
                        //     return false;
                        // });
                    }
                });
        /*if (type == C.TRACK_TYPE_VIDEO)
            mBinding.tracksResolution.setAdapter(adapter);
        else*/ if (type == C.TRACK_TYPE_AUDIO)
            mBinding.includeModalSubtLang.audioTracks.setAdapter(adapter);
        else if (type == C.TRACK_TYPE_TEXT)
            mBinding.includeModalSubtLang.subtitleTracks.setAdapter(adapter);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (!mModel.isShowController()) {
                mModel.setShowController(true, true);
                new Handler().postDelayed(() -> mBinding.playerMoviesControl.imageView16.requestFocus(), 1000);
            } else mModel.setShowController(true, true);
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (mModel.isShowTracks()) {
                    mModel.setShowTracks(false);
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackActivity() {
        finish();
    }
}
