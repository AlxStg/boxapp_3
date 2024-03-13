package com.example.boxapp3.views.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.example.boxapp3.BuildConfig;
import com.example.boxapp3.R;
import com.example.boxapp3.common.GenericUtils;
import com.example.boxapp3.databinding.ActivityLoginBinding;
import com.example.boxapp3.listeners.activities.LoginActivityListener;
import com.example.boxapp3.models.activities.LoginActivityModel;
import com.example.iptvsdk.common.IptvSettings;
import com.example.iptvsdk.ui.signin.IptvSignIn;
import com.example.iptvsdk.ui.signin.IptvSigninListener;
import com.example.iptvsdk.utils.NetworkUtils;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding mBinding;
    private IptvSettings mIptvSettings;
    private IptvSignIn mIptvSignIn;
    private LoginActivityModel mModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mModel = new LoginActivityModel();
        mIptvSettings = new IptvSettings(this);
        mIptvSignIn = new IptvSignIn(this, GenericUtils.getLoginType(), new IptvSigninListener() {
            @Override
            public void onSigninSuccess(Context context) {
                super.onSigninSuccess(context);
            }
        }, BuildConfig.LOAD_VOD, BuildConfig.LOAD_SERIES, BuildConfig.LOAD_LIVE,
                BuildConfig.LOAD_EPG, BuildConfig.LOAD_SPORTS, false);

        if(BuildConfig.LOGIN_XC_DATA)
            mModel.setType(IptvSignIn.LOGIN_TYPE_XC_DATA);
        else if(BuildConfig.LOGIN_URL)
            mModel.setType(IptvSignIn.LOGIN_TYPE_URL);
        else if(BuildConfig.LOGIN_MAG) {
            mModel.setType(IptvSignIn.LOGIN_TYPE_MAG);
            setupMag();
        }
        else
            mModel.setType(IptvSignIn.LOGIN_TYPE_XC_MIDDLEWARE);

        mBinding.setModel(mModel);

        mBinding.setListener(new LoginActivityListener() {
            @Override
            public void onMagLogin() {
                mIptvSignIn.saveMagAuth(mModel.getMacAddress(), mModel.getMagUrl());
            }
        });

    }

    private void setupMag() {

        String savedMac = mIptvSettings.getMagMac();

        if(savedMac != null)
            mModel.setMacAddress(savedMac);
        else
            mModel.setMacAddress(NetworkUtils.getMacWifi(this));

        String savedUrl = mIptvSettings.getMagUrl();
        if(savedUrl != null)
            mModel.setMagUrl(savedUrl);

    }
}
