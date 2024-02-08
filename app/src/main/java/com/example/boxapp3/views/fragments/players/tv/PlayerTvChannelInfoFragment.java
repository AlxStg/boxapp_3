package com.example.boxapp3.views.fragments.players.tv;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.databinding.FragmentTvPlayerChannelInfoBinding;
import com.example.boxapp3.databinding.PlayerTimelineItemBinding;
import com.example.boxapp3.listeners.activities.PlayerTvActivityListener;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.data.models.EpgDb;
import com.example.iptvsdk.ui.live.IptvLive;
import com.example.iptvsdk.utils.DateUtils;

import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.Single;

public class PlayerTvChannelInfoFragment extends Fragment {

    private IptvLive mIptvLive;
    private PlayerTvActivityListener listener;
    private FragmentTvPlayerChannelInfoBinding mBinding;
    private int streamId;

    public PlayerTvChannelInfoFragment(int streamId, IptvLive iptvLive,
                                       PlayerTvActivityListener listener) {
        this.streamId = streamId;
        this.listener = listener;
        this.mIptvLive = iptvLive;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTvPlayerChannelInfoBinding.inflate(inflater, container, false);

        mBinding.playerControlsTv.textView23.setFormat24Hour("dd/MM/yyyy - HH:mm:ss");

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mIptvLive.getChannel(streamId)
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .doOnSuccess(streamXc -> {
                    mBinding.playerChannelInfo.setStream(streamXc);
                    mIptvLive.getActualEpg(streamXc)
                            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                            .doOnSuccess(epgDb -> {
                                int percentage = DateUtils.getPercentageBetweenDates(new Date(), epgDb.getStart(), epgDb.getEnd());
                                mBinding.playerControlsTv.setEpg(epgDb);
                                mBinding.playerChannelInfo.setEpg(epgDb);
                                mBinding.playerControlsTv.setPercentageActualProgramme(percentage);
                            })
                            .doOnError(throwable -> {
                                Log.e("PlayerTvChannelInfo", "Error getting epg", throwable);
                            })
                            .subscribe();
                    GenericAdapter<EpgDb, PlayerTimelineItemBinding> adapter = new GenericAdapter<>(getContext(),
                            new GenericAdapter.GenericAdapterHelper<EpgDb, PlayerTimelineItemBinding>() {
                                @Override
                                public PlayerTimelineItemBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                                    return PlayerTimelineItemBinding.inflate(inflater, parent, false);
                                }

                                @Override
                                public Single<EpgDb> getItem(int position) {
                                    return mIptvLive.getEpg(streamXc, position);
                                }

                                @Override
                                public Observable<Integer> getTotalItems() {
                                    return mIptvLive.getTotalEpg(streamXc);
                                }

                                @Override
                                public void setModelToItem(PlayerTimelineItemBinding binding, EpgDb item, int bindingAdapterPosition, GenericAdapter<EpgDb, PlayerTimelineItemBinding> adapter) {
                                    binding.setModel(item);
                                }
                            });
                    mBinding.playerControlsTv.horizontalScrollView.setAdapter(adapter);
                })
                .doOnError(throwable -> {
                    Log.e("PlayerTvChannelInfo", "Error getting channel", throwable);
                })
                .subscribe();

    }
}
