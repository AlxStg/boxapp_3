package com.example.boxapp3;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class MenuOnClickListener implements View.OnClickListener {

    private final MainActivity mainActivity;
    private final int layoutId;

    public MenuOnClickListener(MainActivity mainActivity, int layoutId) {
        this.mainActivity = mainActivity;
        this.layoutId = layoutId;
    }

    @Override
    public void onClick(View v) {
        if (mainActivity.getFragmentOnScreen() != layoutId) {
            v.setVisibility(View.VISIBLE);
            Fragment fragment = new MenuTargetFragment(layoutId);
            FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
            // Clear back stack
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Set custom animations
            fragmentTransaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
            fragmentTransaction.replace(R.id.main_active_fragment, fragment);
            fragmentTransaction.commit();
            mainActivity.setFragmentOnScreen(layoutId);
        }
    }
}