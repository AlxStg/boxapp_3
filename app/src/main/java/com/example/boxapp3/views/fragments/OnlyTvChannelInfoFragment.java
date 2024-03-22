package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.databinding.FragmentOnlyTvChannelInfoBinding;
import com.example.boxapp3.listeners.activities.OnlyTvActivityListener;
import com.example.boxapp3.listeners.fragments.KeyListener;
import com.example.boxapp3.listeners.fragments.OnlyTvChannelInfoFragmentListener;
import com.example.iptvsdk.data.models.xtream.StreamXc;
import com.example.iptvsdk.ui.favorite.IptvFavorite;
import com.example.iptvsdk.ui.live.IptvLive;
import com.example.iptvsdk.utils.DateUtils;
import com.example.iptvsdk.utils.ViewUtils;

import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class OnlyTvChannelInfoFragment extends Fragment implements KeyListener, OnlyTvChannelInfoFragmentListener {

    private IptvLive mIptvLive;
    private IptvFavorite mIptvFavorite;
    private OnlyTvActivityListener listener;
    private FragmentOnlyTvChannelInfoBinding mBinding;
    private int streamId;
    private StreamXc stream;
    private boolean isZapping;


    public OnlyTvChannelInfoFragment(int streamId, IptvLive iptvLive,
                                     boolean isZapping, OnlyTvActivityListener listener) {
        this.streamId = streamId;
        this.listener = listener;
        this.mIptvLive = iptvLive;
        this.isZapping = isZapping;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentOnlyTvChannelInfoBinding.inflate(inflater, container, false);
        mBinding.setListener(this);
        mBinding.textView23.setFormat24Hour("dd/MM/yyyy - HH:mm:ss");
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mIptvFavorite = new IptvFavorite(getContext());

        mIptvLive.getChannel(streamId)
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .doOnSuccess(streamXc -> {
                    this.stream = streamXc;
                    mBinding.setStream(streamXc);
                    mIptvFavorite.isFavorite(streamXc)
                            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                            .doOnSuccess(aBoolean -> {
                                mBinding.setIsFavorite(aBoolean);
                            })
                            .doOnError(throwable -> {
                                Log.e("PlayerTvChannelInfo", "Error getting favorite", throwable);
                            })
                            .subscribe();
                    mIptvLive.getActualEpg(streamXc)
                            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                            .doOnSuccess(epgDb -> {
                                int percentage = DateUtils.getPercentageBetweenDates(new Date(), epgDb.getStart(), epgDb.getEnd());
                                mBinding.setEpg(epgDb);
                                mBinding.setPercentageActualProgramme(percentage);
                            })
                            .doOnError(throwable -> {
                                Log.e("PlayerTvChannelInfo", "Error getting epg", throwable);
                            })
                            .subscribe();
                    mIptvLive.getNextEpg(streamXc)
                            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                            .doOnSuccess(epgDb -> {
                                mBinding.setNextEpg(epgDb);
                            })
                            .doOnError(throwable -> {
                                Log.e("PlayerTvChannelInfo", "Error getting next epg", throwable);
                            })
                            .subscribe();
                })
                .doOnError(throwable -> {
                    Log.e("PlayerTvChannelInfo", "Error getting channel", throwable);
                })
                .subscribe();

        mBinding.setSeekbarCanFocus(false);
        mBinding.textView71.requestFocus();

        ViewUtils.listenFocus(this, (view1, viewName) -> {
            if(view1 != null) {
                if(view1.getId() == mBinding.imageView72.getId()
                        || view1.getId() == mBinding.imageView7.getId()) {
                    listener.onBtnFocused();
                }
            }

        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(!mBinding.getSeekbarCanFocus())
            mBinding.setSeekbarCanFocus(true);
        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (mBinding.imageView72.hasFocus()){
                    onFavoriteClicked();
                    return true;
                }
                if(mBinding.imageView7.hasFocus()){
                    listener.onShowEpg();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onFavoriteClicked() {
        if(stream == null)
            return;
        mBinding.setIsFavorite(!mBinding.getIsFavorite());
        mIptvFavorite.toggleFavorite(stream)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(aBoolean -> {
                    mBinding.setIsFavorite(aBoolean);
                })
                .doOnError(throwable -> {
                    Log.e("PlayerTvChannelInfo", "Error toggling favorite", throwable);
                })
                .subscribe();
    }

    @Override
    public void onEpgClicked() {

    }
}
