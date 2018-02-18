package com.example.user.dailytv.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.user.dailytv.Adapter.ChattingCusorAdapter;
import com.example.user.dailytv.MyDatabase.DbOpenHelper;
import com.example.user.dailytv.MyDatabase.GlobalVariable;
import com.example.user.dailytv.R;
import com.github.glomadrian.grav.GravView;
import com.github.glomadrian.grav.figures.Grav;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.quickblox.sample.groupchatwebrtc.App;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

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
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ExoPlayer2Activity extends AppCompatActivity implements VideoRendererEventListener,Button.OnClickListener {


    private static final String TAG = "MainActivity";
    private SimpleExoPlayer player;
    private SimpleExoPlayerView simpleExoPlayerView;

    private ProgressDialog progressBar;
    private int progressBarflag=0;
    private boolean progressBarswitch=false;

    static final int BUY_MOON_WORK=1123;



    final App.GlobalVariable global=App.getGlobal();




    //-----------------채팅부분------------------//
    ListView chatlistview;

    SQLiteDatabase sqlite;
    EditText textvalue;
    SharedPreferences shared;
    ChattingCusorAdapter chattingadapter;

    //------------------네티부분----------------//

    ChatClient chatclient;
    Handler h;


    //선물하기
    RelativeLayout giftlayout;
    EditText mooncount_edit;
    TextView mooncount_text;

    RelativeLayout moon_anim_layout;

    GravView gravview;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exoplayer2activity_portrait);










        initUi();

        initAnimationUi();

        initActionbar();

        initNettyChatting();

         //----------------------------------------방송부분 설정 시작 -------------------------//

        initExoPlayer();

        initGitfMoonPopup();

        initGitfMoon();

        initBuyMoonButton();

        initRecommend();


        //네티 채팅 쓰레드를 시작하는 부분이다.
        new nettythread().execute(global.getIP());


    }//End of onCreate


    private void initBuyMoonButton()
    {
        final Button buyMoonBtn=(Button)findViewById(R.id.buyMoonBtn);
        buyMoonBtn.setOnClickListener(this);
    }


    public void initActionbar()
    {
        ActionBar actionbar=getSupportActionBar();

        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeButtonEnabled(true);

        actionbar.setTitle("뒤로가기");

    }

    private void initRecommend()
    {
        new recommendInitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        ImageButton likeBtn=(ImageButton)findViewById(R.id.likeBtn);
        likeBtn.setOnClickListener(this);

    }


    public void initGitfMoonPopup()
    {

        giftlayout=(RelativeLayout)findViewById(R.id.giftlayout);
        giftlayout.setVisibility(View.GONE);
        ImageButton gitfpopup=(ImageButton) findViewById(R.id.giftpopup);
        gitfpopup.setOnClickListener(this);
    }

    public void initGitfMoon()
    {

        mooncount_edit=(EditText)findViewById(R.id.mooncount_edit);

        //별풍선의 라이오 버튼의 리스너를 등록한다.
        RadioGroup radiogroup=(RadioGroup)findViewById(R.id.radiogroup);
        RadioGroup.OnCheckedChangeListener radiolistener=new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radio1)
                {
                    mooncount_edit.setText("10");
                }else if(checkedId==R.id.radio2)
                {
                    mooncount_edit.setText("50");
                }
                else if(checkedId==R.id.radio3)
                {
                    mooncount_edit.setText("100");
                }

            }
        };

        radiogroup.setOnCheckedChangeListener(radiolistener);

        final Button giftbutton=(Button)findViewById(R.id.giftbutton);
        giftbutton.setOnClickListener(this);

        mooncount_text=findViewById(R.id.mooncount_text);


        new moonStarInitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }




    public void initUi()
    {

        //핸들러 부분
        h=new Handler();
        shared=getSharedPreferences("logininfo", Activity.MODE_PRIVATE);

    }

    public void initAnimationUi()
    {
        final ImageView moonimage=(ImageView)findViewById(R.id.moonimage);
        final TextView moongifttext=(TextView)findViewById(R.id.moongift_text);
        final GravView grav_anim=(GravView)findViewById(R.id.grav);

        moonimage.setVisibility(View.INVISIBLE);
        moongifttext.setVisibility(View.INVISIBLE);
        grav_anim.setVisibility(View.INVISIBLE);
    }

    // 네티 채팅을 시작하기 위해 필요한 어뎁터와 리스트뷰 SQliteDB를 초기화하는 함수이다

    public void initNettyChatting()
    {


        final String bjnickname=shared.getString("bjnickname",null);
        textvalue=(EditText)findViewById(R.id.textvalue);



        //sqlite 초기화
        DbOpenHelper h=new DbOpenHelper(getApplicationContext());
        h.open();//SQLiteOpenHelper 구현
        sqlite= h.getReadableDb();

        sqlite.execSQL("delete from message where bjnickname='"+bjnickname+"';");
        sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','알림','[알림] 방송에 입장하였습니다.','#FFE400')");



        //채팅 데이터 초기화
        Cursor c=sqlite.rawQuery("select * from message",null);

        //어뎁터 초기화
        chattingadapter=new ChattingCusorAdapter(getApplicationContext(),c);
        chattingadapter.changeCursor(c);


        //리스트뷰 초기화
        chatlistview=(ListView)findViewById(R.id.chat_listview);
        chatlistview.setAdapter(chattingadapter);

        //입력버튼 리스너 설정.
        Button inputbutton;
        inputbutton=(Button)findViewById(R.id.inputbutton);
        inputbutton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        //채팅창을 입력하였을때 발생하는 이벤트이다.
        if(v.getId()==R.id.inputbutton)
        {
            final String bjnickname=shared.getString("bjnickname",null);
            final String nickname=shared.getString("nickname",null);
            final String text=textvalue.getText().toString();
            final String color="#FF00DD";

            // 노랑 -> #FFE400
            // 분홍 -> #FF00DD
            // 흰색 -> #FFFFFF


            //Sqlite(어뎁터)에 값 추가하기.
            sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','"+nickname+"','"+text+"','"+color+"')");
            notificationchat();

            //네티에 값 추가하기
            // 02: 비제이 : 내 닉네임 : 텍스트
            chatclient.writeandflush("02:"+bjnickname+":"+nickname+":"+text);
            textvalue.setText("");
        }
        else if(v.getId()==R.id.giftpopup)
        {


            if(giftlayout.getVisibility()==View.GONE)
            {
                giftlayout.setVisibility(View.VISIBLE);
            }
            else
            {
                giftlayout.setVisibility(View.GONE);
                mooncount_edit.setText("0");
            }

        }
        else if(v.getId()==R.id.giftbutton)
        {
            new moonGiftTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        else if(v.getId()==R.id.likeBtn)
        {
            new recommendLikeUpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }
        else if (v.getId()==R.id.buyMoonBtn)
        {
            Intent intent=new Intent(ExoPlayer2Activity.this,BuyMoonActivity.class);

            startActivityForResult(intent,BUY_MOON_WORK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        Toast.makeText(getApplicationContext(),"onActivityResult",Toast.LENGTH_LONG).show();


        if(resultCode!=RESULT_OK)
        {
            return;
        }


        switch(requestCode) {
            case BUY_MOON_WORK: {

                //1. 디비 작업이 업데이트 된 이후 결제화면의 액티비티가 종료되고 이전 액티비티로 돌아왔을때
                //2. 화면의 별풍선 갯수 UI를 결제한 별풍선의 수 만큼 증가시킨다.


                //1. 현재 달풍선 텍스트에서 숫자값을 추출한다.
                //2. 추출한 숫자값을 증가한 달풍선갯수에 더한다.
                //3. 더한값을 다시 텍스트뷰에 설정한다.

                try {
                    String mooncountText = mooncount_text.getText().toString();
                    mooncountText = mooncountText.substring(mooncountText.indexOf(" ") + 1, mooncountText.length() - 1);

                    int mooncount = Integer.parseInt(mooncountText);
                    int up_mooncount = Integer.parseInt(data.getExtras().getString("mooncount"));
                    mooncount=mooncount+up_mooncount;

                    Toast.makeText(getApplicationContext(), "달풍선 " + up_mooncount + "개 구매성공", Toast.LENGTH_LONG).show();
                    String moontext = "달풍선 " + mooncount + "개";
                    mooncount_text.setText(moontext);

                }
                catch (Exception e)
                {
                    Log.e("달풍선 계산 Exception",e.getMessage());
                }


            }
        }


    }

    public void initExoPlayer()

    {

        //프로그래스바 설정
        progressBar=new ProgressDialog(ExoPlayer2Activity.this);
        progressBar.setCancelable(true);
        progressBar.setMessage("버퍼링중입니다....");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // 1. Create a default TrackSelector
        // 트랙 셀렉터 설정 => 화면 이동하는 바라고 하자
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        // 데이터의 Load를 조절하는 컨트롤러
        LoadControl loadControl = new DefaultLoadControl();

        // 3. Create the player
        // 플레이어 생성
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        simpleExoPlayerView = new SimpleExoPlayerView(this);
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);

        //Set media controller
        //미디어 컨트롤러 생성
        simpleExoPlayerView.setUseController(false);
        simpleExoPlayerView.requestFocus();

        // Bind the player to the view.
        //플레이어 설정
        simpleExoPlayerView.setResizeMode(3);
        simpleExoPlayerView.setPlayer(player);



        // ADJUST HERE:
        //CHOOSE CONTENT: LiveStream / SdCard
        //LIVE STREAM SOURCE: * Livestream links may be out of date so find any m3u8 files online and replace:
        // Uri mp4VideoUri =Uri.parse("http://81.7.13.162/hls/ss1/index.m3u8"); //random 720p source
        // http://13.58.94.159:8080/dash/v1.mpd
        // Uri mp4VideoUri =Uri.parse("http://54.255.155.24:1935//Live/_definst_/amlst:sweetbcha1novD235L240P/playlist.m3u8"); //Radnom 540p indian channel
        Uri mp4VideoUri =Uri.parse("http://"+global.getIP()+":8080/dash/"+shared.getString("userid","user1")+".mpd"); //Radnom 540p indian channel

        //Uri mp4VideoUri =Uri.parse("FIND A WORKING LINK ABD PLUg INTO HERE"); //PLUG INTO HERE<------------------------------------------
        //비디오를 안드로이드 저장소에 저장하는 소스이다.
        //VIDEO FROM SD CARD: (2 steps. set up file and path, then change videoSource to get the file)
        //String urimp4 = "path/FileName.mp4"; //upload file to device and add path/name.mp4
        //Uri mp4VideoUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+urimp4);
        //Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();

        //Produces DataSource instances through which media data is loaded.
        //데이터 소스를 업데이트 하는 소스(스트림)
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), bandwidthMeterA);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();


        // 내가 구현한 dash 소스
        MediaSource videoSourceDash = new DashMediaSource(mp4VideoUri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);


        final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSourceDash);
        // Prepare the player with the source.
        player.prepare(loopingSource);
        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                Log.v(TAG, "Listener-onTimelineChanged...");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.v(TAG, "Listener-onTracksChanged...");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.v(TAG, "Listener-onLoadingChanged...isLoading:"+isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.v(TAG, "Listener-onPlayerStateChanged..." + playbackState);
                /*
                if(playbackState==3)
                {

                    progressBar.dismiss();
                    progressBarswitch=false;
                    //2 -> 방송이 시작됨
                    progressBarflag=2;
                }

                //방송이 시작되고 멈춤현상이 생겼을떄는
                if(playbackState==2&&progressBarflag==2&&!progressBarswitch)
                {
                    //기다리라는 다이어로그 표시
                    progressBar.show();
                    //스위치 온 -> 이미 다이어로그가 켜져있다.
                    progressBarswitch=true;

                }
                //방송이 시작되기 전에 나오는 2번 상태메세지는
                else if(playbackState==2&&progressBarflag==0)
                {
                    progressBar.dismiss();
                    progressBarswitch=false;
                }

                //방송이 시작되고 에러가 발생하면
                if(playbackState==1 && progressBarflag==2)
                {
                    progressBar.dismiss();
                    progressBarswitch=false;
                    progressBarflag=0;
                }
                //방송이 시작되기전에 주기적으로 이유없이 발생하는 에러메세지는
                else if(playbackState==1&&progressBarflag==0)
                {
                    progressBar.dismiss();
                    progressBarswitch=false;
                }
                */


                if(playbackState==3)
                {
                    progressBar.dismiss();
                    progressBarswitch=true;
                }


                if(playbackState==2&&progressBarswitch)
                {
                    progressBar.show();
                    progressBarswitch=false;
                }



                //playback State
                //1일때 에러 2일때 잠시멈춤 3일때 다시 시작
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.v(TAG, "Listener-onRepeatModeChanged...");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.v(TAG, "Listener-onPlayerError...");
                player.stop();
                player.prepare(loopingSource);
                player.setPlayWhenReady(true);
            }

            @Override
            public void onPositionDiscontinuity() {
                Log.v(TAG, "Listener-onPositionDiscontinuity...");
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.v(TAG, "Listener-onPlaybackParametersChanged...");
            }
        });

        /*
        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                Log.v(TAG, "Listener-onTimelineChanged...");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.v(TAG, "Listener-onTracksChanged...");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.v(TAG, "Listener-onLoadingChanged...isLoading:"+isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.v(TAG, "Listener-onPlayerStateChanged..." + playbackState);
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.v(TAG, "Listener-onRepeatModeChanged...");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.v(TAG, "Listener-onPlayerError...");
                player.stop();
                player.prepare(loopingSource);
                player.setPlayWhenReady(true);
            }

            @Override
            public void onPositionDiscontinuity() {
                Log.v(TAG, "Listener-onPositionDiscontinuity...");
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.v(TAG, "Listener-onPlaybackParametersChanged...");
            }
        });

        */

        player.setPlayWhenReady(true); //run file/link when ready to play.
        player.setVideoDebugListener(this); //for listening to resolution change and  outputing the resolution


    }

    @Override
    public void onVideoEnabled(DecoderCounters counters) {

    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

    }

    @Override
    public void onVideoInputFormatChanged(Format format) {

    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        Log.v(TAG, "onVideoSizeChanged ["  + " width: " + width + " height: " + height + "]");
        //resolutionTextView.setText("RES:(WxH):"+width+"X"+height +"\n           "+height+"p");
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {

    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {

    }




//-------------------------------------------------------ANDROID LIFECYCLE---------------------------------------------------------------------------------------------

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop()...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()...");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e("그레브","222222222222222222");

        Log.v(TAG, "onResume()...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()...");
        player.release();


        final String bjnickname=shared.getString("bjnickname","");
        final String nickname=shared.getString("nickname","");
        chatclient.writeandflush("03:"+bjnickname+":"+nickname+":"+"퇴장");


        chatclient.setworking(false);


    }

    //-------------------------------------------------------채팅부분 !!!!!!!!!!!!!!---------------------------------------------------------------------------------------------



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

    //1. 현재 유저의 id를 전송한다.
    //2. 현재 유저에 대한 별풍선의 보유 정보를 가져와서 TextView Ui에 입력한다.

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
            //Toast.makeText(getApplicationContext(),result+"",Toast.LENGTH_LONG).show();
            Log.e("달풍선 갯수",result);

            moonCountInitUi(Integer.parseInt(result));

        }

        private void moonCountInitUi(int initmooncount)
        {
            String mooncountText=mooncountText="달풍선 "+initmooncount+"개";
            mooncount_text.setText(mooncountText);
        }
    }


    ////////////////////////////달풍선////////////////

    public class moonGiftTask extends AsyncTask<Void,Void,String>
    {
        String moonstarcount;

        @Override
        protected String doInBackground(Void... voids) {

            final String URL="http://"+global.getIP()+"/gift_moonstar.php";


            SharedPreferences shared=getSharedPreferences("logininfo",Activity.MODE_PRIVATE);
            String sendid=shared.getString("userid",null);


            Intent receivedIntent=getIntent();
            String receiveid=receivedIntent.getExtras().getString("publisherid");

            moonstarcount=mooncount_edit.getText().toString();


            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("sendid",sendid)
                    .add("receiveid",receiveid)
                    .add("moonstarcount",moonstarcount)
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

            if(result.equals("db update success"))
            {
                moonCountUi();
                // 이부분에서 netty로 메세지를 전송한다.

                String bjnickname=shared.getString("bjnickname","");
                String nickname=shared.getString("nickname","");
                chatclient.writeandflush("04:"+bjnickname+":"+nickname+":"+moonstarcount);

            }

        }

        private void moonCountUi()
        {


            String mooncountText=mooncount_text.getText().toString();

            final int index1=mooncountText.indexOf(" ")+1;
            final int index2=mooncountText.indexOf("개");

            int moondowncount=Integer.parseInt(mooncount_edit.getText().toString());

            String moonnumber=mooncountText.substring(index1,index2);
            moonnumber=""+(Integer.parseInt(moonnumber)-moondowncount);

            mooncountText="달풍선 "+moonnumber+"개";
            mooncount_text.setText(mooncountText);

        }

    }






    /////////////////////////////////네티부분 시작 //////////////////////////////////////////////////

    public class ChatClient {

        private final String host;
        private final int port;
        Channel channel;

        boolean working=true;

        public void setworking(boolean working)
        {
            this.working=working;
        }


        public ChatClient(String host,int port)
        {
            this.host=host;
            this.port=port;
        }


        //\n \r 정리
        //\n은 아스키코드 10번이며 \r은 아스키코드 13번이다.
        //DOS나 윈도우의 줄바꿈을 의미한다 =>/r/n
        //\r의 의미는 캐리지 리턴(Carriage Return)이며 타자기의 커서를 종이 앞쪽으로 놓는 것에서 유래되었다.
        //\n의 의미는 new line을 의미하며 새줄을 추가하고 커서를 밑으로 내리는 동작에서 유래되었다.

        public void writeandflush(String str)
        {
            channel.writeAndFlush(str+"\r\n");
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

        //함수 설명
        // 1. 네티서버로부터 값을 전달받는다.
        // 2. 현재 로컬에 저장되어있는 bjnickname과 nickname값을 네티에서 들어온 값과 비교한다.
        // 3. 서버에서 받은 값에 : 토큰이 없다면 "[알림]네티님께서 입장하셨습니다" 인 메세지를 db에 입력한다.
        // 4. 서버에서 받은 값에 : 토큰이 있다면


        @Override
        public void channelRead0(ChannelHandlerContext arg0, String message) throws Exception{

            System.out.println(message);
            Log.e("네티서버로부터 들어오는값",message);

            //String command=message.substring(0,2);
            //Log.e("네티서버로부터 온 커맨드",command);

            final String bjnickname=shared.getString("bjnickname","");
            final String nickname=shared.getString("nickname","");


            // 다음은 입력될 텍스트의 색상을 나타내는 Rgb 표이다.
            // 노랑 -> #FFE400 // 분홍 -> #FF00DD // 흰색 -> #FFFFFF // 초록 -> #47C83E

            if(!message.contains(":"))
            {
                sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','"+nickname+"','"+"[알림] "+message+"','#FFE400')");
                //notificationchat();
            }
            else
            {
                //첫번째 토튼은 닉네임이고 두번째 토큰은 텍스트 메세지이다.
                String tokens[]=message.split(":");

                //만약에 나한테 온 메세지라면 메세지를 입력하지 않는다.
                if(tokens[0].equals(nickname))
                {
                    Log.e("네티서버로부터 들어오는값",message);
                }
                //bj에게 온 메세지라면 노란색 메세지를 입력한다.

                else if(tokens[0].equals(bjnickname)) {

                    sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','"+tokens[0]+"','"+tokens[1]+"','#FFE400')");

                }

                //달풍선 선물이 온다면 초록색 메세지를 입력한다.

                else if(tokens[0].equals("달풍선"))
                {
                    //1. 서버에서 해당 형식으로 메세지가 도착한다. =>달풍선:네티:100개
                    //2. sqlite에 입력할 텍스트는 다음과 같은 형식이다. => 네티님께서 달풍선 100개를 선물하셨습니다.
                    //3. 애니메이션의 위젯들을 Visible로 바꾸고 5초뒤에 위젯들을 GONE으로 바꾼다.

                    String moonstartext="[알림]"+tokens[1]+"님께서 달풍선 "+tokens[2]+"개를 선물하셨습니다.";

                    sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','"+tokens[0]+"','"+moonstartext+"','#47C83E')");
                    moonGiftUiChange(moonstartext);

                }

                //추천이 온다면 주황색 메세지를 입력한다.

                else if(tokens[0].equals("추천"))
                    {
                    //1. 서버에서 해당 형식으로 메세지가 도착한다. => 추천:네티
                    String recommendtext="[추천]"+tokens[1]+"님께서 추천하셨습니다.";
                    sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','"+tokens[0]+"','"+recommendtext+"','#ED4C00')");

                    new recommendInitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }

                // 다른 유저에게 온 메세지라면 하얀색 메세지를 입력한다.
                else
                {
                    sqlite.execSQL("insert into message (bjnickname,nickname,text,color) values ( '"+bjnickname+"','"+tokens[0]+"','"+tokens[1]+"','#FFFFFF')");
                    //notificationchat();
                }

            }


            new Thread(new Runnable() {
                @Override
                public void run() {
                    h.post(new Runnable() {
                        @Override
                        public void run() {
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
                    h.post(new Runnable() {
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

                    ExoPlayer2Activity.this.runOnUiThread(new Runnable() {
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


    //맨 처음 액티비티가 시작될때 추천수를 서버에서 가져오는 함수이다.

    public class recommendInitTask extends AsyncTask<Void,Void,String>
    {
        @Override
        protected String doInBackground(Void... voids) {

            final String URL="http://"+global.getIP()+"/recommend_init.php";


            //리스트뷰를 클릭하여 방송 액티비티로 넘어올때 Intent를 통해서 받은 BJ의 아이디를 받아온다.
            Intent receivedIntent=getIntent();
            String bjid=receivedIntent.getExtras().getString("publisherid");



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
            //Toast.makeText(getApplicationContext(),result+"",Toast.LENGTH_LONG).show();
            Log.e("추천갯수:" , result);
            try {
                recommendInitUi(Integer.parseInt(result));
            }catch (Exception e)
            {
                Log.e("ExoPlayer2Activity",e.getMessage()+"");
            }
        }

        private void recommendInitUi(int initreccount)
        {
            final TextView recPresence_tv=(TextView)findViewById(R.id.recPresence_tv);
            String mooncountText=mooncountText=initreccount+"회";
            recPresence_tv.setText(mooncountText);
        }
    }


    //이 클래스는 추천 버튼을 눌렀을때 BJ의 추천수를 올려주는 비동기 작업이다.

    public class recommendLikeUpTask extends AsyncTask<Void,Void,String>
    {
        //이 함수의 작업순서
        //1. post_stringkey를 얻어서 okhttp3로 서버로 전송한다
        //2. 서버의 필요한 정보를 json값으로 받아서 onpostExecute에서 파싱한다.
        //3. 파싱을 한 뒤에 리스트뷰에 현재 아이템을 입력시키고 어뎁터를 새로 생신한다.

        @Override
        protected String doInBackground(Void... voids) {


            SharedPreferences shared=getSharedPreferences("logininfo",Activity.MODE_PRIVATE);
            String userid=shared.getString("userid",null);

            final String INSERT_COMMENT_URL="http://"+global.getIP()+"/likeup_comment_rec.php";


            //리스트뷰를 클릭하여 방송 액티비티로 넘어올때 Intent를 통해서 받은 BJ의 아이디를 받아온다.
            Intent receivedIntent=getIntent();
            String bjid=receivedIntent.getExtras().getString("publisherid");



            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("bjid",bjid)
                    .add("id",shared.getString("userid",null))
                    .build();

            Request request=new Request.Builder()
                    .url(INSERT_COMMENT_URL)
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
            Log.e("VideoPreviewActivity","recommendLike 작업 결과 : "+result);


            if(result.equals("추천 되었습니다."))
            {

                //04:bjnickname,nickname,text

                String bjnickname=shared.getString("bjnickname","");
                String nickname=shared.getString("nickname","");

                new recommendInitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                chatclient.writeandflush("05:"+bjnickname+":"+nickname+":"+"추천");
            }
        }
    }

}



