package com.example.user.dailytv.Activities;

import android.app.Activity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.user.dailytv.Adapter.GridAdapter;
import com.example.user.dailytv.Adapter.Tv_adapter;
import com.example.user.dailytv.AsyncTasks.DownLoadProfileImage;
import com.example.user.dailytv.AsyncTasks.UploadProfileImage;
import com.example.user.dailytv.DialogActivity.TvPopupActivity;
import com.example.user.dailytv.ListData.TV_ListData;
import com.example.user.dailytv.ListData.Video_ListData;
import com.example.user.dailytv.Module.ExpandableHeightGridView;
import com.example.user.dailytv.Module.URIHelper;
import com.example.user.dailytv.Module.UploadFileModule;
import com.example.user.dailytv.Module.UploadFileModule2;
import com.example.user.dailytv.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.quickblox.sample.groupchatwebrtc.App;
import com.quickblox.sample.groupchatwebrtc.activities.SplashActivity;
import com.synnapps.carouselview.CarouselView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by user on 2017-11-01.
 */

public class TabviewActivity extends AppCompatActivity implements Button.OnClickListener ,TabHost.OnTabChangeListener {


    private Tv_adapter tv_adapter=null;
    ListView tv_listview;

    //탭호스트 변수

    TabHost tabHost1;

    //리스트뷰 내에서 이미지 처리를 위한 핸들러
    Handler handler;

    //redis item 드랍하는 url
    String TVLISTDROPURL="";
    String TVLISTCHECKURL="";

    //DB와 전역변수 설정
    SharedPreferences shared;






    //캐러샐 뷰
    CarouselView carouselView;
    int[] sampleImages = {R.drawable.image_11, R.drawable.image_2, R.drawable.image_3, R.drawable.image_4};
    Bitmap[] sampleimagebitmap=new Bitmap[4];


    //프로필 사진 부분

    CircleImageView circleimageview;

    private static final int PICK_FROM_CAMERA=0;
    private static final int PICK_FROM_ALBUM=1;
    private static final int CROP_FROM_CAMERA=2;


    boolean isresumecalledforlivevediolist=true;

    //////////////////그리드뷰 설정부분/////////////////////


    GridView video_gridview;
    GridAdapter video_gridadapter;



    //앨범으로 사진설정부분 Uri
    Uri myuri;



    //글로벌 IP함수
    final App.GlobalVariable global=App.getGlobal();

    //pull to refresh를 위한 변수

    SwipeRefreshLayout swipe_tvlistview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        //이부분은 나중에 살펴봐야 한다.
        //수동으로 액션바의 색상을 변경하는 코드이다.
        //현재 레이아웃의 테마변경이 되지 않는데 그로 인해서 이 부분을 집어 넣었다..
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        setContentView(R.layout.tabviewactivity3);
        handler=new Handler();
        TVLISTDROPURL="http://"+global.getIP()+"/tvlistdrop.php";
        TVLISTCHECKURL="http://"+global.getIP()+"/tvlistcheck.php";

        // 데이터 베이스 설정
        initShared();

        // 버튼 탭호스트 등의 UI설정
        initUi();

        // FloatingButton애니메이션 설정
        initAnimation();



 //////////////////////캐러셀 부분//////////////////////////////////////////////////////

        /*
        carouselView = (CarouselView) findViewById(R.id.carouselView);
        carouselView.setPageCount(sampleImages.length);

        carouselView.setImageListener(new ImageListener() {
            @Override
            public void setImageForPosition(int position, ImageView imageView) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;

                for(int i=0;i<sampleImages.length;i++)
                {
                    sampleimagebitmap[i]=BitmapFactory.decodeResource(getResources(),sampleImages[i],options);
                }

                imageView.setImageBitmap(sampleimagebitmap[position]);
            }
        });

        carouselView.setImageClickListener(new ImageClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(getApplicationContext(), "Clicked item: "+ position, Toast.LENGTH_SHORT).show();
            }
        });

        */


        //프로필사진을 눌렀을떄의 다이얼로그 창의 리스너 및 이벤트 설정

       initProfilePictureDialog();

       //프로필 사진 (CircleImageView에 사진값 초기화)

       initProfilePicture();


       initListViewEvent();





    }

    public void initListViewEvent()
    {

        //////////////리스트뷰 처리하는 부분/////////////////////////

        swipe_tvlistview=(SwipeRefreshLayout)findViewById(R.id.swipe_tvlistview);
        swipe_tvlistview.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new tvlistchecktask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });


        tv_listview =(ListView)findViewById(R.id.tvlistview);
        tv_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SharedPreferences.Editor editor=shared.edit();
                editor.remove("bjnickname");
                editor.putString("bjnickname","펭귄");
                editor.commit();

                //exoplayer로 이동하는 인텐트
                Intent intent=new Intent(getApplicationContext(),ExoPlayer2Activity.class);
                //startActivityForResult(intent,1001);

                Log.e("리스트뷰의 포지션값",position+"");

                TV_ListData item=(TV_ListData)tv_adapter.getItem(position);


                //Toast.makeText(getApplicationContext(),item.publisherid,Toast.LENGTH_LONG).show();


                intent.putExtra("publisherid",item.publisherid);
                startActivity(intent);


            }
        });

        //어뎁터 초기화

        tv_adapter=new Tv_adapter(getApplicationContext());



        /////////////////////비디오 그리드뷰 처리하는 부분/////////////////////////////////


        video_gridview=(GridView)findViewById(R.id.videogridview);
        //아이템이 클릭되면 url을 가지도 VideoPreviewActivity로 넘어간다.
        video_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent=new Intent(getApplicationContext(),VideoPreviewActivity.class);
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
        video_gridadapter=new GridAdapter(getApplicationContext());
        //video_gridview.setExpanded(true);

        video_gridview.setAdapter(video_gridadapter);

    }


    //1. SharedPreperences 설정

    //함수 설명
    //1. 맨 처음 시작될때 userid를 userid로 설정하고 bjnickname을 "펭귄"으로 설정한다.
    //2. 현재 댓글 입력시에는 DB에서 자동으로 select문을 통해 nickname을 설정하므로 현재 로컬과 동기화가 되어있지 않다.
    //3. 하지만 userid는 DB쿼리 조회시에 필요하므로 주의해서 설정하도록 해야하며 추후 테스트가 끝나면 MainActivity에서 넘어온 userid로 설정해야 한다.
    public void initShared()
    {


        shared=getSharedPreferences("logininfo", Activity.MODE_PRIVATE);
        Toast.makeText(getApplicationContext(),shared.getString("nickname","")+"",Toast.LENGTH_LONG).show();
        SharedPreferences.Editor editor=shared.edit();
        editor.putString("userid","user1");
        editor.putString("nickname","펭귄");
        editor.commit();
    }

    public void initUi()
    {

        ImageButton nicbutton=(ImageButton)findViewById(R.id.nicbutton);
        nicbutton.setOnClickListener(this);
        //Button tvbutton=(Button)findViewById(R.id.tvbutton);
        //tvbutton.setOnClickListener(this);
        Button tvstopbutton=(Button)findViewById(R.id.tvstopbutton);
        tvstopbutton.setOnClickListener(this);




        tabHost1=(TabHost)findViewById(R.id.tabHost1);
        tabHost1.setup();
        tabHost1.setOnTabChangedListener(this);

        //첫번째 tab 추가


        TabHost.TabSpec ts1=tabHost1.newTabSpec("Tab Spec 1");
        ts1.setContent(R.id.content1);
        ts1.setIndicator("Home");
        tabHost1.addTab(ts1) ;


        // 두 번째 Tab. (탭 표시 텍스트:"TAB 2"), (페이지 뷰:"content2")

        TabHost.TabSpec ts2 = tabHost1.newTabSpec("Tab Spec 2") ;
        ts2.setContent(R.id.content2) ;
        ts2.setIndicator("Live") ;
        tabHost1.addTab(ts2) ;

        // 세 번째 Tab. (탭 표시 텍스트:"TAB 3"), (페이지 뷰:"content3")
        TabHost.TabSpec ts3 = tabHost1.newTabSpec("Tab Spec 3") ;
        ts3.setContent(R.id.content3) ;
        ts3.setIndicator("Vedioss") ;
        tabHost1.addTab(ts3) ;


        tabHost1.getTabWidget().getChildAt(tabHost1.getCurrentTab()).getBackground().setColorFilter(Color.parseColor("#FF4081"), PorterDuff.Mode.MULTIPLY);

    }


    public void initAnimation()
    {
        //애니매이션 설정부분

        final FloatingActionButton floatingbutton=(FloatingActionButton)findViewById(R.id.floatingbutton);
        final FloatingActionButton stream_float=(FloatingActionButton)findViewById(R.id.floating2);
        final FloatingActionButton webrtc_float=(FloatingActionButton)findViewById(R.id.floating3);



        final LinearLayout stream_layout=(LinearLayout)findViewById(R.id.streamLayout);
        final LinearLayout rtc_layout=(LinearLayout) findViewById(R.id.rtcLayout);

        stream_layout.setVisibility(View.GONE);
        rtc_layout.setVisibility(View.GONE);

        final Animation mShowButton= AnimationUtils.loadAnimation(TabviewActivity.this,R.anim.show_button);
        final Animation mHideButton= AnimationUtils.loadAnimation(TabviewActivity.this,R.anim.hide_button);
        final Animation mShowLayout= AnimationUtils.loadAnimation(TabviewActivity.this,R.anim.show_layout);
        final Animation mHideLayout= AnimationUtils.loadAnimation(TabviewActivity.this,R.anim.hide_layout);

        floatingbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //방송설정 화면으로 넘어간다.
                /*
                Intent intent=new Intent(getApplicationContext(),TvPopupActivity.class);
                startActivity(intent);
                */

                if(stream_layout.getVisibility()==View.VISIBLE&&rtc_layout.getVisibility()==View.VISIBLE)
                {
                    stream_layout.setVisibility(View.GONE);
                    rtc_layout.setVisibility(View.GONE);


                    floatingbutton.startAnimation(mHideButton);

                    stream_layout.startAnimation(mHideLayout);
                    rtc_layout.startAnimation(mHideLayout);


                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    stream_float.setVisibility(View.GONE);
                                    webrtc_float.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
                    t.run();

                }
                else
                {
                    stream_float.setVisibility(View.VISIBLE);
                    webrtc_float.setVisibility(View.VISIBLE);

                    stream_layout.setVisibility(View.VISIBLE);
                    rtc_layout.setVisibility(View.VISIBLE);
                    floatingbutton.startAnimation(mShowButton);

                    stream_layout.startAnimation(mShowLayout);
                    rtc_layout.startAnimation(mShowLayout);
                }

            }
        });

        stream_float.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),TvPopupActivity.class);
                startActivity(intent);
            }
        });

        webrtc_float.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getApplicationContext(),SplashActivity.class);
                startActivity(i);
            }
        });
    }

    public void initProfilePictureDialog()
    {
        circleimageview=(CircleImageView)findViewById(R.id.circleimageview);

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

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        circleimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                builder
                        .setTitle("업로드할 이미지 선택")
                        .setPositiveButton("사진촬영",cameraListener)
                        .setNeutralButton("앨범선택",albumListener)
                        .setNegativeButton("취소",cancelListener)
                        .show();
            }
        });
    }

    //onCreate에서 circleimageview의 값을 초기화 하는 부분이다.
    public void initProfilePicture()
    {


        //서버에서 이미지 url을 받아와서 Circle이미지뷰에 출력하는 백그라운드 작업을 실행한다.
        new DownLoadProfileImage(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,circleimageview);


        /*
        final String pngfilepath=getApplication().getCacheDir()+File.separator+shared.getString("nickname",null)+".png";
        final File pngfile=new File(pngfilepath);

        Log.e("pngfilepath",pngfilepath);
        if(pngfile.exists())
        {

            Log.e("pngfilepath","캐시파일이 존재함");
            //URI 경로로 bitmap값 가져옴
            picturebitmap=BitmapFactory.decodeFile(pngfilepath);
            circleimageview.setImageBitmap(picturebitmap);

        }

        else
        {
            Log.e("pngfilepath","캐시파일이 존재하지 않음");
        }
        */


    }




    //버튼 이벤트 처리 부분
    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.nicbutton)
        {

        }


        /*
        else if(view.getId()==R.id.tvbutton)
        {
            //방송설정 화면으로 넘어간다.
            Intent intent=new Intent(this,TvPopupActivity.class);
            startActivity(intent);
        }
        */


        else if(view.getId()==R.id.tvstopbutton)
        {
            //방송을 종료하고 redis로 업데이트 한다.
            new tvlistdroptask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    //탭 바뀌는 이벤트

    public void onTabChanged(String tabid)
    {

        tabHost1.getTabWidget().getChildAt(tabHost1.getCurrentTab()).getBackground().setColorFilter(Color.parseColor("#FF4081"), PorterDuff.Mode.MULTIPLY);

        String message;
        message="onTabChanged : "+tabid;
        String lasttabid=tabid.substring(tabid.length()-1);

        //이부분에서 onCreate 되서 url 선언되기 전에 실행되서 오류
        //두번째 탭일때 실행


        if(lasttabid.equals("2")) {
            if (isresumecalledforlivevediolist) {
                new tvlistchecktask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                isresumecalledforlivevediolist=false;
            }
        }else if(lasttabid.equals("1"))
        {
            Log.e("videolistcheck","1111111111111111111111111111");
            new videolistchecktask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isresumecalledforlivevediolist=true;

    }


    //////////////이부분부터 리스트뷰 관련된 코드 ///////////////////






    private static class imagetaskinlistviewinput
    {
        public String urlstring;
        public ImageView imageview;


        imagetaskinlistviewinput(String urlstring,ImageView imageview)
        {
                this.urlstring=urlstring;
                this.imageview=imageview;
        }
    }


    private class imagetaskinlistview extends AsyncTask<imagetaskinlistviewinput,Void,Void>
    {
        @Override
        protected Void doInBackground(imagetaskinlistviewinput... imagetaskinlistviewinputs) {

            final String urlstring=imagetaskinlistviewinputs[0].urlstring;
            final ImageView imageview=imagetaskinlistviewinputs[0].imageview;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //Glide.with(getApplicationContext()).load(urlstring).thumbnail(0.3f).into(imageview);
                    Glide.with(getApplicationContext()).load(urlstring).thumbnail(0.1f).into(imageview);

                }
            });
            return null;
        }
    }


    public class tvlistdroptask extends AsyncTask <Void,Void,String>{

        //String password -> 만약에 비밀방 설정할떄 .. 이부분은 나중에 추가

        Handler h;

        String userid="user1"; //userid는 user1으로 통일

        String result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            h=new Handler(Looper.getMainLooper());

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


            Log.e("[tvlistdrop]","결과값"+result);

            if(result.equals("1"))
            {
                Toast.makeText(getApplicationContext(),"방송이 종료되었습니다. ",Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(),"[Redis DB] : row 삭제 성공 ",Toast.LENGTH_LONG).show();
            }
            else if(result.equals("0"))
            {
                Toast.makeText(getApplicationContext(),"방송이 종료되었습니다. ",Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(),"[Redis DB] : row 삭제 시도(이미 삭제됨)"+result,Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
            }
         }
    }

    ////////////////////////////////레디스 tvlistcheck하는 Asynctask //



    // redis delete하는 Asynctask
    // 디비 업데이트 하는 부분이네 .. 아니지 삭제하는 부분이지...
    // 드랍을 해야한다.

    //컨펌 완료
    public class tvlistchecktask extends AsyncTask  <Void,Void,String>{

        //String password -> 만약에 비밀방 설정할떄 .. 이부분은 나중에 추가

        String userid="user1"; //userid는 user1으로 통일
        String result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //핸들러 설정
        }
        @Override
        protected String doInBackground(Void... voids) {


            OkHttpClient client=new OkHttpClient();

            RequestBody body=new FormBody.Builder()
                    .add("userid",userid)
                    .build();

            Request request=new Request.Builder()
                    .url(TVLISTCHECKURL)
                    .post(body)
                    .build();


            try {
                Response response=client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e)
            {
                Log.e("[tvlistchecktask 에러발생]",result+"");

            }


            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            //result=response.body().string();

            //만약 조회했는데 방이 없다면
            if(result.equals("0"))
            {
                tv_adapter.remove(0);
                tv_listview.setAdapter(tv_adapter);
                //Toast.makeText(getApplicationContext(),"[Redis DB] : 방 목록 조회 실패",Toast.LENGTH_LONG).show();


            }else {//if (tv_adapter.getCount()==0) { //방이 존재한다면
                //Toast.makeText(getApplicationContext(),"[Redis DB] : 방 목록 조회 성공 ",Toast.LENGTH_LONG).show();
                //방목록 조회성공시 리스트뷰에 값입력

                try {
                    final JSONObject jsonobj = new JSONObject(result);
                    final JSONArray tvlistarray = jsonobj.getJSONArray("result");

                    //리스트 모두 삭제
                    tv_adapter.removeAll();

                    for(int i=0;i<tvlistarray.length();i++)
                    {
                        final String roomtitle,roompassword,bjnickname,viewernumberlimit,viewernumber,imageurl,publisherid;
                        final JSONObject tvlistjson=tvlistarray.getJSONObject(i);
                        roomtitle=tvlistjson.getString("roomtitle");
                        roompassword=tvlistjson.getString("roompassword");
                        bjnickname=tvlistjson.getString("bjnickname");
                        viewernumberlimit=tvlistjson.getString("viewernumberlimit");
                        viewernumber=tvlistjson.getString("viewernumber");
                        imageurl=tvlistjson.getString("imageurl");

                        publisherid=tvlistjson.getString("publisherid");

                        imageurl.replaceAll("\\\\","");
                        Log.e("tabviewactivity","이미지 url "+"http://"+global.getIP()+imageurl);

                        tv_adapter.addItem(roomtitle,roompassword,bjnickname,viewernumberlimit,viewernumber,"http://"+global.getIP()+imageurl,publisherid);

                    }

                    tv_listview.setAdapter(tv_adapter);



                }catch (JSONException e)
                {
                    Log.e("[Tabviewactivity]JSON에러",e.getMessage());
                }

                swipe_tvlistview.setRefreshing(false);

           }
        }
    }


    //////////////////////////////////////프로필 사진 변경하는 함수 부분///////////////////////////////////////////////


    //사진찍기 부분
    private void doTakePhotoAction()
    {
        /*
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String url="tmp_"+String.valueOf(System.currentTimeMillis());
        mImageCaptureUri=Uri.fromFile(new File(Environment.getExternalStorageDirectory(),url));
        intent.putExtra(MediaStore.EXTRA_OUTPUT,mImageCaptureUri);

        startActivityForResult(intent,PICK_FROM_CAMERA);
        */
        //opencv 테스트
        Intent i=new Intent(getApplicationContext(),OpencvActivity.class);
        startActivityForResult(i,1001);
    }

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

                    new UploadFileModule2(getApplicationContext(),photo).execute();

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
            case PICK_FROM_ALBUM:
            {
                //Uri로부터 bitmap 이미지를 가져오는 부분 임시파일의 uri를 가져와서 bitmap으로 바꿔준다.
                /*
                try {
                    //사진앨범에서 data.getData형식으로 파
                    picturebitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                }catch(Exception e)
                {
                    e.printStackTrace();

                }
                circleimageview.setImageBitmap(picturebitmap);
                */
                //////////////////원래코드//////////////
                myuri=data.getData();

                Log.e("앨범 myuri",myuri.getPath());
                // /external/images/media/12015

            }



            case PICK_FROM_CAMERA:
            {

                /*
                bm=new BitmapUtil().loadBitmapRotated(mImageCaptureUri.getPath(),4);

                //Toast.makeText(getActivity(),bm+"",Toast.LENGTH_LONG).show();
                try {
                    ByteArrayOutputStream stream=new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG,80,stream);
                    byte[] bytearray=stream.toByteArray();

                    FileOutputStream fos=new FileOutputStream(mImageCaptureUri.getPath());
                    fos.write(bytearray);

                }catch(Exception e)
                {
                    e.printStackTrace();
                }

                */

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

                //Toast.makeText(getApplicationContext(),data.getExtras().getString("pngfilepath")+"",Toast.LENGTH_LONG).show();
                String pngfilepath=data.getExtras().getString("pngfilepath");

                File inputpngfile=new File(pngfilepath);

                /*
                File originpngfile=new File(getCacheDir().getAbsolutePath()+File.separator+shared.getString("nickname",null)+".png");

                if(originpngfile.exists())
                {
                    Log.e("Tabviewactivity","파일존재 새로운 파일로 대체합니다.");
                    if(originpngfile.delete())
                    {
                        Log.e("tabviewAcitvity","파일삭제 성공");
                        if(inputpngfile.renameTo(originpngfile))
                        {
                            Log.e("tabviewactivity","새로운 파일로 대체성공");
                            pngfilepath=originpngfile.getAbsolutePath();
                        }
                    }
                }

                */




                //picturebitmap=BitmapFactory.decodeFile(pngfilepath);
                //circleimageview.setImageBitmap(picturebitmap);


                //myuri=Uri.fromFile(inputpngfile);
                //String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                //File myurifile=new File(Environment.getExternalStorageDirectory(), url);
                //fileCopy(pngfilepath,myurifile.getPath());

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

    //파일을 복사하는 메소드
    public static void fileCopy(String inFileName, String outFileName) {
        try {
            FileInputStream fis = new FileInputStream(inFileName);
            FileOutputStream fos = new FileOutputStream(outFileName);

            int data = 0;
            while((data=fis.read())!=-1) {
                fos.write(data);
            }
            fis.close();
            fos.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    ////////////////////////////////비디오 그리드뷰 설정부분///////////////////////////////////






    ////////////////////////////////////////////비디오 목록 가져와서 뿌려주는 비동기 처리//////////////////////////////////////////////////



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
                tv_adapter.remove(0);
                tv_listview.setAdapter(tv_adapter);
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

            //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();

        }
    }



}


