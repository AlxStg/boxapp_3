package com.example.boxapp3.models.fragments;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class TvFragmentModel extends BaseObservable {

    private boolean showNoEpgData = false;

    @Bindable
    public boolean isShowNoEpgData() {
        return showNoEpgData;
    }

    public void setShowNoEpgData(boolean showNoEpgData) {
        this.showNoEpgData = showNoEpgData;
        notifyPropertyChanged(com.example.boxapp3.BR.showNoEpgData);
    }
}
