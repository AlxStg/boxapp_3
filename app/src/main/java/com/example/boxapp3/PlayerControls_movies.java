package com.example.boxapp3;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.VideoView;

public class PlayerControls_movies {
    private View movie_info;
    private View player_movies_control;
    private SeekBar seekBar_movies;
    private View btn_config_player_movies;
    private View modal_subs_lang;
    private Context context;

    public PlayerControls_movies(Context context, View movie_info, View player_movies_control, SeekBar seekBar_movies, View btn_config_player_movies, View modal_subs_lang) {
        this.context = context;
        this.movie_info = movie_info;
        this.player_movies_control = player_movies_control;
        this.seekBar_movies = seekBar_movies;
        this.btn_config_player_movies = btn_config_player_movies;
        this.modal_subs_lang = modal_subs_lang;
    }

    public void setupPlayerControls() {
        btn_config_player_movies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(modal_subs_lang.getVisibility() != View.VISIBLE) {
                    Animation slideInLeft = AnimationUtils.loadAnimation(context, R.anim.slide_in_left);
                    modal_subs_lang.startAnimation(slideInLeft);
                    modal_subs_lang.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void setupSeekBar(VideoView videoView_movies) {
        videoView_movies.setFocusableInTouchMode(true);
        videoView_movies.requestFocus();
        videoView_movies.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (movie_info.getVisibility() == View.GONE && player_movies_control.getVisibility() == View.GONE) {
                        Animation slideInRight = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
                        movie_info.startAnimation(slideInRight);
                        movie_info.setVisibility(View.VISIBLE);

                        Animation slideInUp = AnimationUtils.loadAnimation(context, R.anim.slide_in_up);
                        player_movies_control.startAnimation(slideInUp);
                        player_movies_control.setVisibility(View.VISIBLE);
                        btn_config_player_movies.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });
    }
}