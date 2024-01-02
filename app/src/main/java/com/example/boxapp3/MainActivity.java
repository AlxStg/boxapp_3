package com.example.boxapp3;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView bgModal;

    View lbl_home;
    View lbl_tv;
    View lbl_movies;
    View lbl_series;
    View lbl_kids;
    View lbl_sports;
    View lbl_adult;

    List menuLabels;

    int fragment_on_screen;
    int btn_home;
    int btn_tv;
    int btn_movies;
    int btn_series;
    int btn_kids;
    int btn_sports;
    int btn_adult;

    int include_active_fragment;

    public static Boolean menuHasFocus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // NÃO REMOVER
        // É necessário criar um fragmento inicial para que o aplicativo não fique em branco
        Fragment fragment = new MenuTargetFragment(R.layout.fragment_home);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        // Clear back stack
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Set custom animations
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.main_active_fragment, fragment);
        fragmentTransaction.commit();
        this.setFragmentOnScreen(R.layout.fragment_home);



        btn_home = R.id.btn_home_menu;
        findViewById(btn_home).requestFocus();
        btn_tv = R.id.btn_tv_menu;
        btn_movies = R.id.btn_movies_menu;
        btn_series = R.id.btn_series_menu;
        btn_kids = R.id.btn_kids_menu;
        btn_sports = R.id.btn_sports_menu;
        btn_adult = R.id.btn_adult_menu;

        lbl_home = findViewById(R.id.label_home_menu);
        lbl_tv = findViewById(R.id.label_tv_menu);
        lbl_movies = findViewById(R.id.menu_label_movies);
        lbl_series = findViewById(R.id.menu_label_series);
        lbl_kids = findViewById(R.id.menu_label_kids);
        lbl_sports = findViewById(R.id.menu_label_sports);
        lbl_adult = findViewById(R.id.menu_label_adult);

        menuLabels = Arrays.asList(lbl_home, lbl_tv, lbl_movies, lbl_series, lbl_kids, lbl_sports, lbl_adult);

        include_active_fragment = R.id.main_active_fragment;

        findViewById(btn_home).setOnClickListener(new MenuOnClickListener(this, R.layout.fragment_home));
        findViewById(btn_tv).setOnClickListener(new MenuOnClickListener(this, btn_tv));
        findViewById(btn_movies).setOnClickListener(new MenuOnClickListener(this, R.layout.fragment_movie_especific));
        findViewById(btn_series).setOnClickListener(new MenuOnClickListener(this, R.layout.fragment_movies));
//        findViewById(btn_kids).setOnClickListener(new MenuClickListener(this, R.layout.fragment_kids));
//        findViewById(btn_sports).setOnClickListener(new MenuOnClickListener(this, R.layout.fragment_sports));
//        findViewById(btn_adult).setOnClickListener(new MenuClickListener(this, R.layout.fragment_adult));
        findViewById(btn_home).setOnFocusChangeListener(new MenuOnFocusChangeListener(this, findViewById(btn_home)));
        findViewById(btn_tv).setOnFocusChangeListener(new MenuOnFocusChangeListener(this, findViewById(btn_tv)));
        findViewById(btn_movies).setOnFocusChangeListener(new MenuOnFocusChangeListener(this, findViewById(btn_movies)));
        findViewById(btn_series).setOnFocusChangeListener(new MenuOnFocusChangeListener(this, findViewById(btn_series)));
        findViewById(btn_kids).setOnFocusChangeListener(new MenuOnFocusChangeListener(this, findViewById(btn_kids)));
        findViewById(btn_sports).setOnFocusChangeListener(new MenuOnFocusChangeListener(this, findViewById(btn_sports)));
        findViewById(btn_adult).setOnFocusChangeListener(new MenuOnFocusChangeListener(this, findViewById(btn_adult)));

        for (int i = 0; i < menuLabels.size(); i++) {
            View lbl = (View) menuLabels.get(i);
            lbl.setVisibility(View.GONE);
        }

        // ... other code ...

//findViewById(btn_tv).setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View v) {
//        Intent intent = new Intent(MainActivity.this, PlayerTvActivity.class);
//        startActivity(intent);
//    }
//});



    }



    public int getFragmentOnScreen() {
        return fragment_on_screen;
    }

    public void setFragmentOnScreen(int layoutId) {
        this.fragment_on_screen = layoutId;
    }


    public void setInvisibleAnyId(int id) {
        View view = findViewById(id);
        view.setVisibility(View.INVISIBLE);
    }

    public void menuColapse(Boolean bool) {
        // Get the menu view
        View menu = findViewById(R.id.menu);

        // Get the current width of the menu
        int currentWidth = menu.getWidth();

        if (bool) {
            for (int i = 0; i < menuLabels.size(); i++) {
                View lbl = (View) menuLabels.get(i);
                lbl.setVisibility(View.VISIBLE);
            }

            // Calculate the new width of the menu when the labels are visible
            int newWidth = currentWidth + 100;

            // Animate the change in width
            ValueAnimator anim = ValueAnimator.ofInt(currentWidth, newWidth);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = menu.getLayoutParams();
                    layoutParams.width = val;
                    menu.setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(300);
            anim.start();


        } else {


            // Calculate the new width of the menu when the labels are invisible
            int newWidth = currentWidth - 100;

            // Animate the change in width
            ValueAnimator anim = ValueAnimator.ofInt(currentWidth, newWidth);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = menu.getLayoutParams();
                    layoutParams.width = val;
                    menu.setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(100);
            anim.start();
            for (int i = 0; i < menuLabels.size(); i++) {
                View lbl = (View) menuLabels.get(i);
                lbl.setVisibility(View.GONE);
            }
        }
    }

}