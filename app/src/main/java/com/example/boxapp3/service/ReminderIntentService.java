package com.example.boxapp3.service;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.boxapp3.BuildConfig;
import com.example.boxapp3.views.activities.MainActivity;
import com.example.boxapp3.views.activities.OnlyTvActivity;
import com.example.iptvsdk.services.ReminderService;
import com.example.iptvsdk.ui.reminder.IptvReminder;

public class ReminderIntentService extends com.example.iptvsdk.services.ReminderIntentService {

    private ReminderService mReminderService;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        super.onHandleIntent(intent);

        if(intent == null)
            return;

        mReminderService = new ReminderService(getApplicationContext());

        String title = intent.getStringExtra("epgTitle") == null ? intent
                .getStringExtra("sportName") : intent.getStringExtra("epgTitle");

        mReminderService.getReminder(title, intent.getDoubleExtra("epgStart", 0))
                .doOnSuccess(reminders -> {
                    if (reminders != null && reminders.isActive()){
                        Intent i = new Intent(getApplicationContext(), BuildConfig.FLAVOR.equals("tunningNew")
                                || BuildConfig.FLAVOR.equals("tiger1") ?
                                OnlyTvActivity.class : MainActivity.class);
                        i = IptvReminder.passData(intent, i);

                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                })
                .subscribe();


    }
}
