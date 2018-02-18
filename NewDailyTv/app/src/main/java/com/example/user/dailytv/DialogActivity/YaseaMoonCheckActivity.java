package com.example.user.dailytv.DialogActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.dailytv.R;
import com.quickblox.sample.groupchatwebrtc.App;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by user on 2017-11-02.
 */

public class YaseaMoonCheckActivity extends Activity {


    SharedPreferences shared;
    EditText title;

    Handler h=null;

    final App.GlobalVariable global=App.getGlobal();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //이부분 setContentview 이전에 선언해야함
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.yaseamooncheckactivity);

        InitUi();
    }

    private void InitUi()
    {
        Button button=(Button)findViewById(R.id.cancelBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent received=getIntent();
        String starcount=received.getExtras().getString("starcount");
        String reccount=received.getExtras().getString("reccount");

        TextView starcount_tv=(TextView)findViewById(R.id.starcount_tv);
        TextView reccount_tv=(TextView)findViewById(R.id.reccount_tv);

        starcount_tv.setText(starcount+"개");
        reccount_tv.setText(reccount+"회");

    }
}
