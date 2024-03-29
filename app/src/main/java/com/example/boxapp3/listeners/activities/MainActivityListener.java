package com.example.boxapp3.listeners.activities;

public interface MainActivityListener {
    void openDetails(int id, String type);
    void onGoToMenu();
    void onGoToSearch();
    void onPlayEpisode(int id, int episodeId);
}
