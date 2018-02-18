package com.example.user.dailytv.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.dailytv.Adapter.ChattingCursorAdapterForYasea;
import com.example.user.dailytv.DialogActivity.YaseaMoonCheckActivity;
import com.example.user.dailytv.DialogActivity.YaseaRoomtitleActivity;
import com.example.user.dailytv.Module.GpsInfo;
import com.example.user.dailytv.MyDatabase.DbOpenHelper;
import com.example.user.dailytv.R;
import com.github.faucamp.simplertmp.RtmpHandler;
import com.github.glomadrian.grav.GravView;
import com.quickblox.sample.groupchatwebrtc.App;
import com.seu.magicfilter.utils.MagicFilterType;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by user on 2017-11-09.
 */

public class YaseaStreamActivity extends AppCompatActivity implements RtmpHandler.RtmpListener,
        SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener,Button.OnClickListener {


    Context context;

    private static final String TAG = "Yasea";

    private ImageButton btnPublish;
    private ImageButton btnPublish2;


    private ImageButton btnSwitchCamera;
    private Button btnRecord;
    private Button btnSwitchEncoder;

    private SharedPreferences sp;
    //private String rtmpUrl = "rtmp://ossrs.net/" + getRandomAlphaString(3) + '/' + getRandomAlphaDigitString(5);
    private String recPath = Environment.getExternalStorageDirectory().getPath() + "/test.mp4";

    private String rtmpurl;

    private SrsPublisher mPublisher;









    //////////////// 채팅 부분///////////////////////

    ImageButton chatbutton;
    LinearLayout chat_layout;

    ListView chatlistview;

    SharedPreferences shared;
    SQLiteDatabase sqlite;

    ChattingCursorAdapterForYasea chattingadapter;

    EditText textvalue;
    Button inputbutton;
    Handler handler;

    final App.GlobalVariable global=App.getGlobal();
    //////////////////////////////////////////////////////
    ///////////////네티부분////////////////

    ChatClient chatclient;
    ////////////////////////////////////////



    //Timer timer;
    //TimerTask timertask;


    //닉네임 받아오는 부분

    TextView roomtitle;


    /////////////////////////////동영상 녹화하는 부분/////////////////////////////

    ImageButton recordbutton;
    String currenttimestring="";

    private ProgressDialog pDialog;
    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;


    //좌표를 서버에 업로드를 하기위해 필요한 모듈이다.

    GpsInfo gpsInfo;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.yaseastreamactivity);


        InitDB();

        InitChatUi();

        InitAnimationUi();

        InitStarRecommendCheck();

        InitChatListView();

        InitChatButtonEvent();

        startNettyThread();

        InitRtmpUi();

        InitRtmpOption();

        InitRtmpButtonEvent();

        InitVideoRecordButton();

        InitChangeRoomTitleButton();

        //GBS값을 LocationManager로 얻어온다.


        gpsInfo=new GpsInfo(getApplicationContext());

        Log.e("YaseaStreamActivity","위도값"+gpsInfo.getLatitude());
        Log.e("YaseaStreamActivity","경도값"+gpsInfo.getLongitude());

    }

    public void InitStarRecommendCheck()
    {
        //달풍선 및 추천수를 확인하는 액티비티를 띄우는 버튼의 이벤트를 등록한다.
        ImageButton starcheckbtn=(ImageButton)findViewById(R.id.starcheckbtn);
        starcheckbtn.setOnClickListener(this);

        new moonStarInitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new recommendInitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    public void InitAnimationUi()
    {
        final ImageView moonimage=(ImageView)findViewById(R.id.moonimage);
        final TextView moongifttext=(TextView)findViewById(R.id.moongift_text);
        final GravView grav_anim=(GravView)findViewById(R.id.grav);

        moonimage.setVisibility(View.INVISIBLE);
        moongifttext.setVisibility(View.INVISIBLE);
        grav_anim.setVisibility(View.INVISIBLE);
    }






    private void InitDB()
    {
        shared=getSharedPreferences("logininfo", Activity.MODE_PRIVATE);
        handler=new Handler();

        //sqlite 초기화
        DbOpenHelper h=new DbOpenHelper(getApplicationContext());
        h.open();//SQLiteOpenHelper 구현
        sqlite= h.getReadableDb();
    }

    private void InitChatUi()
    {
        chatbutton=(ImageButton)findViewById(R.id.chattingbutton);
        chat_layout=(LinearLayout)findViewById(R.id.chat_layout);
        //리스트뷰 설정
        chatlistview=(ListView)findViewById(R.id.chat_listview);
        textvalue=(EditText)findViewById(R.id.textvalue);

        inputbutton=(Button)findViewById(R.id.inputbutton);
    }


    private void InitChatListView()
    {
        final String bjnickname=shared.getString("bjnickname",null);

        sqlite.execSQL("delete from message where bjnickname='"+bjnickname+"';");
        sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','알림','[알림] 방송에 입장하였습니다.','#FFE400')");


        //채팅 데이터 초기화
        Cursor c=sqlite.rawQuery("select * from message",null);
        //어뎁터 초기화
        chattingadapter=new ChattingCursorAdapterForYasea(getApplicationContext(),c);
        chattingadapter.changeCursor(c);
        chatlistview.setAdapter(chattingadapter);
    }

    private void InitChatButtonEvent()
    {

        inputbutton.setOnClickListener(this);
        //레이아웃 보이게 하는 부분.
        chatbutton.setOnClickListener(this);

    }
    private void startNettyThread()
    {
        //////네티시작////
        new nettythread().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,global.getIP());
    }

    private void InitRtmpUi()
    {

        rtmpurl="rtmp://"+global.getIP()+":1935/dash/"+shared.getString("userid","user1");
        // initialize url.
        final EditText efu = (EditText) findViewById(R.id.url);
        efu.setText(rtmpurl);
        efu.setVisibility(View.GONE);

        btnPublish = (ImageButton) findViewById(R.id.publish);
        btnPublish2=(ImageButton)findViewById(R.id.publish2);
        btnSwitchCamera = (ImageButton) findViewById(R.id.swCam);
        btnRecord = (Button) findViewById(R.id.record);
        btnSwitchEncoder = (Button) findViewById(R.id.swEnc);

    }

    private void InitRtmpOption()
    {

        mPublisher = new SrsPublisher((SrsCameraView) findViewById(R.id.glsurfaceview_camera));
        mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
        mPublisher.setRtmpHandler(new RtmpHandler(this));
        mPublisher.setRecordHandler(new SrsRecordHandler(this));
        mPublisher.setPreviewResolution(640, 360);
        mPublisher.setOutputResolution(360, 640);
        mPublisher.setVideoHDMode();
        mPublisher.startCamera();
    }

    private void InitRtmpButtonEvent()
    {

        btnPublish.setOnClickListener(this);

        btnPublish2.setOnClickListener(this);

        btnSwitchCamera.setOnClickListener(this);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (btnRecord.getText().toString().contentEquals("record")) {

                    if (mPublisher.startRecord(recPath)) {
                        btnRecord.setText("pause");
                    }
                } else if (btnRecord.getText().toString().contentEquals("pause")) {
                    mPublisher.pauseRecord();
                    btnRecord.setText("resume");
                } else if (btnRecord.getText().toString().contentEquals("resume")) {
                    mPublisher.resumeRecord();
                    btnRecord.setText("pause");
                }
            }
        });

        //마지막 인코딩 버튼을 바꾸고 인코딩 모드를 설정하는 부분
        btnSwitchEncoder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnSwitchEncoder.getText().toString().contentEquals("soft encoder")) {
                    mPublisher.switchToSoftEncoder();
                    btnSwitchEncoder.setText("hard encoder");
                } else if (btnSwitchEncoder.getText().toString().contentEquals("hard encoder")) {
                    mPublisher.switchToHardEncoder();
                    btnSwitchEncoder.setText("soft encoder");
                }
            }
        });
    }


    private void InitVideoRecordButton()
    {
        recordbutton=(ImageButton)findViewById(R.id.recordbutton);
        recordbutton.setTag("record");
        recordbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //녹화버튼을 눌렀을때 대화상자를 설정한다.

                AlertDialog.Builder ad=new AlertDialog.Builder(YaseaStreamActivity.this);
                ad.setTitle("동영상 업로드");
                ad.setMessage("현재 방송중인 화면을 동영상 녹화 하시겠습니까?");
                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });
                ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        //녹화 버튼 누를때마다 recPath 갱신해볼까??

                        Log.e("recordbutton",System.currentTimeMillis()+"");
                        Log.e("recordbutton",System.currentTimeMillis()/1000+"");

                        String k=System.currentTimeMillis()/1000+"";
                        long time=Long.parseLong(k)*1000;
                        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy년 MM월 dd일 hh:mm:ss");
                        currenttimestring= dayTime.format(new Date(time));

                        Log.e("recordbutton",currenttimestring);

                        recPath = Environment.getExternalStorageDirectory().getPath() + "/"+System.currentTimeMillis()+"_"+shared.getString("userid",null)+"_"+roomtitle.getText().toString()+".mp4";

                        //1 태그 달기!!
                        if((recordbutton.getTag().equals("record")))
                        {
                            //long time = System.currentTimeMillis();
                            //SimpleDateFormat dayTime = new SimpleDateFormat("yyyy년 MM월 dd일 hh:mm:ss");
                            //currenttimestring= dayTime.format(new Date(time));
                            //String videofilename=shared.getString("userid",null)+"_"+roomtitle.getText().toString()+"_"+currenttimestring+".mp4";

                            Log.e("recordbutton","recPath="+recPath);

                            if(mPublisher.startRecord(recPath))
                            {
                                recordbutton.setTag("recording");
                            }else
                            {
                                Toast.makeText(getApplicationContext(),"스트리밍중이 아닙니다...",Toast.LENGTH_LONG).show();
                            }
                        }

                        if(recordbutton.getTag().equals("recording"))
                        {
                            Toast.makeText(getApplicationContext(),"현재 녹화중입니다...",Toast.LENGTH_LONG).show();
                        }

                    }
                });
                ad.show();
            }
        });
    }

    private void InitChangeRoomTitleButton()
    {

        /////////////////////////방 제목 변경부분//////////////////////////////////////

        ImageButton titlechangebtn=findViewById(R.id.titlechangbtn);
        titlechangebtn.setOnClickListener(this);
        roomtitle=findViewById(R.id.roomtitle);
        final String text=shared.getString("nickname",null)+"님이 스트리밍중...";

        //영문버전 한글버전
        //roomtitle.setText(text);
        roomtitle.setText("VideoExample100");

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            // 채팅입력 버튼을 눌렀을때의 이벤트이다.
            case R.id.inputbutton :{

                // RGB값을 나타낸다.
                // 노랑 -> #FFE400 // 분홍 -> #FF00DD // 흰색 -> #FFFFFF


                final String bjnickname=shared.getString("bjnickname",null);
                final String nickname=shared.getString("nickname",null);
                final String text=textvalue.getText().toString();
                final String color="#FFE400";

                //어뎁터에 값을 추가하기 위한 쿼리문이다.
                sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','"+nickname+"','"+text+"','"+color+"')");
                notificationchat();

                //네티서버로 텍스트 값을 전송한다.
                //입력 형식은 다음과 같다.(02: 비제이 : 내 닉네임 : 텍스트)
                chatclient.writeandflush("02:"+bjnickname+":"+nickname+":"+text);
                textvalue.setText("");
                break;
            }

            //채팅창을 화면에 표시할때의 이벤트이다.
            case R.id.chattingbutton : {

                if (chat_layout.getVisibility() == View.VISIBLE) {
                    chat_layout.setVisibility(View.GONE);
                } else {
                    chat_layout.setVisibility(View.VISIBLE);
                }
                break;
            }
            //방송시작 버튼(동그라미)을 눌렀을때의 이벤트이다.
            case R.id.publish :{

                if(v.getId()==R.id.publish)
                {

                    //버튼이 일단 사라진다.
                    btnPublish.setVisibility(View.INVISIBLE);
                    //스탑버튼이 생긴다.
                    btnPublish2.setVisibility(View.VISIBLE);

                    //방송을 시작한다.
                    //방송을 시작하는 부분
                    mPublisher.startPublish(rtmpurl);
                    mPublisher.startCamera();
                    btnSwitchEncoder.setEnabled(false);

                    //방송을 시작하면 타이머를 켠다 (10초마다 썸네일 이미지를 저장하도록)
                }
                break;
            }

            //방송 중지버튼(네모)를 눌렀을 떄의 이벤트이다.
            case R.id.publish2 :{
                if(v.getId()==R.id.publish2)
                {

                    //버튼이생긴다
                    btnPublish.setVisibility(View.VISIBLE);
                    //스탑버튼이 사라진다.
                    btnPublish2.setVisibility(View.INVISIBLE);

                    //방송이 종료된다.
                    mPublisher.stopPublish();
                    mPublisher.stopRecord();


                    //버튼 작동이 되게한다.
                    btnSwitchEncoder.setEnabled(true);

                    //버튼을 끄면 타이머가 종료되고 썸네일부분도 종료된다.
                    //timertask.cancel();

                }
                break;
            }
            //카메라 전환 버튼을 눌렀을 때의 이벤트이다.
            case R.id.swCam:
            {
                mPublisher.switchCameraFace((mPublisher.getCamraId() + 1) % Camera.getNumberOfCameras());
                break;
            }

            //방 제목을 변경하는 버튼의 이벤트이다.
            case R.id.titlechangbtn:
            {
                Intent intent=new Intent(getApplicationContext(),YaseaRoomtitleActivity.class);
                startActivityForResult(intent,1);
                break;
            }

            //현재 달풍선과 추천수를 확인하기 위한 버튼의 이벤트이다.
            //1. 다음과 같은 형식의 문자열을 파싱한다 [  달풍선 보유 현황 : 100개 //BJ 추천 횟수 : 100회 ]
            //2. 파싱한 값의 데이터를  YaseaMoonCheckActivity로 intent에 담아서 전송한다.

            case R.id.starcheckbtn:
            {
                final TextView star_presence=(TextView)findViewById(R.id.starPresence_tv);
                final TextView rec_presence=(TextView)findViewById(R.id.recPresence_tv);

                Intent intent=new Intent(YaseaStreamActivity.this, YaseaMoonCheckActivity.class);
                String str=star_presence.getText().toString();
                int index1=0;
                int index2=str.indexOf("개");
                str=str.substring(index1,index2);
                intent.putExtra("starcount",str);

                String str2=rec_presence.getText().toString();
                index1=0;
                index2=str2.indexOf("회");
                str2=str2.substring(index1,index2);

                intent.putExtra("reccount",str2);
                startActivity(intent);


                break;
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case 1: {

                String roomtitle_=data.getExtras().getString("roomtitle");
                roomtitle.setText(roomtitle_);
                break;

            }
        }
    }





    /////////////////////////////////////////////////////////////////////////////////////////////





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.



        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //옵션 아이템 설정창 => 이부분은 필터설정하는 부분이니까 나중에 추가해서 넣자 !
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else {
            switch (id) {
                case R.id.cool_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.COOL);
                    break;
                case R.id.beauty_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.BEAUTY);
                    break;
                case R.id.early_bird_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.EARLYBIRD);
                    break;
                case R.id.evergreen_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.EVERGREEN);
                    break;
                case R.id.n1977_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.N1977);
                    break;
                case R.id.nostalgia_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.NOSTALGIA);
                    break;
                case R.id.romance_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.ROMANCE);
                    break;
                case R.id.sunrise_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.SUNRISE);
                    break;
                case R.id.sunset_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.SUNSET);
                    break;
                case R.id.tender_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.TENDER);
                    break;
                case R.id.toast_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.TOASTER2);
                    break;
                case R.id.valencia_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.VALENCIA);
                    break;
                case R.id.walden_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.WALDEN);
                    break;
                case R.id.warm_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.WARM);
                    break;
                case R.id.original_filter:
                default:
                    mPublisher.switchCameraFilter(MagicFilterType.NONE);
                    break;
            }
        }
        setTitle(item.getTitle());

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final ImageButton btn = (ImageButton) findViewById(R.id.publish);

        //이미지버튼이 사용이 가능하게 한다.
        btn.setEnabled(true);
        mPublisher.resumeRecord();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPublisher.pauseRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPublisher.stopPublish();
        mPublisher.stopRecord();

        final String bjnickname=shared.getString("bjnickname","");
        final String nickname=shared.getString("nickname","");
        chatclient.writeandflush("03:"+bjnickname+":"+nickname+":"+"퇴장");


        chatclient.setworking(false);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPublisher.stopEncode();
        mPublisher.stopRecord();
        //구성이 변경되면 방송이 멈춘다.


        //방향을 일단 잡는다.
        mPublisher.setScreenOrientation(newConfig.orientation);


        /*
        //stop이라고 표시된 상태일때는 => 방송중인 상태일때는
        if (btnPublish.getText().toString().contentEquals("stop")) {

            mPublisher.startEncode();
        }
        */

        if(btnPublish2.getVisibility()==View.VISIBLE)
        {
            mPublisher.startEncode();
        }


        mPublisher.startCamera();
    }

    private static String getRandomAlphaString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    private static String getRandomAlphaDigitString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    private void handleException(Exception e) {
        try {


            Log.e("recordbutton","예외발생???");
            Log.e("recordbutton","예외"+e.getMessage());
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

            //예외가 발생하면 방송이 종료된다.
            mPublisher.stopPublish();
            mPublisher.stopRecord();

            /*
            //버튼을 방송중 상태로 바꾼다.
            btnPublish.setText("publish");
            */

            //방송버튼이 생긴다
            btnPublish.setVisibility(View.VISIBLE);
            //스탑버튼이 사라진다
            btnPublish2.setVisibility(View.INVISIBLE);

            btnRecord.setText("record");
            //이 시점에서 서버로 업로드해야함***************************************************************

            btnSwitchEncoder.setEnabled(true);

        } catch (Exception e1) {

        }
    }

    // Implementation of SrsRtmpListener.

    @Override
    public void onRtmpConnecting(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpConnected(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoStreaming() {
    }

    @Override
    public void onRtmpAudioStreaming() {
    }

    @Override
    public void onRtmpStopped() {

        Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRtmpDisconnected() {
        Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {
        Log.i(TAG, String.format("Output Fps: %f", fps));
    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Video bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Video bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Audio bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Audio bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpSocketException(SocketException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
        handleException(e);
    }

    // Implementation of SrsRecordHandler.

    @Override
    public void onRecordPause() {
        Toast.makeText(getApplicationContext(), "Record paused", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordResume() {
        Toast.makeText(getApplicationContext(), "Record resumed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordStarted(String msg) {
        //Toast.makeText(getApplicationContext(), "Recording file: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordFinished(String msg) {


        Log.e("recordbutton","2222222222222222----------------------22222222222222222222222");
        Toast.makeText(getApplicationContext(), "MP4 file saved: " + msg, Toast.LENGTH_SHORT).show();

        //방송이 종료되면 로컬에 저장된 동영상을 서버로 업로드한다.

        if(recordbutton.getTag().equals("recording"))
        {
            recordbutton.setTag("record");
            //동영상 업로드 작업 시작!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

            Log.e("recordbutton","22222222222222222222222222");

            new videoFileUploadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


            //new videoFileUploadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    @Override
    public void onRecordIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    // Implementation of SrsEncodeHandler.

    @Override
    public void onNetworkWeak() {
        Toast.makeText(getApplicationContext(), "Network weak", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNetworkResume() {
        Toast.makeText(getApplicationContext(), "Network resume", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    ////////////////////////////////////채팅 어뎁터 부분 ///////////////////////////////////////




    public void notificationchat()
    {

        Cursor c=sqlite.rawQuery("select * from message where bjnickname='"+shared.getString("bjnickname",null)+"'",null);
        chattingadapter.changeCursor(c);
        //리스트뷰 초기화
        //chatlistview=(ListView)findViewById(R.id.chat_listview);
        //chatlistview.setAdapter(chattingadapter);
        chattingadapter.notifyDataSetChanged();
        chatlistview.setSelection(c.getCount()-1);

    }


    ////////////////////////////네티부분 시작 ////////////////////////////////////////////

    public class ChatClient {

        private final String host;
        private final int port;
        Channel channel;

        public ChatClient(String host,int port)
        {
            this.host=host;
            this.port=port;
        }


        public void writeandflush(String str)
        {
            channel.writeAndFlush(str+"\r\n");
        }


        boolean working=true;

        public void setworking(boolean working)
        {
            this.working=working;
        }


        public void run() throws Exception{

            EventLoopGroup group=new NioEventLoopGroup();




            try{
                Bootstrap bootstrap=new Bootstrap()
                        .group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChatClientInitializer());

                channel=bootstrap.connect(host,port).sync().channel();


                String str=textvalue.getText().toString();
                InputStream is = new ByteArrayInputStream(str.getBytes());
                //BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
                BufferedReader in=new BufferedReader(new InputStreamReader(is));

                String bjnickname=shared.getString("bjnickname","");
                String nickname=shared.getString("nickname","");

                channel.writeAndFlush("01:"+bjnickname+":"+nickname+":"+"입장"+"\r\n");

                while(working){
                }
            }
            finally {
                Log.e("채널소켓이 이미 종료되었나","yes");
                group.shutdownGracefully();
            }
        }
    }

    public class ChatClientHandler extends SimpleChannelInboundHandler<String> {

        @Override
        public void channelRead0(ChannelHandlerContext arg0, String message) throws Exception{

            System.out.println(message);
            Log.e("네티서버로부터 들어오는값",message);

            //String command=message.substring(0,2);
            //Log.e("네티서버로부터 온 커맨드",command);

            final String bjnickname=shared.getString("bjnickname","");
            final String nickname=shared.getString("nickname","");

            // 노랑 -> #FFE400
            // 분홍 -> #FF00DD
            // 흰색 -> #FFFFFF

            if(!message.contains(":"))
            {
                sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','"+nickname+"','"+"[알림] "+message+"','#FFE400')");
                //notificationchat();
            }
            else
            {
                String tokens[]=message.split(":");

                //만약에 나한테 온 메세지라면
                if(tokens[0].equals(nickname))
                {
                    Log.e("네티서버로부터 들어오는값55555",message);
                }
                //bj에게 온 메세지라면
                else if(tokens[0].equals(bjnickname)) {

                    sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','"+tokens[0]+"','"+tokens[1]+"','#FFE400')");
                    //notificationchat();

                }

                else if(tokens[0].equals("달풍선"))
                {
                    //1. 서버에서 해당 형식으로 메세지가 도착한다. =>달풍선:네티:100개
                    //2. sqlite에 입력할 텍스트는 다음과 같은 형식이다. => 네티님께서 달풍선 100개를 선물하셨습니다.
                    //3. 애니메이션의 위젯들을 Visible로 바꾸고 5초뒤에 위젯들을 GONE으로 바꾼다.

                    String moonstartext="[알림] "+tokens[1]+"님께서 달풍선 "+tokens[2]+"개를 선물하셨습니다.";
                    sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','"+tokens[0]+"','"+moonstartext+"','#47C83E')");
                    moonGiftUiChange(moonstartext);


                    new moonStarInitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                //추천이 온다면 주황색 메세지를 입력한다.

                else if(tokens[0].equals("추천"))
                {
                    //1. 서버에서 해당 형식으로 메세지가 도착한다. => 추천:네티
                    String recommendtext="[추천] "+tokens[1]+"님께서 추천하셨습니다.";
                    sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','"+tokens[0]+"','"+recommendtext+"','#ED4C00')");

                    new recommendInitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }


                // 다른 유저에게 온 메세지라면
                else
                {
                    sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','"+tokens[0]+"','"+tokens[1]+"','#FFFFFF')");
                    //notificationchat();

                }

            }


            new Thread(new Runnable() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("네티서버로부터 들어오는값7777","ㅋㅋㅋ");
                            notificationchat();
                        }
                    });
                }
            }).run();

        }

        private void moonGiftUiChange(final String moonstartext)
        {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            final ImageView moonimage=(ImageView)findViewById(R.id.moonimage);
                            final TextView moongifttext=(TextView)findViewById(R.id.moongift_text);
                            final GravView grav_anim=(GravView)findViewById(R.id.grav);

                            moongifttext.setText(moonstartext);

                            moonimage.setVisibility(View.VISIBLE);
                            moongifttext.setVisibility(View.VISIBLE);
                            grav_anim.setVisibility(View.VISIBLE);

                        }
                    });
                }
            }).run();



            final TimerTask timertask=new TimerTask() {
                @Override
                public void run() {

                    YaseaStreamActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            final ImageView moonimage=(ImageView)findViewById(R.id.moonimage);
                            final TextView moongifttext=(TextView)findViewById(R.id.moongift_text);
                            final GravView grav_anim=(GravView)findViewById(R.id.grav);

                            moonimage.setVisibility(View.INVISIBLE);
                            moongifttext.setVisibility(View.INVISIBLE);
                            grav_anim.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            };

            final Timer timer=new Timer();
            timer.schedule(timertask,5000);


        }

    }

    public class ChatClientInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel arg0) throws Exception{

            ChannelPipeline pipeline=arg0.pipeline();


            pipeline.addLast("framer",new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            pipeline.addLast("decoder",new StringDecoder());
            pipeline.addLast("encoder",new StringEncoder());
            pipeline.addLast("handler",new ChatClientHandler());

        }

    }


    public class nettythread extends AsyncTask<String,Void,Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... latlng) {

            try {
                chatclient=new ChatClient(latlng[0], 8000);
                chatclient.run();
            }catch(Exception e)
            {
                Log.e("netty에러",e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e("AsyncTask() 종료","종료");
        }
    }

    /////////////////////////////FFmpeg로 이미지 썸네일 업로드하는 부분/////////////////////////////


    public class ffmpegthumbtask extends AsyncTask <Void,Void,Void>{

        String result="";
        String FFMPEGTHUMBURL="http://"+global.getIP()+"/ffmpegthumb.php";



        private Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                result=e.getMessage();
                Log.e("YaseaStream","[FFmpeg에러]"+result);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                result=response.body().string();
                Log.e("YaseaStream","[FFmpeg결과] :"+result);
            }
        };

        @Override
        protected Void doInBackground(Void... voids) {


            OkHttpClient client=new OkHttpClient();
            String userid=shared.getString("userid",null);

            //방제목, 패스워드,닉네임 , 시청인원 제한, 현재 시청인원, 이미지 url

            Log.e("YaseaStream","userid="+userid);

            RequestBody body=new FormBody.Builder()
                    .add("userid",userid)
                    .build();



            Request request=new Request.Builder()
                    .url(FFMPEGTHUMBURL)
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





    //////////////////////////타이머를 재생성하는 부분////////////////////////


    public TimerTask timerTaskMaker(){
        TimerTask tempTask = new TimerTask() {
            @Override
            public void run() {
                new ffmpegthumbtask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);;
                Log.e("YaseaStreamActivity","서버 전송 실행??Okay");
            }
        };
        return tempTask;
    }


    /////////////////////////비디오를 비동기로 업로드 하는 부분///////////////////////////////


    private class videoFileUploadTask extends AsyncTask<String,String,Void>
    {


        public final String UPLOAD_URL= "http://"+global.getIP()+"/videofileupload.php";
        private int serverResponseCode;

        boolean progressStarted=true;

        String error;

        byte[] bytes;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... location) {

            Log.e("recordbutton","33333333333333333333333333");

            //비디오 파일을 업로드한다.
            //현재 로컬에 저장된 비디오파일의 경로를 매개변수로 입력받는다.
            error=uploadVideo(recPath,gpsInfo.getLatitude()+"",gpsInfo.getLongitude()+"");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.e("recordbutton","44444444444444444"+error);

        }

        @Override
        protected void onProgressUpdate(String... progress) {
            super.onProgressUpdate(progress);

            pDialog.setProgress(Integer.parseInt(progress[0]));
        }



        //이 함수는 local에 저장된 파일을 서버로 http post를 이용하여 서버로 업로드 해주는 함수이다.

        //서버에 전달해야할 값 => 비디오파일에대한 정보(현재시간+userid+방제목), 위도와 경도에 대한 정보.

        private String setValue(String key, String value) {
            return "Content-Disposition: form-data; name=\"" + key + "\"r\n\r\n"
                    + value;
        }

        public String uploadVideo(String file,String latitude,String longitude) {

            String fileName = file;
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            //int maxBufferSize = 1 * 1024 * 1024;
            int maxBufferSize=1024;
            File sourceFile = new File(file);

            ////////////
            //파일의 크기
            bytes = new byte[(int) sourceFile.length()];

            //Log.e("recordbutton","파일크기"+bytes.length);

            if (!sourceFile.isFile()) {
                Log.e("Huzza", "Source File Does not exist");
                return null;
            }

            try {
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(UPLOAD_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);

                //서버에서 필요한 key값을 설정하는 부분이다.
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("charset","utf-8");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("myFile", fileName);


                //데이터 아웃풋 스트림을 초기화한다.
                //DataOutputStream의 특징은 기본적인 바이트스트림과 다르게 기본자료형으로(char,int,double) 입력과 출력을 할 수 있다는 점이다.
                //UTF8이나 유니코드로도 입출력 할 수 있다.

                dos = new DataOutputStream(conn.getOutputStream());

                //Log.e("recordbutton",fileName+"");
                //Log.e("recordbutton","파일이름"+fileName+"");

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes(setValue("lat",latitude));
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes(setValue("lng",longitude));
                dos.writeBytes(lineEnd);


                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"myFile\";filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);


                bytesAvailable = fileInputStream.available();
                Log.i("Huzza", "Initial .available : " + bytesAvailable);

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);


                final int maxprogress=bytes.length;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        pDialog = new ProgressDialog(YaseaStreamActivity.this);
                        pDialog.setMessage("Video file uploading.. Please wait....");
                        pDialog.setIndeterminate(false);
                        pDialog.setMax(maxprogress);
                        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        pDialog.setCancelable(true);
                        pDialog.show();
                        progressStarted=false;

                    }
                });
                /*로직 대충 이런식
                int bufferLength = 1024;
                for (int i = 0; i < bytes.length; i += bufferLength) {
                    int progress = (int)((i / (float) bytes.length) * 100);
                    publishProgress(progress);
                    if (bytes.length - i >= bufferLength) {
                        outputStream.write(bytes, i, bufferLength);
                    } else {
                        outputStream.write(bytes, i, bytes.length - i);
                    }
                }
                */
                int i=0;


                while (bytesRead > 0) {

                    i=i+bufferSize;
                    //i=i+bytesRead;


                    //Log.e("recordbutton","byte: i값"+i);
                    //Log.e("recordbutton","전체 바이트값"+bytes.length);
                    //int progress = (int)((i / (float) bytes.length) * 100);
                    //publishProgress(progress+"");

                    publishProgress(i+"");
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = conn.getResponseCode();

                Log.e("recordbutton","서버 응답"+serverResponseCode);

                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (serverResponseCode == 200) {
                StringBuilder sb = new StringBuilder();
                try {


                    Log.e("recordbutton","66666666666666666666666서버응답 OKay");
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                            .getInputStream()));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    rd.close();
                } catch (IOException ioex) {
                }
                return sb.toString();
            }else {

                Log.e("recordbutton","66666666666666666666666서버응답 No");
                return "Could not upload";
            }
        }
    }


    public class moonStarInitTask extends AsyncTask<Void,Void,String>
    {
        @Override
        protected String doInBackground(Void... voids) {

            final String URL="http://"+global.getIP()+"/moonstar_init.php";


            SharedPreferences shared=getSharedPreferences("logininfo",Activity.MODE_PRIVATE);
            String sendid=shared.getString("userid",null);



            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("sendid",sendid)
                    .build();

            Request request=new Request.Builder()
                    .url(URL)
                    .header("Content-Type","text/html")
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
            Toast.makeText(getApplicationContext(),result+"",Toast.LENGTH_LONG).show();
            Log.e("달풍선","gift 작업 결과 : "+result);

            moonCountInitUi(Integer.parseInt(result));

        }

        private void moonCountInitUi(int initmooncount)
        {
            final TextView starPresence_tv=(TextView)findViewById(R.id.starPresence_tv);
            String mooncountText=mooncountText=initmooncount+"개";
            starPresence_tv.setText(mooncountText);
        }

    }



    public class recommendInitTask extends AsyncTask<Void,Void,String>
    {
        @Override
        protected String doInBackground(Void... voids) {

            final String URL="http://"+global.getIP()+"/recommend_init.php";


            SharedPreferences shared=getSharedPreferences("logininfo",Activity.MODE_PRIVATE);
            String bjid=shared.getString("userid",null);



            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("bjid",bjid)
                    .build();

            Request request=new Request.Builder()
                    .url(URL)
                    .header("Content-Type","text/html")
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
            Toast.makeText(getApplicationContext(),result+"",Toast.LENGTH_LONG).show();
            Log.e("달풍선","gift 작업 결과 : "+result);

            recommendInitUi(Integer.parseInt(result));

        }

        private void recommendInitUi(int initreccount)
        {
            final TextView recPresence_tv=(TextView)findViewById(R.id.recPresence_tv);
            String mooncountText=mooncountText=initreccount+"회";
            recPresence_tv.setText(mooncountText);
        }
    }


}