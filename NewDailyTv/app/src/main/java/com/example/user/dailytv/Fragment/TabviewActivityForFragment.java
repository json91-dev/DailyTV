package com.example.user.dailytv.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.user.dailytv.Activities.TabviewActivity;
import com.example.user.dailytv.AsyncTasks.DownLoadProfileImage;
import com.example.user.dailytv.DialogActivity.TvPopupActivity;
import com.example.user.dailytv.R;
import com.quickblox.sample.groupchatwebrtc.activities.SplashActivity;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

/**
 * Created by user on 2018-01-05.
 */

public class TabviewActivityForFragment extends AppCompatActivity {

    Handler handler;

    private Fragment_home fragment_home;
    private Fragment_live fragment_live;

    private Fragment_video fragment_video;

    private Fragment_googlemap fragment_googlemap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabviewactivityforfragment);
        handler=new Handler();



        initUserInfo();

        fragment_home=new Fragment_home();
        fragment_live=new Fragment_live();
        fragment_video=new Fragment_video();
        fragment_googlemap=new Fragment_googlemap();

        initFragment();


        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {


            @Override
            public void onTabSelected(@IdRes int tabId) {
                //messageView.setText(TabMessage.get(tabId, false));
                FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();

                if(tabId==R.id.tab_home)
                {
                    transaction.replace(R.id.contentContainer,fragment_home).commit();
                }
                else if(tabId==R.id.tab_live)
                {
                    transaction.replace(R.id.contentContainer,fragment_live).commit();
                }

                else if(tabId==R.id.tab_video)
                {
                    transaction.replace(R.id.contentContainer,fragment_video).commit();
                }
                else if(tabId==R.id.tab_nearby)
                {
                    transaction.replace(R.id.contentContainer,fragment_googlemap).commit();
                }

            }
        });

        //initAnimation();


        /*
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                Toast.makeText(getApplicationContext(), TabMessage.get(tabId, true), Toast.LENGTH_LONG).show();
            }
        });
        */
    }

    public void initFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //transaction.add(R.id.contentContainer, fragment_home);
        transaction.add(R.id.contentContainer, fragment_googlemap);

        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void initUserInfo()
    {

        Intent intent=getIntent();

        SharedPreferences shared=getSharedPreferences("logininfo", Activity.MODE_PRIVATE);
        Toast.makeText(getApplicationContext(),shared.getString("nickname","")+"",Toast.LENGTH_LONG).show();
        SharedPreferences.Editor editor=shared.edit();

        //1. 이부분은 맨 마지막 최종 작품 완료시 설정하도록 한다.
        //2. 테스트용은 user1으로 id값을 설정한다.
        //editor.putString("userid",intent.getExtras().getString("id"));

        //editor.putString("userid","user1");
        //editor.putString("nickname","펭귄");


        editor.commit();
    }


}
