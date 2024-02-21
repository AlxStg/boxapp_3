package com.example.boxapp3.models.fragments;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class TvFragmentModel extends BaseObservable {

    private boolean showNoEpgData = false;

    private boolean showEpg = false;
    private boolean showChannelOptions = true;

    @Bindable
    public boolean isShowNoEpgData() {
        return showNoEpgData;
    }

    public void setShowNoEpgData(boolean showNoEpgData) {
        this.showNoEpgData = showNoEpgData;
        notifyPropertyChanged(com.example.boxapp3.BR.showNoEpgData);
    }

    @Bindable
    public boolean isShowEpg() {
        return showEpg;
    }

    public void setShowEpg(boolean showEpg) {
        this.showEpg = showEpg;
        notifyPropertyChanged(com.example.boxapp3.BR.showEpg);
    }

    @Bindable
    public boolean isShowChannelOptions() {
        return showChannelOptions;
    }

    public void setShowChannelOptions(boolean showChannelOptions) {
        this.showChannelOptions = showChannelOptions;
        notifyPropertyChanged(com.example.boxapp3.BR.showChannelOptions);
    }
}
