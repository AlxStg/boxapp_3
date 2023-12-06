package com.example.boxapp3;

import android.view.View;

public class MenuOnFocusChangeListener implements View.OnFocusChangeListener {

    MainActivity mainActivity;

    public MenuOnFocusChangeListener(MainActivity mainActivity, View vv) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        View lbl_home = v.findViewById(R.id.label_home_menu);
        View lbl_tv = v.findViewById(R.id.label_tv_menu);
        View lbl_movies = v.findViewById(R.id.menu_label_movies);
        View lbl_series = v.findViewById(R.id.menu_label_series);
        View lbl_kids = v.findViewById(R.id.menu_label_kids);
        View lbl_sports = v.findViewById(R.id.menu_label_sports);
        View lbl_adult = v.findViewById(R.id.menu_label_adult);

        mainActivity.menuHasFocus = hasFocus;
        mainActivity.menuColapse(hasFocus);
    }
}