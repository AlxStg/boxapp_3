package com.example.boxapp3;

import com.example.iptvsdk.IptvApplication;

import java.util.ArrayList;
import java.util.List;


public class App extends IptvApplication {

    public App() {
        super(BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME,
                "simpletv",
                BuildConfig.IS_MOBILE
        );
    }

    public String[] getLoadContentTypes() {
        List<String> types = new ArrayList<>();
        if(BuildConfig.LOAD_LIVE)
            types.add(LOAD_CONTENT_TYPE_LIVE);

        if(BuildConfig.LOAD_VOD)
            types.add(LOAD_CONTENT_TYPE_VOD);

        if(BuildConfig.LOAD_SERIES)
            types.add(LOAD_CONTENT_TYPE_SERIES);

        if(BuildConfig.LOAD_SPORTS)
            types.add(LOAD_CONTENT_TYPE_SPORTS);

        return types.toArray(new String[0]);
    }
}
