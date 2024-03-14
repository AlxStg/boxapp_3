package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.BuildConfig;
import com.example.boxapp3.databinding.FragmentMobileAppBinding;
import com.example.iptvsdk.ui.mobile.IptvMobile;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MobileAppFragment extends Fragment {

    private FragmentMobileAppBinding mBinding;
    private IptvMobile mIptvMobile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentMobileAppBinding.inflate(inflater, container, false);
        mIptvMobile = new IptvMobile(requireContext(), BuildConfig.IS_MOBILE);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mIptvMobile.getAccessCode()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("MobileAppFragment", "getAccessCode: ", th))
                .doOnSuccess(accessCode -> mBinding.setCode(accessCode))
                .subscribe();
    }
}
