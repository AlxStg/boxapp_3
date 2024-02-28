package com.example.boxapp3.models.activities;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.boxapp3.listeners.models.activities.MainActivityModelListener;

import io.reactivex.subjects.BehaviorSubject;

public class MainActivityModel extends BaseObservable {

    private boolean showMenu = false;
    private boolean showMenuLabels = false;
    private boolean showModalAdult, showModalExit, showModalMobile = false;
    private boolean showSearchInput = false;
    private String actualMenu = "home";
    private String search = "";
    private String mobileCode = "";
    public BehaviorSubject<String> searchQuery = BehaviorSubject.create();

    private MainActivityModelListener listener;

    public MainActivityModel(MainActivityModelListener listener) {
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
}
