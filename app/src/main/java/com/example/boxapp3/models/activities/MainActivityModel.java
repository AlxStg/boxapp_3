package com.example.boxapp3.models.activities;

import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;

public class MainActivityModel extends BaseObservable {

    private boolean showMenu = false;
    private boolean showMenuLabels = false;
    private boolean showModalAdult = false;


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
}
