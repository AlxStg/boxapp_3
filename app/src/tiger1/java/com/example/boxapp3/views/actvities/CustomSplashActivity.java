package com.example.boxapp3.views.actvities;

import com.example.boxapp3.databinding.ActivitySplashBinding;
import com.example.boxapp3.views.activities.BaseActivity;

public class CustomSplashActivity extends BaseActivity {

    private ActivitySplashBinding mBinding;

    public void passBindingToSuper(ActivitySplashBinding binding) {
        mBinding = binding;
    }
}
