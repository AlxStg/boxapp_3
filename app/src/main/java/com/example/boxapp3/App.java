package com.example.boxapp3;

import com.example.iptvsdk.IptvApplication;


public class App extends IptvApplication {

    public App() {
        super(BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME,
                "simpletv",
                BuildConfig.IS_MOBILE
        );
    }
}
