package com.example.boxapp3.models.fragments.players.tv;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class PanelsItem extends BaseObservable {
    private String name;
    private boolean active;
    private int id;

    public PanelsItem(String name, boolean active, int id) {
        this.name = name;
        this.active = active;
        this.id = id;
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(com.example.boxapp3.BR.name);
    }

    @Bindable
    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        notifyPropertyChanged(com.example.boxapp3.BR.active);
    }

    @Bindable
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        notifyPropertyChanged(com.example.boxapp3.BR.id);
    }
}
