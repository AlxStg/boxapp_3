package com.example.boxapp3.models.adapters;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.boxapp3.BR;
import com.example.iptvsdk.player.exo.ExoTracks;

public class ItemTracksPlayerModel extends BaseObservable {

    private ExoTracks exoTracks;
    private String trackName;


    public ItemTracksPlayerModel(String trackName, ExoTracks exoTracks){
        this.trackName = trackName;
        this.exoTracks = exoTracks;
    }

    @Bindable
    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
        notifyPropertyChanged(BR.trackName);
    }

    public ExoTracks getExoTracks() {
        return exoTracks;
    }
}
