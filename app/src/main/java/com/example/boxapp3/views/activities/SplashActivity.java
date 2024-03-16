package com.example.boxapp3.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

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

    private ActivitySplashBinding mBinding;

    TranslateAnimation animation;
    TranslateAnimation animation2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         mBinding = DataBindingUtil.setContentView(this,
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

        if(BuildConfig.FLAVOR.equals("tunningNew")){
            showLoadingBalls(true);
        }

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
        Intent intent = new Intent(this, BuildConfig.ONLY_TV ?
                OnlyTvActivity.class : MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoadingBalls(boolean show) {
        mBinding.loading.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.loading.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                ImageView bounceBallImageBlue = mBinding.loading.bounceBallBlue;
                ImageView bounceBallImageOrange = mBinding.loading.bounceBallOrange;
                int parentWidth = ((View) bounceBallImageBlue.getParent()).getWidth();

                if (show) {
                    // Verifica se a animação já está em andamento
                    if (animation != null && !animation.hasEnded()) {
                        // Se sim, reinicie a animação
                        animation.cancel();
                        animation2.cancel();
                    }

                    // Crie as animações apenas se estiverem nulas ou terminadas
                    if (animation == null || animation.hasEnded()) {
                        animation = new TranslateAnimation(-1 * (parentWidth / 2 - bounceBallImageBlue.getWidth()), parentWidth / 2 - bounceBallImageBlue.getWidth(), 0, 0);
                        animation.setDuration(1000);
                        animation.setRepeatCount(Animation.INFINITE);
                        animation.setRepeatMode(Animation.REVERSE);
                    }
                    if (animation2 == null || animation2.hasEnded()) {
                        animation2 = new TranslateAnimation(parentWidth / 2 - bounceBallImageBlue.getWidth(), -1 * (parentWidth / 2 - bounceBallImageBlue.getWidth()), 0, 0);
                        animation2.setDuration(1000);
                        animation2.setRepeatCount(Animation.INFINITE);
                        animation2.setRepeatMode(Animation.REVERSE);
                    }

                    bounceBallImageBlue.startAnimation(animation);
                    bounceBallImageOrange.startAnimation(animation2);
                } else {
                    // Se show for falso, cancelar a animação
                    if (animation != null) {
                        animation.cancel();
                    }
                    if (animation2 != null) {
                        animation2.cancel();
                    }
                }
            }
        });
    }
}
