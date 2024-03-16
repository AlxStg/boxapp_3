package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.databinding.FragmentOnlyTvChannelInfoBinding;
import com.example.boxapp3.databinding.PlayerControlsTvBinding;
import com.example.boxapp3.databinding.PlayerTimelineItemBinding;
import com.example.boxapp3.listeners.activities.OnlyTvActivityListener;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.data.models.EpgDb;
import com.example.iptvsdk.ui.live.IptvLive;
import com.example.iptvsdk.utils.DateUtils;

import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class OnlyTvChannelInfoFragment extends Fragment {

    private IptvLive mIptvLive;
    private OnlyTvActivityListener listener;
    private FragmentOnlyTvChannelInfoBinding mBinding;
    private int streamId;
    private boolean isZapping;


    public OnlyTvChannelInfoFragment(int streamId, IptvLive iptvLive,
                                     boolean isZapping, OnlyTvActivityListener listener) {
        this.streamId = streamId;
        this.listener = listener;
        this.mIptvLive = iptvLive;
        this.isZapping = isZapping;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentOnlyTvChannelInfoBinding.inflate(inflater, container, false);
        mBinding.setListener(listener);
        mBinding.textView23.setFormat24Hour("dd/MM/yyyy - HH:mm:ss");
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mIptvLive.getChannel(streamId)
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .doOnSuccess(streamXc -> {
                    mBinding.setStream(streamXc);
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
    }
}
