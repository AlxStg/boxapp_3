package com.example.boxapp3;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class PlayerMoviesActivity extends AppCompatActivity {
    View btn_back_modal_movies_subt_lang;
    View btn_config_player_movies;
    View modal_subs_lang;
    VideoView videoView_movies;
    MainActivity mainActivity;
    View movie_info;
    View player_movies_control;
    SeekBar seekBar_movies;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_movies);

        videoView_movies = findViewById(R.id.videoView_movies);
        movie_info = findViewById(R.id.movie_info);
        player_movies_control = findViewById(R.id.player_movies_control);
        seekBar_movies = findViewById(R.id.seekBar_movies);
        btn_config_player_movies = findViewById(R.id.btn_config_player_movies);
        modal_subs_lang = findViewById(R.id.include_modal_subt_lang);

        // Set the video path
        //String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.clip_big_bang;
        //videoView_movies.setVideoURI(Uri.parse(videoPath));

        // Start the VideoView
        videoView_movies.start();

        btn_back_modal_movies_subt_lang = findViewById(R.id.btn_back_modal_movies_subtitles);
        btn_back_modal_movies_subt_lang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation slideOutLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left);
                modal_subs_lang.startAnimation(slideOutLeft);
                modal_subs_lang.setVisibility(View.GONE);
                btn_config_player_movies.requestFocus();
            }
        });

        PlayerControls_movies playerControls = new PlayerControls_movies(this, movie_info, player_movies_control, seekBar_movies, btn_config_player_movies, modal_subs_lang);
        playerControls.setupPlayerControls();
        playerControls.setupSeekBar(videoView_movies);
    }
}