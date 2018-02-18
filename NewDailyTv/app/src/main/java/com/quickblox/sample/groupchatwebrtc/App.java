package com.quickblox.sample.groupchatwebrtc;

import android.app.Application;

import com.example.user.dailytv.MyDatabase.GlobalVariable;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.groupchatwebrtc.util.QBResRequestExecutor;

public class App extends CoreApp {
    private static App instance;
    private QBResRequestExecutor qbResRequestExecutor;

    public static GlobalVariable global;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initApplication();
        global=new GlobalVariable();
    }



    private void initApplication(){
        instance = this;
    }

    public static GlobalVariable getGlobal()
    {
        return global;
    }


    public synchronized QBResRequestExecutor getQbResRequestExecutor() {

        //추적 2222222222222222222222222222222222222222

        return qbResRequestExecutor == null
                ? qbResRequestExecutor = new QBResRequestExecutor()
                : qbResRequestExecutor;

    }

    public class GlobalVariable {
        private String ip="18.218.144.214";

        public void setIp(String ip){
            this.ip=ip;
        }
        public String getIP() {
            return ip;
        }

    }


}
