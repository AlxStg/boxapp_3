package com.example.boxapp3.views.actvities;

import android.view.ViewTreeObserver;

import androidx.fragment.app.Fragment;

import com.example.boxapp3.databinding.ActivityOnlyTvBinding;
import com.example.boxapp3.models.activities.OnlyTvActivityModel;
import com.example.boxapp3.views.activities.BaseActivity;
import com.example.boxapp3.views.fragments.MobileAppFragment;
import com.example.boxapp3.views.fragments.OnlyTvPanelsFragment;
import com.example.boxapp3.views.fragments.ParentalFragment;
import com.example.boxapp3.views.fragments.SportFragment;
import com.example.iptvsdk.common.centerContent.CenterContent;
import com.example.iptvsdk.common.centerContent.CenterContentListener;

public class CustomOnlyTvActivity  extends BaseActivity {

    protected void setCenterContent(ActivityOnlyTvBinding mBinding, OnlyTvActivityModel mModel, CenterContent centerContent) {
        mBinding.constraintLayout11.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.constraintLayout11.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                centerContent.setCenterContentListener(new CenterContentListener() {
                    @Override
                    public void onFragmentChange(Fragment fragment) {
                        boolean showTopBar = fragment
                                instanceof OnlyTvPanelsFragment || fragment
                                instanceof SportFragment || fragment
                                instanceof MobileAppFragment ||
                                fragment
                                        instanceof ParentalFragment;
                        mModel.setShowTopBar(showTopBar);

                        mBinding.imageView32.setSelected(mModel.getShowMenu());
                        if(!mModel.getShowMenu() && !(fragment instanceof SportFragment))
                            mBinding.linearLayout19.setSelected(true);
                        if(!mModel.getShowMenu() && fragment instanceof SportFragment)
                            mBinding.linearLayout192.setSelected(true);
                    }
                });
            }
        });
    }


    public void onGoToChannelTopBar(ActivityOnlyTvBinding binding) {
        binding.linearLayout19.requestFocus();
    }

    public void onGoToSportTopBar(ActivityOnlyTvBinding binding) {
        binding.linearLayout192.requestFocus();
    }

    public void onGoToMenuTopBar(ActivityOnlyTvBinding binding) {
        binding.imageView32.requestFocus();
    }
}
