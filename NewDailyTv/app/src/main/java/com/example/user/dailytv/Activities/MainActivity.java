package com.example.user.dailytv.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;


import com.example.user.dailytv.DialogActivity.TvPopupActivity;
import com.example.user.dailytv.Fragment.TabviewActivityForFragment;
import com.example.user.dailytv.Module.MyVideoView;
import com.example.user.dailytv.R;
import com.quickblox.sample.groupchatwebrtc.App;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    EditText idtext,pwtext;

    String LOGINURL="";


    SharedPreferences shared;

    //LoginButton facebook_loginbutton;

    //CallbackManager callbackManager;

    final App.GlobalVariable global=App.getGlobal();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        //FacebookSdk.sdkInitialize(this.getApplicationContext());


        InitLogin();


        final MyVideoView backVideoView=(MyVideoView)findViewById(R.id.backVideoView);

        Uri uri=Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.loginvideo);
        //Toast.makeText(getApplicationContext(),"android.resource://"+getPackageName()+"/"+R.raw.loginvideo,Toast.LENGTH_LONG).show();

        backVideoView.setVideoURI(uri);
        backVideoView.start();
        backVideoView.setSoundEffectsEnabled(false);

        //반복재생하기
        backVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                backVideoView.start();
            }
        });


        //Intent intent=new Intent(MainActivity.this,YaseaStreamActivity.class);
        //Intent i=new Intent(getApplication(),TabviewActivity.class);
        //Intent i=new Intent(getApplication(),BuyMoonActivity.class);
        //Intent i=new Intent(getApplication(),TabviewActivityForFragment.class);

        //Intent i=new Intent(getApplication(),ArgumentRealityExampleActivity.class);

        //startActivity(i);

        //finish();

    }

    private void InitLogin()
    {
        idtext=(EditText)findViewById(R.id.idtext);
        pwtext=(EditText)findViewById(R.id.pwtext);


        Log.e("globalip",global.getIP()+"");
        LOGINURL="http://"+global.getIP()+"/login.php";

        shared=getSharedPreferences("logininfo", Activity.MODE_PRIVATE);
    }




    public void loginbutton(View v) {
        new logintask().execute();


        //libstreaming 예제로 가는 인텐트
        /*
        Toast.makeText(getApplicationContext(),"DB(Postgresql+Nginx) 서버 에러 발생",Toast.LENGTH_LONG).show();
        Intent intent=new Intent(getApplicationContext(),Example1Activity.class);
        startActivity(intent);
        finish();
        */
    }

    public class logintask extends AsyncTask<Void,Void,String>
    {
        String userid,userpw;
        Handler h;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            h=new Handler(Looper.getMainLooper());

            userid=idtext.getText().toString();
            userpw=pwtext.getText().toString();

        }

        @Override
        protected String doInBackground(Void... voids) {

            OkHttpClient client=new OkHttpClient();

            RequestBody body=new FormBody.Builder()
                    .add("userid",userid)
                    .add("userpw",userpw)
                    .build();

            Request request=new Request.Builder()
                    .url(LOGINURL)
                    .header("Content-Type","text/html")
                    .post(body)
                    .build();


            try {
                Response response=client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e)
            {
                Log.e("TabviewActivity","video file query exception"+e.getMessage());
            }
            return null;


        }

        @Override
        protected void onPostExecute(String result_) {

            super.onPostExecute(result_);


            final String responsestring=result_;
            final JSONObject jObject;
            final String result;



            try {

                jObject=new JSONObject(responsestring);
                result=jObject.getString("result");

                h.post(new Runnable() {
                    @Override
                    public void run() {
                        if(result.equals("success"))
                        {
                            Toast.makeText(getApplicationContext(),"로그인 성공",Toast.LENGTH_LONG).show();
                            Intent intent=new Intent(getApplicationContext(),TabviewActivityForFragment.class);
                            intent.putExtra("id",idtext.getText().toString());
                            startActivity(intent);

                            try {
                                SharedPreferences.Editor editor = shared.edit();
                                editor.putString("nickname", jObject.getString("nickname"));
                                editor.putString("userid",idtext.getText().toString());
                                editor.commit();

                            }catch(JSONException e)
                            {
                                Log.e("MainActivity.java","json Exception() 발생11111");
                            }

                            //editor.putString("nickname")

                            finish();

                        }else if(result.equals("fail"))
                        {
                            Toast.makeText(getApplicationContext(),"ID와 PW를 확인해주세요.",Toast.LENGTH_LONG).show();
                            /*
                            //임시로 넣은거 ▽ (나중에 지울것)
                            Intent intent=new Intent(getApplicationContext(),TabviewActivity.class);
                            startActivity(intent);
                            finish();
                            */
                        }else
                        {

                        }
                    }
                });


            }catch(JSONException e)
            {
                Log.e("MainAcitivity.java",responsestring+"");
                Log.e("MainActivity.java","json Exception() 발생2222"+e.getMessage());
            }


        }
    }
}
