package com.example.boxapp3.models.adapters;

import androidx.databinding.BaseObservable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ItemEpgDateModel extends BaseObservable {
    private String date;
    private String day;

    public ItemEpgDateModel(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        this.date = sdf.format(date);
        sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        this.day = sdf.format(date);
    }

    public String getDate() {
        return date;
    }

    public String getDay() {
        return day;
    }

    public void setDate(String date) {
        this.date = date;
        notifyChange();
    }

    public void setDay(String day) {
        this.day = day;
        notifyChange();
    }
}
