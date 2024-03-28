package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.databinding.FragmentOnlyTvSearchBinding;
import com.example.boxapp3.databinding.ScrollTvChannelItemBinding;
import com.example.boxapp3.listeners.activities.OnlyTvActivityListener;
import com.example.boxapp3.models.fragments.OnlyTvSearchFragmentModel;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.data.models.xtream.StreamXc;
import com.example.iptvsdk.ui.live.IptvLive;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class OnlyTvSearchFragment extends Fragment {

    private FragmentOnlyTvSearchBinding mBinding;
    private OnlyTvSearchFragmentModel mModel;
    private OnlyTvActivityListener listener;
    private IptvLive mIptvLive;

    public OnlyTvSearchFragment(OnlyTvActivityListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentOnlyTvSearchBinding.inflate(inflater, container, false);

        mIptvLive = new IptvLive(getContext());

        mModel = new OnlyTvSearchFragmentModel();
        mBinding.setModel(mModel);

        setupList();
        listenSearch();

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        mBinding.tecladoPesquisa.requestFocus();
    }

    private void setupList() {
        GenericAdapter<StreamXc, ScrollTvChannelItemBinding> adapter =
                new GenericAdapter<>(getContext(), new GenericAdapter
                        .GenericAdapterHelper<StreamXc, ScrollTvChannelItemBinding>() {
            @Override
            public ScrollTvChannelItemBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                return ScrollTvChannelItemBinding.inflate(inflater, parent, false);
            }

            @Override
            public Single<StreamXc> getItem(int position) {
                return mIptvLive.getChannels(position);
            }

            @Override
            public Observable<Integer> getTotalItems() {
                return mIptvLive.getTotalChannels();
            }

            @Override
            public void setModelToItem(ScrollTvChannelItemBinding binding, StreamXc item, int bindingAdapterPosition, GenericAdapter<StreamXc, ScrollTvChannelItemBinding> adapter) {
                binding.setModel(item);

                binding.getRoot().setOnClickListener(v -> {
                    listener.playChannel(item.getStreamId());
                });

                mIptvLive.getActualEpg(item)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .doOnError(th -> Log.e("OnlyTvSearchFragment", "getItem: ", th))
                        .doOnSuccess(epgDb -> {
                            if(epgDb != null)
                                binding.setActualProgram(epgDb.getTitle());
                        })
                        .subscribe();

            }
        });
        mBinding.rvResultadosPesquisa.setAdapter(adapter);
    }

    private void listenSearch() {
        mModel.getSearchQuery()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnError(th -> Log.e("OnlyTvSearchFragment", "listenSearch: ", th))
                .doOnNext(search -> {
                    if(search != null && !search.isEmpty())
                        mIptvLive.setSearchQuery(search);
                })
                .subscribe();
    }
}
