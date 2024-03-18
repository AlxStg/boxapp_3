package com.example.boxapp3.listeners.models.activities;

public interface OnlyTvActivityModelListener {
    void onMenuClicked(String menu);
    void onParentalPasswordSet();
    void onModalExitCancel();
    void onModalExitConfirm();
    void onModalMobileConfirm();
    void onSearchIconClicked();
    void onModalMobileOpened();

    void modalReminderWatch(int streamId);
    void modalReminderClose();
    void modalReminderSportClose();
}
