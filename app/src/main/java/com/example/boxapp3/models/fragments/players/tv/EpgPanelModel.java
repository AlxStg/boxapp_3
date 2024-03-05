package com.example.boxapp3.models.fragments.players.tv;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EpgPanelModel extends BaseObservable {
    private String duration;
    private String title;
    private String description;

    private String selectedMonth;

    public EpgPanelModel() {
        this.description = "";
        this.title = "";
        this.duration = "";

    }

    public EpgPanelModel(Date start, Date end, String title, String description) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        this.duration = sdf.format(start) + " - " + sdf.format(end);
        this.title = title;
        this.description = description;
        sdf = new SimpleDateFormat("MMM");
        this.selectedMonth = sdf.format(start);
    }

    @Bindable
    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
        notifyPropertyChanged(com.example.boxapp3.BR.duration);
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(com.example.boxapp3.BR.title);
    }

    @Bindable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        notifyPropertyChanged(com.example.boxapp3.BR.description);
    }

    @Bindable
    public String getSelectedMonth() {
        return selectedMonth;
    }

    public void setSelectedMonth(String selectedMonth) {
        this.selectedMonth = selectedMonth;
        notifyPropertyChanged(com.example.boxapp3.BR.selectedMonth);
    }
}
