package com.example.boxapp3.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.BuildConfig;
import com.example.boxapp3.R;
import com.example.boxapp3.databinding.ActivityMainBinding;
import com.example.boxapp3.databinding.ModalSairBinding;
import com.example.boxapp3.databinding.TopBarBinding;
import com.example.boxapp3.listeners.activities.MainActivityListener;
import com.example.boxapp3.listeners.fragments.KeyListener;
import com.example.boxapp3.listeners.fragments.MainFragmentListener;
import com.example.boxapp3.listeners.fragments.SearchFragmentListener;
import com.example.boxapp3.listeners.models.activities.MainActivityModelListener;
import com.example.boxapp3.models.activities.MainActivityModel;
import com.example.boxapp3.views.fragments.HomeFragment;
import com.example.boxapp3.views.fragments.MovieDetailsFragment;
import com.example.boxapp3.views.fragments.SearchFragment;
import com.example.boxapp3.views.fragments.SeriesDetailsFragment;
import com.example.boxapp3.views.fragments.TvFragment;
import com.example.boxapp3.views.fragments.VodListFragment;
import com.example.iptvsdk.common.centerContent.CenterContent;
import com.example.iptvsdk.common.menu.IptvMenu;
import com.example.iptvsdk.common.menu.IptvMenuListener;
import com.example.iptvsdk.data.models.xtream.StreamXc;
import com.example.iptvsdk.ui.list_streams_categories.ListStreamsCategories;
import com.example.iptvsdk.ui.mobile.IptvMobile;
import com.example.iptvsdk.ui.parental.IptvParental;

import java.util.ArrayList;


public class MainActivity extends BaseActivity implements MainActivityListener, MainActivityModelListener {

    private ActivityMainBinding mBinding;

    private MainActivityModel mModel;
    private CenterContent mCenterContent;
    private IptvMenu mIptvMenu;
    private IptvParental mIptvParental;
    private IptvMobile mIptvMobile;

    private String activeMenu = "home";

    private boolean adultAccessibile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mModel = new MainActivityModel(this);

        mIptvParental = new IptvParental(this);

        mBinding.setModel(mModel);
        mBinding.setListener(this);

        setupContent();

        setupMenu();
        setupSearch();

    }

    private void setupSearch() {
        mModel.searchQuery
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .doOnNext(s -> {
                    if (s.length() > 0) {
                        mCenterContent.changeFragement(new SearchFragment(s, this), false);
                    } else {
                        mCenterContent.backFragment();
                    }
                })
                .doOnError(th -> Log.e("MainActivity", "setupSearch: ", th))
                .subscribe();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mCenterContent.getCurrentFragment() instanceof KeyListener) {
                if (((KeyListener) mCenterContent.getCurrentFragment()).dispatchKeyEvent(event))
                    return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (mModel.getShowMenuLabels() && mCenterContent.getCurrentFragment() instanceof MainFragmentListener) {
                    ((MainFragmentListener) mCenterContent.getCurrentFragment()).firstFocus()
                            .requestFocus();
                    return true;
                }
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                if(((TopBarBinding)mBinding.includeTopBar).editTextText4.hasFocus()){
                    if(mCenterContent.getCurrentFragment() instanceof SearchFragmentListener){
                        ((SearchFragmentListener) mCenterContent.getCurrentFragment()).onFocused();
                        mModel.setShowSearchInput(false);
                        return true;
                    }
                }
                if (mModel.getShowModalAdult()) {
                    if (mBinding.include.editTextText2.hasFocus()) {
                        if (!mIptvParental.hasPassword())
                            mBinding.include.editTextText3.requestFocus();
                        else
                            mBinding.include.textView42.requestFocus();
                        return true;
                    }
                    if (mBinding.include.editTextText3.hasFocus()) {
                        mBinding.include.textView42.requestFocus();
                        return true;
                    }
                    if (mBinding.include
                            .textView42.hasFocus()) {
                        mBinding.include.editTextText2.requestFocus();
                        return true;
                    }
                    if (mBinding.include.btnCancelModalAdultEnter.hasFocus()) {
                        mBinding.include.editTextText2.requestFocus();
                        return true;
                    }
                }
            }

            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                if (mModel.getShowModalAdult()) {
                    if (mBinding.include.editTextText3.hasFocus()) {
                        mBinding.include.editTextText2.requestFocus();
                        return true;
                    }
                    if (mBinding.include.textView42.hasFocus()) {
                        if (!mIptvParental.hasPassword())
                            mBinding.include.editTextText3.requestFocus();
                        else
                            mBinding.include.editTextText2.requestFocus();
                        return true;
                    }
                }
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) {
                if (mModel.getShowModalAdult()) {
                    mBinding.include.editTextText2.setText("");
                    mBinding.include.editTextText3.setText("");
                    mModel.setShowModalAdult(false);
                    return true;
                }

                if (mModel.getShowModalExit()) {
                    mModel.setShowModalExit(false);
                    return true;
                }

                if(mModel.getShowModalMobile()){
                    mModel.setShowModalMobile(false);
                    return true;
                }

                if (mCenterContent.hasFragmentToBack()) {
                    mCenterContent.backFragment();
                } else {
                    mModel.setShowModalExit(true);
                    new Handler().postDelayed(() -> {
                        if (mModel.getShowModalExit())
                            ((ModalSairBinding) mBinding.modalExit).btnYes.requestFocus();
                    }, 1000);
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
        mCenterContent.addFragment("live", new TvFragment(this));
    }

    private void setupMenu() {
        mIptvMenu = new IptvMenu(new IptvMenuListener() {
            @Override
            public void onMenuColapse(boolean colapse) {
                super.onMenuColapse(colapse);
                mModel.setShowMenuLabels(!colapse);
            }
        });
        ((View) mBinding.includeMenu.menu).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((View) mBinding.includeMenu.menu).getViewTreeObserver()
                        .removeOnGlobalLayoutListener(this);
                mIptvMenu.listenMenuFocusAndColapse(BuildConfig.FLAVOR, mBinding.includeMenu.menu,
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
        mModel.setActualMenu(menu);
        activeMenu = menu;

        if (menu.equals("adults")) {
            if (!adultAccessibile) {
                mBinding.include.setRegisterPassword(!mIptvParental.hasPassword());
                mModel.setShowModalAdult(true);
                mBinding.include.editTextText2.requestFocus();
                return;
            }
        } else adultAccessibile = false;
        mCenterContent.showFragment(menu);
    }

    @Override
    public void onParentalPasswordSet() {
        if (mIptvParental.hasPassword()) {
            adultAccessibile = mIptvParental.checkPassword(mBinding.include.editTextText2.getText().toString());
            if (!adultAccessibile) {
                mBinding.include.textView41.setText(R.string.wrong_password);
                return;
            }
            mModel.setShowModalAdult(false);
            mCenterContent.showFragment("adults");
        } else {
            adultAccessibile = mIptvParental.setPassword(mBinding.include.editTextText2.getText().toString(),
                    mBinding.include.editTextText3.getText().toString());
            if (!adultAccessibile) {
                mBinding.include.textView41.setText(R.string.password_not_match);
                return;
            }
            mModel.setShowModalAdult(false);
            mCenterContent.showFragment("adults");
        }
        mBinding.include.editTextText2.setText("");
        mBinding.include.editTextText3.setText("");
    }

    @Override
    public void onModalExitCancel() {
        mModel.setShowModalExit(false);
        onGoToMenu();
    }

    @Override
    public void onModalExitConfirm() {
        finish();
        System.exit(0);
    }

    @Override
    public void onModalMobileConfirm() {
        mModel.setShowModalMobile(false);
    }

    @Override
    public void onSearchIconClicked() {
        ((TopBarBinding)mBinding.includeTopBar).editTextText4.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ((TopBarBinding)mBinding.includeTopBar).editTextText4.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                        ((TopBarBinding)mBinding.includeTopBar).editTextText4.requestFocus();
                    }
                });
    }

    @Override
    public void openDetails(int id, String type) {
        Fragment fragment = null;
        if (type.equals(StreamXc.TYPE_STREAM_LIVE)) {
            Intent intent = new Intent(this, PlayerTvActivity.class);
            intent.putExtra("streamId", id);
            startActivity(intent);
        } else if (type.equals(StreamXc.TYPE_STREAM_ADULTS)) {
            Intent intent = new Intent(this, PlayerTvActivity.class);
            intent.putExtra("streamId", id);
            intent.putExtra("isAdult", true);
            startActivity(intent);
        } else if (type.equals(StreamXc.TYPE_STREAM_VOD) || type.equals("kids")) {
            fragment = new MovieDetailsFragment(id, this);
        } else if (type.equals(StreamXc.TYPE_STREAM_SERIES)) {
            fragment = new SeriesDetailsFragment(id, this);
        }
        if (fragment != null)
            mCenterContent.changeFragement(fragment);
    }

    @Override
    public void onGoToMenu() {
        switch (activeMenu) {
            case "home":
                mBinding.includeMenu.btnHomeMenu.requestFocus();
                break;
            case "live":
                mBinding.includeMenu.btnTvMenu.requestFocus();
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

    @Override
    public void onGoToSearch() {
        mBinding.includeTopBar.imageView22.requestFocus();
    }

    @Override
    public void onPlayEpisode(int streamId, int episodeId) {
        Intent intent = new Intent(this, PlayerVodActivity.class);
        intent.putExtra("seriesId", streamId);
        intent.putExtra("id", episodeId);
        intent.putExtra("type", StreamXc.TYPE_STREAM_SERIES);
        startActivity(intent);
    }

    @Override
    public void onModalMobileOpened() {
        if(mIptvMobile == null)
            mIptvMobile = new IptvMobile(this, BuildConfig.IS_MOBILE);
        mBinding.includeModalMobile.getRoot()
                        .getViewTreeObserver()
                        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                mBinding.includeModalMobile.getRoot()
                                        .getViewTreeObserver()
                                        .removeOnGlobalLayoutListener(this);
                                mBinding.includeModalMobile.btnYes.requestFocus();
                            }
                        });
        mIptvMobile.getAccessCode()
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .doOnSuccess(s -> {
                    mModel.setMobileCode(s);
                })
                .doOnError(th -> Log.e("MainActivity", "onModalMobileOpened: ", th))
                .subscribe();
    }
}