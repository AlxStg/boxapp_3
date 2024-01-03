package com.example.boxapp3.models.activities;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class MainActivityModel extends BaseObservable {
    private boolean showMenu = false;
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
}
