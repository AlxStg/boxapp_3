package com.example.boxapp3.models.activities;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class LoginActivityModel extends BaseObservable {
    private String type;
    private String magUrl;
    private String macAddress;

    @Bindable
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        notifyPropertyChanged(com.example.boxapp3.BR.type);
    }

    @Bindable
    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
        notifyPropertyChanged(com.example.boxapp3.BR.macAddress);
    }

    @Bindable
    public String getMagUrl() {
        return magUrl;
    }

    public void setMagUrl(String magUrl) {
        this.magUrl = magUrl;
        notifyPropertyChanged(com.example.boxapp3.BR.magUrl);
    }
}
