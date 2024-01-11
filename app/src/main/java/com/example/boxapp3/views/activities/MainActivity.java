package com.example.boxapp3.views.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import com.example.boxapp3.R;
import com.example.boxapp3.databinding.ActivityMainBinding;
import com.example.boxapp3.listeners.activities.MainActivityListener;
import com.example.boxapp3.listeners.models.activities.MainActivityModelListener;
import com.example.boxapp3.models.activities.MainActivityModel;
import com.example.boxapp3.views.fragments.HomeFragment;
import com.example.boxapp3.views.fragments.MoviesFragment;
import com.example.iptvsdk.common.centerContent.CenterContent;
import com.example.iptvsdk.common.menu.IptvMenu;
import com.example.iptvsdk.common.menu.IptvMenuListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.subjects.BehaviorSubject;


public class MainActivity extends AppCompatActivity implements MainActivityListener, MainActivityModelListener {

    private ActivityMainBinding mBinding;

    private MainActivityModel mModel;
    private CenterContent mCenterContent;
    private IptvMenu mIptvMenu;

    private List<View> menu;
    private BehaviorSubject<Boolean> mMenuFocus = BehaviorSubject.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mModel = new MainActivityModel();

        mBinding.setModel(mModel);
        mBinding.setListener(this);

        setupContent();

        setupMenu();

    }

    private void setupContent() {
        mCenterContent = new CenterContent(this,
                R.id.main_active_fragment,
                new HomeFragment());
        mCenterContent.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);

        mCenterContent.addFragment("home", new HomeFragment());
        mCenterContent.addFragment("movies", new MoviesFragment());
    }

    private void setupMenu() {
        mIptvMenu = new IptvMenu(new IptvMenuListener() {
            @Override
            public void onMenuColapse(boolean colapse) {
                super.onMenuColapse(colapse);
                mModel.setShowMenuLabels(!colapse);
            }
        });
       ((ConstraintLayout) mBinding.includeMenu.menu).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
           @Override
           public void onGlobalLayout() {
                ((ConstraintLayout) mBinding.includeMenu.menu).getViewTreeObserver()
                        .removeOnGlobalLayoutListener(this);
               mIptvMenu.listenMenuFocusAndColapse(mBinding.includeMenu.menu,
                       mBinding.includeMenu.menu.getWidth(),
                       100,
                       new ArrayList<View>(){{
                           add(mBinding.includeMenu.btnHomeMenu);
                           add(mBinding.includeMenu.btnTvMenu);
                           add(mBinding.includeMenu.btnMoviesMenu);
                           add(mBinding.includeMenu.btnSeriesMenu);
                           add(mBinding.includeMenu.btnKidsMenu);
                           add(mBinding.includeMenu.btnSportsMenu);
                           add(mBinding.includeMenu.btnAdultMenu);
                       }});
           }
       });
    }

    @Override
    public void onMenuSelected(String menu) {

    }

    @Override
    public void onMenuClicked(String menu) {
        mCenterContent.showFragment(menu);
    }
}