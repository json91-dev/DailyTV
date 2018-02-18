package com.example.user.dailytv.Fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.dailytv.Activities.OpencvActivity;
import com.example.user.dailytv.Activities.VideoPreviewActivity;
import com.example.user.dailytv.Adapter.GridAdapter;
import com.example.user.dailytv.AsyncTasks.DownLoadProfileImage;
import com.example.user.dailytv.DialogActivity.TvPopupActivity;
import com.example.user.dailytv.ListData.Video_ListData;
import com.example.user.dailytv.Module.UploadFileModule2;
import com.example.user.dailytv.R;
import com.quickblox.sample.groupchatwebrtc.App;
import com.quickblox.sample.groupchatwebrtc.activities.SplashActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

/**
 * Created by user on 2018-01-05.
 */

public class Fragment_home extends Fragment {


    View view;

    //글로벌 IP함수
    final App.GlobalVariable global=App.getGlobal();


    CircleImageView circleimageview;

    private static final int PICK_FROM_CAMERA=0;
    private static final int PICK_FROM_ALBUM=1;
    private static final int CROP_FROM_CAMERA=2;

    //그리드뷰 설정부분
    GridView video_gridview;
    GridAdapter video_gridadapter;

    //앨범에서 사진을 가져왔을때 그 경로를 저장하는 Uri이다.
    Uri myuri;

    //리사이클러뷰를 설정한다.
    SwipeRefreshLayout videoSwipeLayout;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.f_home,null);

        initProfilePictureDialog();

        initProfilePicture();

        initVideoGridView();

        initBraodcastBtn();

        videoSwipeLayout=view.findViewById(R.id.swipe_videogridview);
        videoSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new videolistchecktask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        return view;
    }

    private  void initBraodcastBtn()
    {
        Button tvStartBtn=(Button)view.findViewById(R.id.tvstartbutton);
        tvStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),TvPopupActivity.class);
                startActivity(intent);
            }
        });

        Button tvStopBtn=(Button)view.findViewById(R.id.tvstopbutton);
        tvStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new tvlistdroptask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        //웹 RTC 버튼을 추가한다.
        Button webrtcButton=(Button)view.findViewById(R.id.webrtcButton);
        webrtcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(),SplashActivity.class);
                startActivity(i);
            }
        });
    }


    public void initProfilePictureDialog()
    {
        circleimageview=(CircleImageView)view.findViewById(R.id.circleimageview);

        final DialogInterface.OnClickListener cameraListener=new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doTakePhotoAction();
            }
        };
        final DialogInterface.OnClickListener albumListener=new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doTakeAlbumAction();
            }
        };
        final DialogInterface.OnClickListener cancelListener=new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        circleimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                builder.setTitle("업로드할 이미지 선택")
                        .setPositiveButton("사진촬영",cameraListener)
                        .setNeutralButton("앨범선택",albumListener)
                        .setNegativeButton("취소",cancelListener)
                        .show();
            }
        });
    }



    //사진을 찍고 처리를 하는 함수이다.
    //사진찍기 버튼을 누르면 OpenCv 화면으로 넘어간다.
    private void doTakePhotoAction()
    {

        Intent i=new Intent(getActivity(),OpencvActivity.class);
        startActivityForResult(i,1001);
    }

    //앨범에서 가져오기 버튼을 누르면
    //앨범목록이 켜질 수 있도록 intent의 값을 설정한뒤에 앨범화면으로 넘어가게된다.
    //이후에 PICK_FROM_ALBUM으로 앨범에서 선택한 값에 대한 Uri를 intent로 받게 된다.
    private void doTakeAlbumAction()
    {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,PICK_FROM_ALBUM);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK)
        {
            return;
        }

        switch(requestCode)
        {
            case CROP_FROM_CAMERA:
            {
                // 크롭이 된 이후의 이미지를 넘겨 받습니다.
                // 이미지뷰에 이미지를 보여준다거나 부가적인 작업 이후에
                // 임시 파일을 삭제합니다.
                final Bundle extras = data.getExtras();

                if(extras != null)
                {

                    //앨범에서 크롭된 이미지를 가져올때 Intent의 크기 때문에 파일의 사이즈의 제한이 있다
                    //따라서 실제 이미지는 조금 깨지거나 화질이 저하되는 현상이 있는데
                    //이는 나중에 해결할 수 있으므로 일단 진행하도록 한다.


                    Bitmap photo = extras.getParcelable("data");


                    //uploadVideo함수는 파일의 경로를 입력받고
                    //서버로 업로드를 시켜주는 함수이다.
                    //안드로이드의 파일의 절대경로가 필요하다.
                    //Log.e("uri경로",data.getData().getPath()+"");

                    //String uriRealpath=URIHelper.getRealPathFromURI(getApplicationContext(),myuri);
                    //new UploadProfileImage(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,photo);

                    new UploadFileModule2(getActivity(),photo).execute();

                    circleimageview.setImageBitmap(photo);

                }

                // 임시 파일 삭제
                File f = new File(myuri.getPath());
                if(f.exists())
                {
                    f.delete();
                }
                break;
            }

                //앨범선택 이후에 이 구문을 실행한다.
                //선택한 사진의 uri를 전달받아서 myuri로 복사한다.

            case PICK_FROM_ALBUM:
            {
                //////////////////원래코드//////////////
                myuri=data.getData();
                Log.e("앨범 myuri",myuri.getPath());
            }
                //이부분은까지 앨범 선택 이후에 실행된다.
                //다이얼로그로는 이 구문으로 넘어가지 않는다.

            case PICK_FROM_CAMERA:
            {

                Intent intent=new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(myuri,"image/*");
                intent.putExtra("outputX",90);
                intent.putExtra("outputY",90);
                intent.putExtra("aspectX",1);
                intent.putExtra("aspectY",1);
                intent.putExtra("scale",true);
                intent.putExtra("return-data",true);
                //intent.putExtra("output",myuri);
                startActivityForResult(intent,CROP_FROM_CAMERA);

                break;



            }

            //opencv카메라에서 온 결과를 받는 부분
            case 1001:
            {


                String pngfilepath=data.getExtras().getString("pngfilepath");

                File inputpngfile=new File(pngfilepath);

                myuri = Uri.fromFile(inputpngfile);

                Intent intent=new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(myuri,"image/*");
                intent.putExtra("outputX",90);
                intent.putExtra("outputY",90);
                intent.putExtra("aspectX",1);
                intent.putExtra("aspectY",1);
                intent.putExtra("scale",true);
                intent.putExtra("return-data",true);
                //intent.putExtra("output",myuri);
                startActivityForResult(intent,CROP_FROM_CAMERA);

                break;



            }
        }


    }

    //서버에서 이미지 url을 받아와서 Circle이미지뷰에 출력하는 백그라운드 작업을 실행한다.
    public void initProfilePicture()
    {
        new DownLoadProfileImage(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,circleimageview);

        SharedPreferences shared=getActivity().getSharedPreferences("logininfo", Activity.MODE_PRIVATE);

        TextView nickname=(TextView)view.findViewById(R.id.nickname);
        nickname.setText(shared.getString("nickname",""));

    }


    public void initVideoGridView()
    {
        /////////////////////비디오 그리드뷰 처리하는 부분/////////////////////////////////


        video_gridview=(GridView)view.findViewById(R.id.videogridview);
        //아이템이 클릭되면 url을 가지도 VideoPreviewActivity로 넘어간다.
        video_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent=new Intent(getActivity(),VideoPreviewActivity.class);
                Video_ListData item=(Video_ListData) video_gridadapter.getItem(position);
                intent.putExtra("videourl",item.videourl);
                intent.putExtra("title",item.roomtitle);

                SimpleDateFormat dayTime = new SimpleDateFormat("yyyy년 MM월 dd일 hh:mm:ss");
                String date= dayTime.format(new Date(Long.parseLong(item.date)));
                intent.putExtra("date",date);

                intent.putExtra("longdate",item.date);

                intent.putExtra("v_userid",item.v_userid);

                Log.e("video","입력 인텐트 "+item.videourl);
                startActivity(intent);
            }
        });
        video_gridadapter=new GridAdapter(getActivity());
        //video_gridview.setExpanded(true);

        video_gridview.setAdapter(video_gridadapter);

        new videolistchecktask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public class videolistchecktask extends AsyncTask  <Void,Void,String>{

        //String password -> 만약에 비밀방 설정할떄 .. 이부분은 나중에 추가

        String userid="user1"; //userid는 user1으로 통일
        String result;




        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client=new OkHttpClient();

            RequestBody body=new FormBody.Builder()
                    .add("userid",userid)
                    .build();

            Request request=new Request.Builder()
                    .url("http://"+global.getIP()+"/videofilecheck.php")
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
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            Log.e("videolistcheck"," :결과값 출력"+ result);

            //만약 조회했는데 방이 없다면
            if(result.equals("0"))
            {
                //tv_adapter.remove(0);
                //tv_listview.setAdapter(tv_adapter);
                Log.e("videolistcheck"," :방 목록 조회 실패"+ result);

            }else {//if (tv_adapter.getCount()==0) { //방이 존재한다면

                Log.e("videolistcheck"," :방 목록 조회 성공"+ result);
                //방목록 조회성공시 리스트뷰에 값입력

                try {
                    final JSONObject jsonobj = new JSONObject(result);
                    final JSONArray videolistarray = jsonobj.getJSONArray("result");

                    //리스트 모두 삭제
                    video_gridadapter.removeAll();

                    for(int i=0;i<videolistarray.length();i++)
                    {
                        final String roomtitle,longdate;
                        String videourl,videoimageurl,v_userid,nickname,profilepath;

                        final JSONObject videolistjson=videolistarray.getJSONObject(i);

                        //새로 추가된 값들
                        v_userid=videolistjson.getString("v_userid");
                        nickname=videolistjson.getString("nickname");
                        profilepath=videolistjson.getString("profilepath");

                        ////////
                        roomtitle=videolistjson.getString("roomtitle");
                        longdate=videolistjson.getString("longdate");
                        videourl=videolistjson.getString("videourl");
                        videoimageurl=videolistjson.getString("videoimageurl");


                        //Log.e("videolistcheck","이미지 url "+"http://"+global.getIP()+"/videos/"+videourl);
                        //Log.e("videolistcheck","이미지 url "+"http://"+global.getIP()+"/videos/"+videoimageurl);

                        videourl="http://"+global.getIP()+"/videos/"+videourl;
                        videoimageurl="http://"+global.getIP()+"/videos/"+videoimageurl;
                        profilepath="http://"+global.getIP()+"/"+profilepath;


                        video_gridadapter.addItem(roomtitle,longdate,videourl,videoimageurl,nickname,profilepath,v_userid);

                    }

                    //video_gridview.setAdapter(video_gridadapter);
                    video_gridadapter.notifyDataSetChanged();



                }catch (JSONException e)
                {
                    Log.e("[Tabviewactivity]JSON에러",e.getMessage());
                }

            }

            videoSwipeLayout.setRefreshing(false);

            //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();

        }



    }


    public class tvlistdroptask extends AsyncTask <Void,Void,String>{

        //String password -> 만약에 비밀방 설정할떄 .. 이부분은 나중에 추가



        String TVLISTDROPURL="http://"+global.getIP()+"/tvlistdrop.php";
        String userid="user1"; //userid는 user1으로 통일

        String result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected String doInBackground(Void... voids) {


            OkHttpClient client=new OkHttpClient();

            RequestBody body=new FormBody.Builder()
                    .add("userid",userid)
                    .build();

            Request request=new Request.Builder()
                    .url(TVLISTDROPURL)
                    .post(body)
                    .build();

            try {

                Response response=client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e)
            {
                Log.e("VideoPreviewActivity","댓글 조회 query오류"+e.getMessage());
            }


            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            result=result.trim();
            Log.e("[tvlistdrop]","결과값"+result);

            if(result.equals("1"))
            {
                Toast.makeText(getActivity(),"[Redis DB] : row 삭제 성공 ",Toast.LENGTH_LONG).show();
            }
            else if(result.equals("0"))
            {
                Toast.makeText(getActivity(),"[Redis DB] : row 삭제 시도(이미 삭제됨)"+result,Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getActivity(),result,Toast.LENGTH_LONG).show();
            }
        }
    }


}
