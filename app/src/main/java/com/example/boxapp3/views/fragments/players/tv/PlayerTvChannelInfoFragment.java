package com.example.boxapp3.views.fragments.players.tv;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.databinding.FragmentTvPlayerChannelInfoBinding;
import com.example.boxapp3.databinding.PlayerControlsTvBinding;
import com.example.boxapp3.databinding.PlayerTimelineItemBinding;
import com.example.boxapp3.listeners.activities.PlayerTvActivityListener;
import com.example.boxapp3.listeners.fragments.KeyListener;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.data.models.EpgDb;
import com.example.iptvsdk.ui.live.IptvLive;
import com.example.iptvsdk.utils.DateUtils;

import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PlayerTvChannelInfoFragment extends Fragment implements KeyListener {

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

        mBinding.playerControlsTv.setListener(listener);

        mBinding.playerControlsTv.textView23.setFormat24Hour("dd/MM/yyyy - HH:mm");
        mBinding.playerControlsTv.imageView7.requestFocus();

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
                                    binding.setDaysPlayback(streamXc.getTvArchiveDuration());
                                    binding.getRoot().setOnClickListener(v -> {
                                        listener.onEpgClick(item, streamXc.getTvArchiveDuration());
                                    });
                                }
                            });
                    mBinding.playerControlsTv.horizontalScrollView.setAdapter(adapter);

                   adapter.totalItemsObservable
                           .subscribeOn(Schedulers.io())
                           .observeOn(AndroidSchedulers.mainThread())
                           .doOnError(th -> Log.e("TAG", "loadEpg: ", th))
                           .doOnNext(integer -> {
                               if (integer > 0) {
                                   mIptvLive.getActualEpgPosition(streamXc)
                                           .subscribeOn(Schedulers.io())
                                           .observeOn(AndroidSchedulers.mainThread())
                                           .doOnError(th -> Log.e("TAG", "loadEpg: ", th))
                                           .doOnSuccess(position -> {
                                               mBinding.playerControlsTv.horizontalScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                                   @Override
                                                   public void onGlobalLayout() {
                                                       mBinding.playerControlsTv.horizontalScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                                       mBinding.playerControlsTv.horizontalScrollView.scrollToPosition(position);
                                                       mBinding.playerControlsTv.horizontalScrollView.setSelectedPosition(position);
                                                   }
                                               });
                                           })
                                           .subscribe();
                               }
                           })
                           .subscribe();
                })
                .doOnError(throwable -> {
                    Log.e("PlayerTvChannelInfo", "Error getting channel", throwable);
                })
                .subscribe();

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        checkButtonsChannelbarFocus();
        return false;
    }

    private void checkButtonsChannelbarFocus() {
        PlayerControlsTvBinding playerControlsTv = mBinding.playerControlsTv;
        if(!(playerControlsTv.horizontalScrollView.hasFocus() || playerControlsTv
                .imageView7.hasFocus() || playerControlsTv.imageView15.hasFocus() || playerControlsTv
                .imageView16.hasFocus() || playerControlsTv.imageView18.hasFocus() || playerControlsTv
                .imageView19.hasFocus()))
            return;
        listener.onChannelInfoNavigate();
    }
}
