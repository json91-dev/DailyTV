package com.example.user.dailytv.MyDatabase;

import android.app.Application;

import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.groupchatwebrtc.util.QBResRequestExecutor;

/**
 * Created by user on 2017-11-01.
 */

public class GlobalVariable extends Application {
    private static final String ip="18.216.199.78";

    public String getIP() {
        return ip;
    }

}



