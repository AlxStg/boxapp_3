package com.example.boxapp3;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.boxapp3.databinding.ActivityMainBinding;
import com.example.boxapp3.models.activities.MainActivityModel;
import com.example.iptvsdk.common.centerContent.CenterContent;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;

    private MainActivityModel mModel;
    private CenterContent mCenterContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mModel = new MainActivityModel();

        mBinding.setModel(mModel);

        mCenterContent = new CenterContent(this,
                R.id.main_active_fragment,
                new MenuTargetFragment(R.layout.fragment_home));
        mCenterContent.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);


    }

//    public void menuColapse(Boolean bool) {
//        // Get the menu view
//        View menu = findViewById(R.id.menu);
//
//        // Get the current width of the menu
//        int currentWidth = menu.getWidth();
//
//        if (bool) {
//            for (int i = 0; i < menuLabels.size(); i++) {
//                View lbl = (View) menuLabels.get(i);
//                lbl.setVisibility(View.VISIBLE);
//            }
//
//            // Calculate the new width of the menu when the labels are visible
//            int newWidth = currentWidth + 100;
//
//            // Animate the change in width
//            ValueAnimator anim = ValueAnimator.ofInt(currentWidth, newWidth);
//            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                    int val = (Integer) valueAnimator.getAnimatedValue();
//                    ViewGroup.LayoutParams layoutParams = menu.getLayoutParams();
//                    layoutParams.width = val;
//                    menu.setLayoutParams(layoutParams);
//                }
//            });
//            anim.setDuration(300);
//            anim.start();
//
//
//        } else {
//
//
//            // Calculate the new width of the menu when the labels are invisible
//            int newWidth = currentWidth - 100;
//
//            // Animate the change in width
//            ValueAnimator anim = ValueAnimator.ofInt(currentWidth, newWidth);
//            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                    int val = (Integer) valueAnimator.getAnimatedValue();
//                    ViewGroup.LayoutParams layoutParams = menu.getLayoutParams();
//                    layoutParams.width = val;
//                    menu.setLayoutParams(layoutParams);
//                }
//            });
//            anim.setDuration(100);
//            anim.start();
//            for (int i = 0; i < menuLabels.size(); i++) {
//                View lbl = (View) menuLabels.get(i);
//                lbl.setVisibility(View.GONE);
//            }
//        }
//    }

}