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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.dailytv.Activities.YaseaStreamActivity;
import com.example.user.dailytv.Module.GpsInfo;
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

public class TvPopupActivity extends Activity {

    String TVLISTURL="";
    SharedPreferences shared;

    ArrayAdapter adapter;
    Spinner spinner;

    TextView title,password;

    final App.GlobalVariable global=App.getGlobal();

    GpsInfo gpsInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //이부분 setContentview 이전에 선언해야함
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tvpopupactivity);

        shared=getSharedPreferences("logininfo", Activity.MODE_PRIVATE);

        //스피너 설정
        spinner= (Spinner) findViewById(R.id.viewerspinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.viewernumber, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);



        Button button = (Button) findViewById(R.id.tvstartbtn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Intent intent=new Intent(TvPopupActivity.this,YaseaStreamActivity.class);
                //startActivity(intent);

                new tvlistupdatetask().execute();

            }
        });

        //tv목록 업데이트 url 설정

        TVLISTURL="http://"+global.getIP()+"/tvlist.php";
        //이 유알엘은


        ///타이틀과 패스워드 설정
        title=(TextView)findViewById(R.id.title);
        password=(TextView)findViewById(R.id.password);

        gpsInfo=new GpsInfo(getApplicationContext());

    }


    //컨펌 완료
    public class tvlistupdatetask extends AsyncTask  <Void,Void,String>{

        //String password -> 만약에 비밀방 설정할떄 .. 이부분은 나중에 추가
        String bjnickname, userid, roomtitle,viewernumberlimit;
        Handler h=null; // 콜백 함수를 위한 핸들러 설정
        String result="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //핸들러 설정
            h=new Handler(Looper.getMainLooper());

        }



        @Override
        protected String doInBackground(Void... voids) {


            OkHttpClient client=new OkHttpClient();

            String roomtitle="";


            //방송 제목 설정하는 부분
            if(title.getText().toString().equals("")||title.getText().toString().equals(" ")||title.getText().toString().equals("  "))
            {
                roomtitle=shared.getString("nickname",null)+"님이 스트리밍중...";
            }
            else
            {
                roomtitle=title.getText().toString();
            }

            Log.e("tvpopupacitivy","roomtitle : "+roomtitle);


            //방송 패스워드 설정하는 부분 =====> 만약 입력하지 않으면 0이 입력된다.

            String roompassword="";

            if(password.getText().toString().equals("")||password.getText().toString().equals(" ")||password.getText().toString().equals("  "))
            {
                roompassword="0";
            }
            else
            {
                roompassword=password.getText().toString();
            }


            Log.e("tvpopupacitivy","roompassword : "+roompassword);


            String bjnickname=shared.getString("nickname",null);
            String viewernumberlimit=spinner.getSelectedItem().toString();


            Log.e("선택된 스피너 아이템",viewernumberlimit);

            String userid=shared.getString("userid",null);

            //방제목, 패스워드,닉네임 , 시청인원 제한, 현재 시청인원, 이미지 url

            String imagename=shared.getString("userid",null);

            RequestBody body=new FormBody.Builder()
                    .add("userid",userid)
                    .add("roomtitle",roomtitle)
                    .add("roompassword",roompassword)
                    .add("bjnickname",bjnickname)
                    .add("viewernumberlimit",viewernumberlimit)
                    .add("imagename",imagename)
                    .add("lat",gpsInfo.getLatitude()+"")
                    .add("lng",gpsInfo.getLongitude()+"")
                    .build();

            Request request=new Request.Builder()
                    .url(TVLISTURL)
                    .post(body)
                    .build();


            try {

                Response response=client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e)
            {
                Log.e("VideoPreviewActivity","비디오 좋아요 업데이트 query오류"+e.getMessage());
            }

            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result.equals("1"))
            {
                Log.e("[tvlistupdate] 결과값 출력", result);

                Toast.makeText(getApplicationContext(), "방송이 시작되었습니다.", Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "[Redis DB] : 데이터 입력 성공 ", Toast.LENGTH_LONG).show();


                SharedPreferences.Editor editor=shared.edit();
                editor.putString("bjnickname",shared.getString("nickname",""));

                Intent intent=new Intent(TvPopupActivity.this,YaseaStreamActivity.class);
                startActivity(intent);

            }
            else if(result.equals("0"))
            {
                Toast.makeText(getApplicationContext(), "현재 방송중입니다.", Toast.LENGTH_LONG).show();

            }

            else{
                Log.e("TvPopupactivity [입력에러]",result+"흠..");
                Toast.makeText(getApplicationContext(),"입력 에러 발생", Toast.LENGTH_LONG).show();
            }
        }
    }
}
