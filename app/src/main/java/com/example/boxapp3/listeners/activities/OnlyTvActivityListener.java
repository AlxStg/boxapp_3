package com.example.boxapp3.listeners.activities;

import com.example.iptvsdk.data.models.xtream.StreamXc;

public interface OnlyTvActivityListener {
    void onGoToMenu();
    void playChannel(StreamXc stream);
    void onEpgVisibilityChanged(boolean visible);
}
