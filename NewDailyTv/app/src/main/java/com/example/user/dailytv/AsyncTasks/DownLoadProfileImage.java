package com.example.user.dailytv.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.user.dailytv.Activities.TabviewActivity;
import com.example.user.dailytv.Module.UploadFileModule;
import com.quickblox.sample.groupchatwebrtc.App;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by user on 2017-12-21.
 */

public class DownLoadProfileImage extends AsyncTask<CircleImageView,Void,Void>
{
    private Context context;
    App.GlobalVariable global=App.getGlobal();
    SharedPreferences shared;

    Handler h;

    CircleImageView myimage=null;

    final String DOWNLOAD_PROFILE_URL="http://"+global.getIP()+"/download_profileimage.php";

    public DownLoadProfileImage(Context context)
    {

        this.context=context;
        h=new Handler();
    }


    @Override
    protected void onPreExecute() {

        shared=context.getSharedPreferences("logininfo", Activity.MODE_PRIVATE);
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(CircleImageView... imageViews) {

        myimage=imageViews[0];

        try {

            //1. 유저ID를 POST로 서버로 보낸다.
            //2. 서버에서 redis를 통해 아이디에 해당하는 이미지 주소를 전송한다.
            //3. 해당 이미지 주소의 이미지를 ImageView에 로드시킨다.


            OkHttpClient client=new OkHttpClient();

            String userid=shared.getString("userid",null);
            RequestBody body=new FormBody.Builder()
                    .add("userid",userid)
                    .build();

            Request request=new Request.Builder()
                    .url(DOWNLOAD_PROFILE_URL)
                    .header("Content-Type","text/html")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(callback);


            return null;
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }


    private Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

            final IOException e1=e;
            Log.e("DownLoadProfileImage","에러"+e.getMessage());

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

            final String responsestring=response.body().string();
            final JSONObject jObject;
            final String result;

            Log.e("DownLoadProfileImage","성공 : "+responsestring);

            final String profileImageUrl="http://"+global.getIP()+"/"+responsestring;
            h.post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(context).load(profileImageUrl).thumbnail(0.1f).into(myimage);
                }
            });

             //if(result.equals("success"))
        }
    };

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}

