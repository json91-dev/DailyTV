package com.example.user.dailytv.Module;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.user.dailytv.R;
import com.quickblox.sample.groupchatwebrtc.App;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by user on 2017-12-21.
 */

public class UploadFileModule2 extends AsyncTask<Void,Void,Void> {


    Context context;
    Bitmap bitmap;
    Handler h;
    App.GlobalVariable global=App.getGlobal();
    CircleImageView myimage;

    SharedPreferences shared;

    public UploadFileModule2(Context context, Bitmap bitmap)
    {
        this.bitmap=bitmap;
        this.context=context;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        shared=context.getSharedPreferences("logininfo", Activity.MODE_PRIVATE);
        h=new Handler();
    }

    @Override
    protected Void doInBackground(Void... Void) {

        uploadImage();
        return null;
    }





    private void uploadImage(){

        /*
        h.post(new Runnable() {
            @Override
            public void run() {
                final ProgressDialog loading=ProgressDialog.show(context,"Uploading...",
                        "Please Wait...",false,false);
            }
        });
        */

        StringRequest stringRequest=new StringRequest(Request.Method.POST,"http://"+global.getIP()+"/profileimage_upload.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String imagurlpath) {
                        //loading.dismiss();

                        h.post(new Runnable() {
                            @Override
                            public void run() {

                                Log.e("UploadFileMode2",imagurlpath);
                                //Glide.with(context).load(imagurlpath).thumbnail(0.1f).into(myimage);
                                Toast.makeText(context,"프로필 업로드 성공",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //loading.dismiss();
                        //Toast.makeText(context,error.getMessage().toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                String image = getStringImage(bitmap);





                Map<String, String> params = new Hashtable<String, String>();

                params.put("Content-Type", "application/json; charset=utf-8");
                params.put("image", image);
                params.put("userid",shared.getString("userid",null));
                params.put("systemtime", System.currentTimeMillis()+"");


                return params;
            }
        };

        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);

    }


    //비트맵을 받아서 JPEG포멧의 바이트 형식으로 아웃풋 스트림에 담아서 imageBytes에 저장하고 이 2진 바이트값을 base64인코딩을 해서 String으로 출력하는 함수
    public static String getStringImage(Bitmap bmp)
    {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG,100,baos);

        byte[] imageBytes=baos.toByteArray();
        String encodedImage= Base64.encodeToString(imageBytes,Base64.DEFAULT);
        return encodedImage;
    }
}
