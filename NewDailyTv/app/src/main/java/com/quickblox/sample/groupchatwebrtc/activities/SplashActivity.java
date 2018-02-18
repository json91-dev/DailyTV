package com.quickblox.sample.groupchatwebrtc.activities;

import android.os.Bundle;
import android.util.Log;

import com.example.user.dailytv.R;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.SharedPrefsHelper;

import com.quickblox.sample.groupchatwebrtc.services.CallService;
import com.quickblox.users.model.QBUser;

public class SplashActivity extends CoreSplashActivity {

    private SharedPrefsHelper sharedPrefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefsHelper = SharedPrefsHelper.getInstance();


        Log.e("퀵1","22222222222222222222");
        if (sharedPrefsHelper.hasQbUser()) {
            startLoginService(sharedPrefsHelper.getQbUser());
            startOpponentsActivity();
            return;
        }

        if (checkConfigsWithSnackebarError()) {
            proceedToTheNextActivityWithDelay();
        }
    }

    @Override
    protected String getAppName() {

        Log.e("퀵1","1111111111111111111");

        //return getString(R.string.splash_app_title);

        return "영상 채팅방으로 이동중입니다..";
    }

    @Override
    protected void proceedToTheNextActivity() {

        Log.e("퀵1","3333333333333333333333333");


        LoginActivity.start(this);
        finish();
    }

    protected void startLoginService(QBUser qbUser) {
        CallService.start(this, qbUser);
    }

    private void startOpponentsActivity() {

        Log.e("퀵1","44444444444444444444");


        OpponentsActivity.start(SplashActivity.this, false);
        finish();
    }
}