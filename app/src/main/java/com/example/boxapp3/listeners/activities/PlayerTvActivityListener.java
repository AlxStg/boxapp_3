package com.example.boxapp3.listeners.activities;

import com.example.iptvsdk.data.models.xtream.StreamXc;

public interface PlayerTvActivityListener {
    void onChannelClick(StreamXc stream);
    void onChannelInfoNavigate();
    void onShowPanels();
    void onRewind();
    void onFastForward();
}
