package com.example.boxapp3.models.activities;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class MainActivityModel extends BaseObservable {

    private boolean showMenu = false;
    private boolean showMenuLabels = false;
    private boolean showModalAdult, showModalExit = false;


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
}
