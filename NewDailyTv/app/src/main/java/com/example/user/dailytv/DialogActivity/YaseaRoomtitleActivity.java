package com.example.user.dailytv.DialogActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.dailytv.MyDatabase.GlobalVariable;
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

public class YaseaRoomtitleActivity extends Activity {


    SharedPreferences shared;
    EditText title;

    Handler h=null;

    final App.GlobalVariable global=App.getGlobal();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //이부분 setContentview 이전에 선언해야함
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.yasearoomtitleactivity);

        shared=getSharedPreferences("logininfo", Activity.MODE_PRIVATE);
        h=new Handler();


        title=(EditText) findViewById(R.id.roomtitle);

        Button button=(Button)findViewById(R.id.okbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new tvlistupdatetask().execute();
            }
        });
    }


    //컨펌 완료
    public class tvlistupdatetask extends AsyncTask  <Void,Void,Void>{


        String result="";

        private Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                result=e.getMessage();
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"서버 에러발생"+result,Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                result=response.body().string();

                if(result.equals("1")||result.equals("0"))
                {
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"닉네임 변경 성공"+result,Toast.LENGTH_LONG).show();


                            Intent intent=new Intent();
                            intent.putExtra("roomtitle",title.getText().toString());
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    });
                }else
                {
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"닉네임 변경 실패"+result,Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        };


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }



        @Override
        protected Void doInBackground(Void... voids) {

            String userid, roomtitle;

            userid=shared.getString("userid","user1");

            //방송 제목 설정하는 부분
            if(title.getText().toString().equals("")||title.getText().toString().equals(" ")||title.getText().toString().equals("  "))
            {
                roomtitle=shared.getString("nickname",null)+"님이 스트리밍중...";
            }
            else
            {
                roomtitle=title.getText().toString();
            }


            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("userid",userid)
                    .add("roomtitle",roomtitle)
                    .build();


            Request request=new Request.Builder()
                    .url("http://"+global.getIP()+"/tvroomtitlechange.php")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(callback);

            return null;
        }


        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
        }
    }
}
