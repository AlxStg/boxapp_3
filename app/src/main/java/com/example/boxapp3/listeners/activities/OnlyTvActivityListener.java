package com.example.boxapp3.listeners.activities;

import com.example.iptvsdk.data.models.xtream.Category;
import com.example.iptvsdk.data.models.xtream.StreamXc;

import java.util.Date;

public interface OnlyTvActivityListener {
    void onGoToMenu();
    void playChannel(StreamXc stream);
    void onEpgVisibilityChanged(boolean visible);
    void onCategorySelected(Category category);
    void onPlayback(int streamId, Date dateStart);
    void onShowPanels();
}
