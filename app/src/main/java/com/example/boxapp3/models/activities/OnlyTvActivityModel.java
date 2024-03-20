package com.example.boxapp3.models.activities;

import android.util.Log;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.boxapp3.listeners.models.activities.OnlyTvActivityModelListener;

import io.reactivex.subjects.BehaviorSubject;

public class OnlyTvActivityModel extends BaseObservable {

    private boolean showMenu = true;
    private boolean showMenuLabels = false;
    private boolean showModalAdult, showModalExit, showModalMobile, showModalRemember,
            showModalRememberSport = false;
    private boolean showSearchInput = false;
    private boolean showLoadingPlayer = false;
    private boolean showSpeed = false;
    private boolean menuEnabled = false;
    private boolean seekbarEnabled = false;
    private String actualMenu = "home";
    private String search = "";
    private String mobileCode = "";
    private String speed = "";
    public BehaviorSubject<String> searchQuery = BehaviorSubject.create();

    private OnlyTvActivityModelListener listener;

    public OnlyTvActivityModel(OnlyTvActivityModelListener listener) {
        this.listener = listener;
    }

    @Bindable
    public boolean getShowMenu() {
        return showMenu;
    }

    public void setShowMenu(boolean showMenu) {
        this.showMenu = showMenu;
        notifyPropertyChanged(com.example.boxapp3.BR.showMenu);
    }

    @Bindable
    public boolean getShowModalAdult() {
        return showModalAdult;
    }

    public void setShowModalAdult(boolean showModalAdult) {
        this.showModalAdult = showModalAdult;
        notifyPropertyChanged(com.example.boxapp3.BR.showModalAdult);
    }

    @Bindable
    public boolean getShowMenuLabels() {
        return showMenuLabels;
    }

    public void setShowMenuLabels(boolean showMenuLabels) {
        this.showMenuLabels = showMenuLabels;
        Log.d("showMenuLabels", "setShowMenuLabels: " + showMenuLabels);
        notifyPropertyChanged(com.example.boxapp3.BR.showMenuLabels);
    }

    @Bindable
    public boolean getShowModalExit() {
        return showModalExit;
    }

    public void setShowModalExit(boolean showModalExit) {
        this.showModalExit = showModalExit;
        notifyPropertyChanged(com.example.boxapp3.BR.showModalExit);
    }

    @Bindable
    public String getActualMenu() {
        return actualMenu;
    }

    public void setActualMenu(String actualMenu) {
        this.actualMenu = actualMenu;
        notifyPropertyChanged(com.example.boxapp3.BR.actualMenu);
    }

    @Bindable
    public boolean getShowSearchInput() {
        return showSearchInput;
    }

    public void setShowSearchInput(boolean showSearchInput) {
        this.showSearchInput = showSearchInput;
        if(showSearchInput)
            listener.onSearchIconClicked();
        notifyPropertyChanged(com.example.boxapp3.BR.showSearchInput);
    }

    @Bindable
    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
        searchQuery.onNext(search);
        notifyPropertyChanged(com.example.boxapp3.BR.search);
    }

    @Bindable
    public String getMobileCode() {
        return mobileCode;
    }

    public void setMobileCode(String mobileCode) {
        this.mobileCode = mobileCode;
        notifyPropertyChanged(com.example.boxapp3.BR.mobileCode);
    }

    @Bindable
    public boolean getShowModalMobile() {
        return showModalMobile;
    }

    public void setShowModalMobile(boolean showModalMobile) {
        this.showModalMobile = showModalMobile;
        if(showModalMobile)
            listener.onModalMobileOpened();
        notifyPropertyChanged(com.example.boxapp3.BR.showModalMobile);
    }

    @Bindable
    public boolean getShowLoadingPlayer() {
        return showLoadingPlayer;
    }

    public void setShowLoadingPlayer(boolean showLoadingPlayer) {
        this.showLoadingPlayer = showLoadingPlayer;
        notifyPropertyChanged(com.example.boxapp3.BR.showLoadingPlayer);
    }

    @Bindable
    public boolean getShowSpeed() {
        return showSpeed;
    }

    public void setShowSpeed(boolean showSpeed) {
        this.showSpeed = showSpeed;
        if(!showSpeed)
            setSpeed("");
        notifyPropertyChanged(com.example.boxapp3.BR.showSpeed);
    }

    @Bindable
    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
        notifyPropertyChanged(com.example.boxapp3.BR.speed);
    }

    @Bindable
    public boolean getShowModalRemember() {
        return showModalRemember;
    }

    public void setShowModalRemember(boolean showModalRemember) {
        this.showModalRemember = showModalRemember;
        notifyPropertyChanged(com.example.boxapp3.BR.showModalRemember);
    }

    @Bindable
    public boolean getShowModalRememberSport() {
        return showModalRememberSport;
    }

    public void setShowModalRememberSport(boolean showModalRememberSport) {
        this.showModalRememberSport = showModalRememberSport;
        notifyPropertyChanged(com.example.boxapp3.BR.showModalRememberSport);
    }

    @Bindable
    public boolean getMenuEnabled() {
        return menuEnabled;
    }

    public void setMenuEnabled(boolean menuEnabled) {
        this.menuEnabled = menuEnabled;
        notifyPropertyChanged(com.example.boxapp3.BR.menuEnabled);
    }
}
