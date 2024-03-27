package com.example.boxapp3.views.actvities;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.example.boxapp3.databinding.ActivitySplashBinding;
import com.example.boxapp3.views.activities.BaseActivity;

public class CustomSplashActivity extends BaseActivity {

    private ActivitySplashBinding mBinding;
    TranslateAnimation animation;
    TranslateAnimation animation2;


    public void passBindingToSuper(ActivitySplashBinding binding) {
        mBinding = binding;
        showLoadingBalls(true);
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
