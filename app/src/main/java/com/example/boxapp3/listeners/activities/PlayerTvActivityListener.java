package com.example.boxapp3.listeners.activities;

import com.example.iptvsdk.data.models.EpgDb;
import com.example.iptvsdk.data.models.xtream.StreamXc;

public interface PlayerTvActivityListener {
    void onChannelClick(StreamXc stream);
    void onChannelInfoNavigate();
    void onShowPanels();
    void onRewind();
    void onFastForward();

    void onEpgClick(EpgDb item, int daysPlayback);
    void setActualStreamPosition(int actualStreamPosition);
}
