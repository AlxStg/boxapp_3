package com.example.boxapp3.views.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.R;
import com.example.boxapp3.databinding.ActivityMainBinding;
import com.example.boxapp3.listeners.activities.MainActivityListener;
import com.example.boxapp3.listeners.fragments.KeyListener;
import com.example.boxapp3.listeners.fragments.MainFragmentListener;
import com.example.boxapp3.listeners.models.activities.MainActivityModelListener;
import com.example.boxapp3.models.activities.MainActivityModel;
import com.example.boxapp3.views.fragments.HomeFragment;
import com.example.boxapp3.views.fragments.MovieDetailsFragment;
import com.example.boxapp3.views.fragments.SeriesDetailsFragment;
import com.example.boxapp3.views.fragments.TvFragment;
import com.example.boxapp3.views.fragments.VodListFragment;
import com.example.iptvsdk.common.centerContent.CenterContent;
import com.example.iptvsdk.common.menu.IptvMenu;
import com.example.iptvsdk.common.menu.IptvMenuListener;
import com.example.iptvsdk.data.models.xtream.StreamXc;
import com.example.iptvsdk.ui.list_streams_categories.ListStreamsCategories;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MainActivityListener, MainActivityModelListener {

    private ActivityMainBinding mBinding;

    private MainActivityModel mModel;
    private CenterContent mCenterContent;
    private IptvMenu mIptvMenu;

    private String activeMenu = "home";

    private boolean adultAccessibile = false;

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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            if(mCenterContent.getCurrentFragment() instanceof KeyListener){
                if(((KeyListener) mCenterContent.getCurrentFragment()).dispatchKeyEvent(event))
                    return true;
            }
            if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT){
                if(mModel.getShowMenuLabels() && mCenterContent.getCurrentFragment() instanceof MainFragmentListener){
                    ((MainFragmentListener) mCenterContent.getCurrentFragment()).firstFocus()
                            .requestFocus();
                    return true;
                }
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) {
                if(mModel.getShowModalAdult()) {
                    mModel.setShowModalAdult(false);
                    return true;
                }

                if(mModel.getShowModalExit()) {
                    mModel.setShowModalExit(false);
                    return true;
                }

                if (mCenterContent.hasFragmentToBack()) {
                    mCenterContent.backFragment();
                } else {
                    mModel.setShowModalExit(true);
                }
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    private void setupContent() {
        mCenterContent = new CenterContent(this,
                R.id.main_active_fragment,
                new HomeFragment(this));
        mCenterContent.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);

        mCenterContent.addFragment("home", new HomeFragment(this));
        mCenterContent.addFragment("movies", new VodListFragment(StreamXc.TYPE_STREAM_VOD,
                this));
        mCenterContent.addFragment("series", new VodListFragment(StreamXc.TYPE_STREAM_SERIES,
                this));
        mCenterContent.addFragment("kids", new VodListFragment(ListStreamsCategories.TYPE_KIDS,
                this));
        mCenterContent.addFragment("adults", new VodListFragment(ListStreamsCategories.TYPE_ADULTS,
                this));
        mCenterContent.addFragment("live", new TvFragment());
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
                        new ArrayList<View>() {{
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
    public void onMenuClicked(String menu) {
        activeMenu = menu;

        if(menu.equals("adults")){
            if(adultAccessibile) {
                mModel.setShowModalAdult(true);
                return;
            }
        } else adultAccessibile = false;
        mCenterContent.showFragment(menu);
    }

    @Override
    public void openDetails(int id, String type) {
        Fragment fragment = null;
        if (type.equals(StreamXc.TYPE_STREAM_VOD) || type.equals("kids")) {
            fragment = new MovieDetailsFragment(id, this);
        } else if (type.equals(StreamXc.TYPE_STREAM_SERIES)) {
            fragment = new SeriesDetailsFragment(id, this);
        }
        if (fragment != null)
            mCenterContent.changeFragement(fragment);
    }

    @Override
    public void onGoToMenu() {
        switch (activeMenu){
            case "home":
                mBinding.includeMenu.btnHomeMenu.requestFocus();
                break;
            case "movies":
                mBinding.includeMenu.btnMoviesMenu.requestFocus();
                break;
            case "series":
                mBinding.includeMenu.btnSeriesMenu.requestFocus();
                break;
            case "kids":
                mBinding.includeMenu.btnKidsMenu.requestFocus();
                break;
            case "sports":
                mBinding.includeMenu.btnSportsMenu.requestFocus();
                break;
            case "adult":
                mBinding.includeMenu.btnAdultMenu.requestFocus();
                break;
        }
    }
}