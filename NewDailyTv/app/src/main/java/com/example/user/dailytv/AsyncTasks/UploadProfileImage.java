package com.example.user.dailytv.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.example.user.dailytv.Module.UploadFileModule;
import com.quickblox.sample.groupchatwebrtc.App;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by user on 2017-12-21.
 */

//이 함수는 비동기로 TabviewActivity에서 프로필 사진을 변경했을때 서버로 업로드 해주는 함수이다.

public class UploadProfileImage extends AsyncTask<Bitmap,Void,Void>
{
    private Context context;
    App.GlobalVariable global=App.getGlobal();
    SharedPreferences shared;

    public UploadProfileImage(Context context)
    {
        this.context=context;
    }


    @Override
    protected void onPreExecute() {

        shared=context.getSharedPreferences("logininfo", Activity.MODE_PRIVATE);
        super.onPreExecute();
    }

    Bitmap croped_bitmap;
    @Override
    protected Void doInBackground(Bitmap... bitmap) {
        try {

            //1. crop된 이미지를 얻어온다.
            //2. crop된 이미지의 bitmap값을 얻어온다.
            //3. bitmap값을 png로 변환시킨다.
            //4. png값을 서버로 업로드한다.

            //MediaStore.Images.Media.getBitmap(getContentResolver(), uris[0]);
            croped_bitmap = bitmap[0];


            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            croped_bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
            byte[] bytearray=stream.toByteArray();
            final String tempImagePath=context.getCacheDir()+ File.separator+"temp.png";

            FileOutputStream fos=new FileOutputStream(tempImagePath);
            fos.write(bytearray);
            UploadFileModule.uploadFile(tempImagePath,"http://"+global.getIP()+"/profileimage_upload.php",shared.getString("userid",null),System.currentTimeMillis()+"");

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
