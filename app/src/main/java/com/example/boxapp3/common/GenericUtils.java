package com.example.boxapp3.common;

import com.example.boxapp3.BuildConfig;
import com.example.iptvsdk.ui.signin.IptvSignIn;

public class GenericUtils {

    public static String getLoginType() {
        if(BuildConfig.LOGIN_XC_DATA)
            return IptvSignIn.LOGIN_TYPE_XC_DATA;
        else if(BuildConfig.LOGIN_URL)
            return IptvSignIn.LOGIN_TYPE_URL;
        else if(BuildConfig.LOGIN_MAG)
            return IptvSignIn.LOGIN_TYPE_MAG;
        else
            return IptvSignIn.LOGIN_TYPE_XC_MIDDLEWARE;
    }
}
