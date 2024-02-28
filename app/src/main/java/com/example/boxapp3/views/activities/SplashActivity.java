package com.example.boxapp3.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.example.boxapp3.BuildConfig;
import com.example.boxapp3.R;
import com.example.boxapp3.common.GenericUtils;
import com.example.boxapp3.databinding.ActivitySplashBinding;
import com.example.iptvsdk.IptvApplication;
import com.example.iptvsdk.ui.signin.IptvSignIn;
import com.example.iptvsdk.ui.signin.IptvSigninListener;
import com.example.iptvsdk.utils.DialogErrorUtil;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySplashBinding binding = DataBindingUtil.setContentView(this,
                R.layout.activity_splash);


        IptvApplication application = (IptvApplication) getApplication();

        IptvSignIn iptvSignIn = new IptvSignIn(this, GenericUtils.getLoginType(),
                new IptvSigninListener() {

            @Override
            public void onSigninSuccess(Context context) {
                super.onSigninSuccess(context);
                application.contentLoaded
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(loaded -> {
                            if(loaded)
                                goToMainActivity();
                        })
                        .doOnError(throwable -> {
                            DialogErrorUtil.showError(SplashActivity.this,
                                    throwable, getString(R.string.login_error),
                                    getString(R.string.an_error_occurred_while_trying_to_login_please_try_again_later));
                        })
                        .subscribe();
            }

            @Override
            public void onMissingLogin() {
                super.onMissingLogin();
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }, false);
        removerColumnsSaved();

    }

    private void removerColumnsSaved() {
        getSharedPreferences("app", MODE_PRIVATE)
                .edit()
                .remove("currentRow")
                .remove("currentColumn")
                .remove("lastStreamId")
                .apply();
    }

    private boolean alreayGoToMainActivity = false;

    private void goToMainActivity() {
        if(alreayGoToMainActivity)
            return;
        alreayGoToMainActivity = true;
        Intent intent = new Intent(this, BuildConfig.FLAVOR.equals("tunningNew") ?
                OnlyTvActivity.class : MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
