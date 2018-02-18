package com.example.user.dailytv.Activities;

/**
 * Created by user on 2017-11-30.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.user.dailytv.Adapter.Comment_adapter;
import com.example.user.dailytv.Adapter.Tv_adapter;
import com.example.user.dailytv.DialogActivity.CommentOptionActivity;
import com.example.user.dailytv.DialogActivity.CommentOptionEditActivity;
import com.example.user.dailytv.ListData.Comment_ListData;
import com.example.user.dailytv.Module.ProgressbarHelper;
import com.example.user.dailytv.R;
import com.quickblox.sample.groupchatwebrtc.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Comment;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


// 이 부분은 저장된 비디오 창을 누르게 되면 나타나게 되는 화면이다.
// 이 부분을 커스터마이징 해서 UI를 예쁘게 바꿔보도록 하겠다.
public class VideoPreviewActivity extends AppCompatActivity implements Button.OnClickListener {

    String SAMPLE_VIDEO_URL = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    VideoView videoView;
    SeekBar seekBar;
    Handler updateHandler = new Handler();

    //리스트뷰에 들어갈 헤더뷰,푸터뷰
    View content_header;
    View content_footer;


    Comment_adapter comment_adapter;
    //댓글목록의 edittext
    EditText commentEdit;


    //이전 액티비티에서 받아온 동영상의 longdate
    String video_longdate;
    App.GlobalVariable global = App.getGlobal();

    //댓글 수정작업에서 돌아왔을때 StartActivityForResult의 액션값
    final int FromCommentOptionEditActivity = 1001;
    final int FromCommentOptionActivity=1002;


    // 댓글목록을 표시하는 리스트뷰에 대한 변수이다.
    SwipeRefreshLayout swipeCommentListview;
    ListView comment_listview;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.videopreviewactivity);


        // 댓글창 리스트뷰 초기화(순서주의)

        initContentListView();

        // 액션바 UI 초기화

        initActionbar();

        // 뷰에들어갈 String변수들 및 각 뷰들의 인스턴스 초기화
        initUi();


        //내가 직접 만든(복사해서) 비디오뷰에 비디오를 로드시키는 함수이다.
        initLoadVideo();

        //댓글 입력작업 이벤트
        initCommentButton();

        InitComment();

        InitVideoPost();

    }


    public void initCommentButton() {

        Button commentButton;

        commentEdit = (EditText) content_footer.findViewById(R.id.comment_edittext);
        commentButton = (Button) content_footer.findViewById(R.id.comment_button);

        //버튼에 댓글 입력시 이벤트를 설정한다.
        commentButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.comment_button: {
                //이부분에서 해야할 작업
                /*
                    1. EditText의 값을 서버로 전송한다
                    2. 서버의 데이터베이스에 값을 입력한다.
                    3. 어뎁터를 갱신한뒤 리스트뷰를 갱신하고 포커스를 이동한다.
                */
                startCommentInputOperation(commentEdit.getText().toString());

                break;
            }


            case R.id.likeButton:{
                new videoLikeUpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    //이 함수는 댓글을 입력하는 EditText 의 비동기 작업을 실행하는 부분이다.
    public void startCommentInputOperation(String comment) {

        new commentInsertTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, comment);
        commentEdit.setText("");

    }

    //리스트뷰를 초기화하고 리스트뷰의 헤더와 푸터를 추가시키는 함수이다.
    public void initContentListView() {



        comment_listview = findViewById(R.id.contentListView);


        //리스트뷰에 Refresh 기능을 추가하기 위해 SwipeRefreshLayout에을 초기화하고 이벤트를 등록시킨다.
        swipeCommentListview=(SwipeRefreshLayout)findViewById(R.id.swipe_contentListView);
        swipeCommentListview.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                comment_adapter.removeAll();
                comment_adapter.notifyDataSetChanged();
                new Init_Comments(false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new Init_VideoPost().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });


        //어뎁터를 초기화하고 어뎁터에 아이템을 1개 집어넣는다
        comment_adapter = new Comment_adapter(getApplicationContext());

        comment_listview.setAdapter(comment_adapter);
        //xml로 저장된 header와 footer을 객체화 한 뒤에 리스트뷰에 추가시킨다.
        content_header = getLayoutInflater().inflate(R.layout.videopreviewactivity_header, null, false);
        comment_listview.addHeaderView(content_header);

        content_footer = getLayoutInflater().inflate(R.layout.videopreviewactivity_footer, null, false);
        comment_listview.addFooterView(content_footer);


        //리스트뷰 함수의 동작 절차
        // 1. 만약 클릭한 아이템의 userid값이 현재 로그인된 사용자라면 댓글 수정및 조회 창(CommentOptionEditActivity)로 이동한다.
        // 2. 만약 클릭한 아이템의 userid값이 현재 로그인된 사용자가 아니라면 좋아요 및 신고 창(CommentOptionActivity)로 이동한다.
        // 3. 이동할 화면에 position값을 intent로 전송하여 나중의 작업시에 사용하도록 한다.

        comment_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SharedPreferences shared = getSharedPreferences("logininfo", Activity.MODE_PRIVATE);
                Comment_ListData item = (Comment_ListData) comment_adapter.getItem(position-1);
                String localid = shared.getString("userid", null);
                if (item.userid.equals(localid)) {
                    Intent intent = new Intent(VideoPreviewActivity.this, CommentOptionEditActivity.class);
                    /*
                        여기서 position은 Listview아이템의 position을 의미한다.
                    */
                    intent.putExtra("position",position);
                    intent.putExtra("origintext",item.commentText);
                    startActivityForResult(intent,1001);

                } else {

                    Intent intent = new Intent(VideoPreviewActivity.this, CommentOptionActivity.class);
                    intent.putExtra("position",position);

                    startActivityForResult(intent,1002);

                }

            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case FromCommentOptionEditActivity : {


                //CommentOptionEditActivity로 붙어 전달받은 옵션값을 기준으로
                //각각의 해당된 함수를 실행시킨다.

                String option=data.getExtras().getString("option");

                if(option.equals("edit"))
                {

                    int position=data.getExtras().getInt("position");
                    String text=data.getExtras().getString("text");

                    edit_comment(position,text);

                }else if (option.equals("delete"))
                {
                    int position=data.getExtras().getInt("position");
                    delete_comment(position);
                }

            }

            case FromCommentOptionActivity :{

                String option=data.getExtras().getString("option");

                if(option.equals("like"))
                {
                    int position=data.getExtras().getInt("position");
                    SharedPreferences shared=getSharedPreferences("logininfo",Activity.MODE_PRIVATE);
                    String userid=shared.getString("userid",null);

                    like_comment(position,userid);
                }
                else if(option.equals("report"))
                {
                    Toast.makeText(getApplicationContext(),"댓글 신고가 접수되었습니다.",Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    public void like_comment(int position,String id)
    {
        // 이 함수 작업 내용
        // 2. 백그라운드로 서버에 해당 댓글의 좋아요를 요청한다(comment_number)필요
        // 3. 만약 서버에서 중복체크에 안걸리고 업데이트가 되었다면 어뎁터에서 해당 position에 있는 댓글의 좋아요를 1올린다.
        // 4. 만약 서버에서 중복체크에 걸렸다면 어뎁터에서 좋아요를 올리지 않는다.

        //작업 1
        Comment_ListData item=(Comment_ListData) comment_adapter.getItem(position-1);
        String post_id=item.commentNumber;

        //좋아요를 백그라운드 작업시키기 위해서는 post_id값과 id값이 필요하다.

        new commentLikeUpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,post_id,id,position+"");

    }

    public void delete_comment(int position)
    {
        //이 함수 작업 내용
        //1. 백그라운드로 서버에 해당 댓글의 삭제를 요청한다(comment_number)필요
        //2. 어뎁터에서 해당 position에 있는 댓글을 삭제한다.
        //3. 수정된 결과를 받는다.

        Comment_ListData item=(Comment_ListData) comment_adapter.getItem(position-1);
        String comment_number=item.commentNumber;
        new commentDeleteTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,comment_number);

        comment_adapter.remove(position-1);
        comment_adapter.notifyDataSetChanged();


    }

    public void edit_comment(int position,String alteredText)
    {
        //이 함수 작업 내용
        //1. 어뎁터에서 해당 position에 있는 댓글을 바뀐 텍스트로 수정한다.
        //2. 백그라운드로 서버에 해당 댓글의 수정을 요청한다.
        //3. 수정된 결과를 받는다.

        //작업 1
        Comment_ListData item=(Comment_ListData) comment_adapter.getItem(position-1);
        item.commentText=alteredText;
        comment_adapter.notifyDataSetChanged();

        String comment_number=item.commentNumber;


        //작업 2

        new commentUpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,alteredText,comment_number);

    }

    public void initActionbar()
    {
        ActionBar actionbar=getSupportActionBar();

        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeButtonEnabled(true);

        actionbar.setTitle("뒤로가기");

    }

    public void initUi()
    {
        EditText tvURL = (EditText)content_header.findViewById(R.id.etVieoURL);

        //이 부분은 이전 액티비티에서 받아온 비디오 url 을 변수에 설정하는 부분이다.
        Intent intent=getIntent();
        String videourl=intent.getExtras().getString("videourl");
        SAMPLE_VIDEO_URL=videourl;
        tvURL.setText(SAMPLE_VIDEO_URL);


        // 이전 액티비티에서 받은 제목 데이터와 날짜 데이터도 각각 텍스트뷰에 설정한다.
        String video_title=intent.getExtras().getString("title");
        String video_date=intent.getExtras().getString("date");
        video_longdate=intent.getExtras().getString("longdate");

        Log.e("VideoPreViewActivity","TabViewActivity로부터 전달받은 longdate"+video_longdate);


        TextView title=(TextView)content_header.findViewById(R.id.title);
        title.setText(video_title);

        TextView date=(TextView)content_header.findViewById(R.id.date);
        date.setText(video_date);


        //비디오 뷰와 Seekbar도 각각 초기화한다.

        videoView = (VideoView)content_header.findViewById(R.id.videoView);
        //MediaController mc = new MediaController(this);
        //videoView.setMediaController(mc);

        seekBar = (SeekBar)content_header.findViewById(R.id.seekBar);

        //비디오 게시물에대한 좋아요 및 댓글 수를 표시하기 위한 위젯들 초기화



    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id==android.R.id.home)
        {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }






    public void loadVideo(View view) {
        //Sample video URL : http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_2mb.mp4
        EditText tvURL = (EditText) content_header.findViewById(R.id.etVieoURL);
        String url = tvURL.getText().toString();


        //Toast.makeText(getApplicationContext(), "Loading Video. Plz wait", Toast.LENGTH_LONG).show();
        videoView.setVideoURI(Uri.parse(url));
        videoView.requestFocus();

        // 토스트 다이얼로그를 이용하여 버퍼링중임을 알린다.
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {

                                        @Override
                                        public boolean onInfo(MediaPlayer mp, int what, int extra) {
                                            switch(what){
                                                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                                                    // Progress Diaglog 출력
                                                    //Toast.makeText(getApplicationContext(), "Buffering", Toast.LENGTH_LONG).show();
                                                    break;
                                                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                                                    // Progress Dialog 삭제
                                                    //Toast.makeText(getApplicationContext(), "Buffering finished.\nResume playing", Toast.LENGTH_LONG).show();
                                                    videoView.start();
                                                    break;
                                            }
                                            return false;
                                        }
                                    }

        );

        // 플레이 준비가 되면, seekBar와 PlayTime을 세팅하고 플레이를 한다.
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
                long finalTime = videoView.getDuration();
                TextView tvTotalTime = (TextView) content_header.findViewById(R.id.tvTotalTime);
                tvTotalTime.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
                );
                seekBar.setMax((int) finalTime);
                seekBar.setProgress(0);
                updateHandler.postDelayed(updateVideoTime, 100);
                //Toast Box
                //Toast.makeText(getApplicationContext(), "Playing Video", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void playVideo(View view){
        videoView.requestFocus();
        videoView.start();

    }

    public void pauseVideo(View view){
        videoView.pause();
    }

    // seekBar를 이동시키기 위한 쓰레드 객체
    // 100ms 마다 viewView의 플레이 상태를 체크하여, seekBar를 업데이트 한다.
    private Runnable updateVideoTime = new Runnable(){
        public void run(){
            long currentPosition = videoView.getCurrentPosition();
            seekBar.setProgress((int) currentPosition);
            updateHandler.postDelayed(this, 100);

        }
    };




    //비디오 재생하는함수

    public void initLoadVideo()
    {



        //비디오 위치의 URL을 설정한다.
        EditText tvURL = (EditText) content_header.findViewById(R.id.etVieoURL);
        String url = tvURL.getText().toString();

        //프로그래스 다이얼로그헬퍼를 설정한다.
        final ProgressbarHelper progresshelper=  new ProgressbarHelper(this);

        //progresshelper.showProgressDialog("Video Loading....");
        //Toast.makeText(getApplicationContext(), "비디오 로딩중입니다..", Toast.LENGTH_LONG).show();

        //비디오뷰의 URI를 설정한다.

        videoView.setVideoURI(Uri.parse(url));
        // 비디오뷰로 포커싱한다.
        videoView.requestFocus();

        // 토스트 다이얼로그를 이용하여 버퍼링중임을 알린다.
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {


                                        //플레이 도중 버퍼링이 될때 시작되고 해제되는 메서드들이다.
                                        @Override
                                        public boolean onInfo(MediaPlayer mp, int what, int extra) {
                                            switch(what){
                                                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                                                    // Progress Diaglog 출력
                                                    //Toast.makeText(getApplicationContext(), "Buffering..", Toast.LENGTH_LONG).show();
                                                    //progresshelper.showProgressDialog("Buffering....");

                                                    break;
                                                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                                                    // Progress Dialog 삭제
                                                    //Toast.makeText(getApplicationContext(), "Buffering finished.\nResume playing", Toast.LENGTH_LONG).show();
                                                    //progresshelper.hideProgressDialog();

                                                    videoView.start();
                                                    break;
                                            }
                                            return false;
                                        }
                                    }

        );

        // 플레이 준비가 되면, seekBar와 PlayTime을 세팅하고 플레이를 한다.
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();

                //long형으로 최종 시간을 받아온다
                long finalTime = videoView.getDuration();

                TextView tvTotalTime = (TextView) content_header.findViewById(R.id.tvTotalTime);
                tvTotalTime.setText(String.format("%d:%d",
                        //분단위의 시간을 받아온다.
                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        //초단위의 시간을 받아온다.
                        //전체 초시간 - 분단위 초시간
                        //만약 2분 10초라면 130 -120
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
                );
                seekBar.setMax((int) finalTime);
                seekBar.setProgress(0);
                updateHandler.postDelayed(updateVideoTime, 100);
                //Toast Box
                //progresshelper.hideProgressDialog();
                //Toast.makeText(getApplicationContext(), "Playing Video", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void InitComment()
    {
        new Init_Comments(false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void InitVideoPost()
    {
        new Init_VideoPost().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Button likebutton=(Button)content_header.findViewById(R.id.likeButton);
        likebutton.setOnClickListener(this);
    }



    public class videoLikeUpTask extends AsyncTask<Void,Void,String>
    {
        //이 함수의 작업순서
        //1. post_stringkey를 얻어서 okhttp3로 서버로 전송한다
        //2. 서버의 필요한 정보를 json값으로 받아서 onpostExecute에서 파싱한다.
        //3. 파싱을 한 뒤에 리스트뷰에 현재 아이템을 입력시키고 어뎁터를 새로 생신한다.

        @Override
        protected String doInBackground(Void... voids) {


            SharedPreferences shared=getSharedPreferences("logininfo",Activity.MODE_PRIVATE);
            String userid=shared.getString("userid",null);

            final String INSERT_COMMENT_URL="http://"+global.getIP()+"/likeup_comment_video.php";

            Intent receivedIntent=getIntent();
            String v_userid=receivedIntent.getExtras().getString("v_userid");



            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("id",userid)
                    .add("post_stringkey",v_userid+"_"+video_longdate)
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
            Log.e("VideoPreviewActivity","commentLike 작업 결과 : "+result);

            if(result.equals("게시글에 하트 성공~"))
            {
                likeupUi();
            }

        }

        private void likeupUi()
        {
            //파싱순서 : (텍스트의 파싱을 진행한다.) =>현재 문자열은 하트 0개, 하트 11개 등으로 구성된다.
            //1. 공백에서 index+1를 구한다.
            //2. 마지막 글자의 index -1값을 구한다
            //3. index1과 index2사이의 숫자를 추출한다.
            //4. 숫자를 int로 바꾸고 1을 더한다.
            //5. 문자를 재배열한다. => 하트 + 띄어쓰기 + 더해진 숫자 + "개"
            //6. 재배열한 문자를 text뷰에 넣는다.
            //* substring은 첫번째 인덱스부터 마지막인덱스 -1까지 추출한다.


            TextView likecount=(TextView)content_header.findViewById(R.id.likecount);
            String textLike=likecount.getText().toString();

            final int index1=textLike.indexOf(" ")+1;
            final int index2=textLike.indexOf("개");

            String likenumber=textLike.substring(index1,index2);
            likenumber=""+(Integer.parseInt(likenumber)+1);

            textLike="하트 "+likenumber+"개";
            likecount.setText(textLike);
        }
    }


    //이 메서드는 댓글 목록을 최신화하는 백그라운드 작업을 수행하는 함수이다
    // 1. 서버에
    public class Init_Comments extends AsyncTask<Void,Void,String>
    {
        //이 함수의 작업순서
        //1. post_stringkey를 얻어서 okhttp3로 서버로 전송한다
        //2. 서버의 필요한 정보를 json값으로 받아서 onpostExecute에서 파싱한다.
        //3. 파싱을 한 뒤에 리스트뷰에 현재 아이템을 입력시키고 어뎁터를 새로 생신한다.
        boolean isEdit=false;


        // 만약 새로운 댓글을 불러오는 작업이 액티비티가 시작될때가 아니라
        // 댓글변경 후에 불러올때 리스트뷰의 보여지는 포니션을 아래쪽으로 설정하기 위한 플레그를 생성자에서 받는다.
        public Init_Comments(boolean isEdit)
        {
            this.isEdit=isEdit;

        }

        @Override
        protected String doInBackground(Void... voids) {


            SharedPreferences shared=getSharedPreferences("logininfo",Activity.MODE_PRIVATE);
            String userid=shared.getString("userid",null);

            final String INSERT_COMMENT_URL="http://"+global.getIP()+"/select_comment.php";

            Intent receivedIntent=getIntent();
            String v_userid=receivedIntent.getExtras().getString("v_userid");


            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("post_stringkey",v_userid+"_"+video_longdate)
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
                Log.e("VideoPreviewActivity","댓글 조회 query오류"+e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            Log.e("VideoPreviewActivity","댓글목록 조회 결과"+result);
            //Toast.makeText(getApplicationContext(),result+"",Toast.LENGTH_LONG).show();

            try {
                final JSONObject jsonobj = new JSONObject(result);
                final JSONArray videolistarray = jsonobj.getJSONArray("result");


                for(int i=0;i<videolistarray.length();i++)
                {
                    String roomtitle,longdate;
                    String videourl,videoimageurl;

                    final String commentnumber,imageurl,date,like,commentText,userid,nickname;

                    JSONObject c_json=videolistarray.getJSONObject(i);

                    commentnumber=c_json.getString("commentnumber");
                    imageurl=c_json.getString("imageurl");
                    date=c_json.getString("date");
                    like=c_json.getString("like");
                    commentText=c_json.getString("commentText");
                    userid=c_json.getString("userid");
                    nickname=c_json.getString("nickname");

                    final String circleimageurl="http://"+global.getIP()+"/"+imageurl;

                    comment_adapter.addItem(commentnumber,circleimageurl,nickname,date,like,commentText,userid);

                }

            }catch (JSONException e)
            {
                Log.e("[VideoPreviewActivity]","json 에러"+e.getMessage());
            }

            comment_adapter.notifyDataSetChanged();

            if(isEdit)
            {
                comment_listview.setSelection(comment_adapter.getCount()-1);
            }

            // refreshing을 종료한다.
            swipeCommentListview.setRefreshing(false);
        }
    }




    //이 메서드는 댓글 댓글 상단의 비디오 게시물에 대한 정보를 함수이다
    // 1. 서버에
    public class Init_VideoPost extends AsyncTask<Void,Void,String>
    {

        @Override
        protected String doInBackground(Void... voids) {
            final String INSERT_COMMENT_URL="http://"+global.getIP()+"/select_videopost.php";

            Intent receivedIntent=getIntent();
            String v_userid=receivedIntent.getExtras().getString("v_userid");


            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("v_userid",v_userid)
                    .add("post_stringkey",v_userid+"_"+video_longdate)
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
                Log.e("VideoPreviewActivity","댓글 조회 query오류"+e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            Log.e("VideoPreviewActivity","비디오 정보 조회 결과"+result);

            try {
                final JSONObject jsonobj = new JSONObject(result);
                final JSONArray videolistarray = jsonobj.getJSONArray("result");


                for(int i=0;i<videolistarray.length();i++)
                {
                    String imagepath,nickname,commentcount,likecount;


                    JSONObject c_json=videolistarray.getJSONObject(i);


                    imagepath=c_json.getString("imagepath");
                    nickname=c_json.getString("nickname");
                    commentcount=c_json.getString("commentcount");
                    likecount=c_json.getString("likecount");

                    final String imageurl="http://"+global.getIP()+"/"+imagepath;
                    initVideoPost(imageurl,nickname,commentcount,likecount);

                }

            }catch (JSONException e)
            {
                Log.e("[Init_Videopost]","json 에러"+e.getMessage());
            }

        }

        private void initVideoPost(String imageurl,String nickname,String commentcount,String likecount)
        {
            CircleImageView publishercircle=(CircleImageView)content_header.findViewById(R.id.publisherCircle);
            try {
                Glide.with(getApplicationContext()).load(new URL(imageurl)).thumbnail(0.1f).into(publishercircle);
            }catch (Exception e)
            {
                Log.e("URL error",e.getMessage());
            }

            TextView nickname_=(TextView)content_header.findViewById(R.id.nickname);
            nickname_.setText(nickname);

            TextView likecount_=(TextView)content_header.findViewById(R.id.likecount);
            final String liketag="하트 "+likecount+"개";
            likecount_.setText(liketag);

            TextView commentnumber=(TextView)content_header.findViewById(R.id.commentNumber);
            final String commenttag="댓글 "+commentcount+"개";
            commentnumber.setText(commenttag);

        }
    }


    //이 AsyncTask는 댓글을 입력했을때 서버의 postgresql에 데이터가 입력되게 해주는 메서드이다.
    //1. Okhttp3 를 사용하여 post 메세지를 보낸다.
    //2. 서버의 php를 통해 정보가 insert된다.
    //3. 성공했다는 메세지를 보낸다.

    public class commentInsertTask extends AsyncTask<String,Void,String>
    {
        String comment;
        Handler h;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            h=new Handler(Looper.getMainLooper());
        }

        @Override
        protected String doInBackground(String... str) {

            //str[0]은 댓글이다.
            /*

                댓글의 방 목록은 이전 액티비티에서 받은 유저아이디+시스템시간함수 를 키값으로한다
                댓글의 정보는 댓글의 입력시간과, 유저 아이디정보, 댓글의 text값만을 필요로 한다.

                post에
                post_stringkey => 이전 액티비티로부터 넘겨받은 (user1+시스템시간함수)
                유저아이디,      => 현재 로컬에 저장된 SharedPreperence 유저아이디
                시스템 시간함수, => 진짜 System 시간함수
                imageurl         =>  userid_시스템시간함수.png (필요없을듯)
                 댓글
                좋아요수 등을 입력한다.
            */

            SharedPreferences shared=getSharedPreferences("logininfo",Activity.MODE_PRIVATE);
            String userid=shared.getString("userid",null);

            final String INSERT_COMMENT_URL="http://"+global.getIP()+"/insert_comment.php";
            Intent receivedIntent=getIntent();
            String v_userid=receivedIntent.getExtras().getString("v_userid");

            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("publisher_id",userid)
                    .add("post_stringkey",v_userid+"_"+video_longdate)
                    .add("long_stringtime",System.currentTimeMillis()+"")
                    .add("comment_text",str[0])
                    .build();

            Request request=new Request.Builder()
                    .url(INSERT_COMMENT_URL)
                    .header("Content-Type","text/html")
                    .post(body)
                    .build();


            try {

                Response response=client.newCall(request).execute();
                return response.body().string();

                //client.newCall(request).enqueue(callback);
            }catch (Exception e)
            {
                Log.e("VideoPreviewActivity","댓글 입력 query오류"+e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            Toast.makeText(getApplicationContext(),result+"",Toast.LENGTH_LONG).show();
            Log.e("VideoPreviewActivity","comment isert작업 결과 : "+result);

            // 이 이후에 어뎁터를 변경시키는 작업을 해야한다.
            // 원래 프로세스데로 어뎁터를 변경시키고 값을 서버로 보내는 것이 아니라,
            // 입력이 된 뒤에 서버에서 댓글들의 값을 모두 다시 가져오는 방법으로 구현하여야한다.

            if(result.equals("success"))
            {
                comment_adapter.removeAll();
                new Init_Comments(true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                commentCountUpUi();
            }
        }

        //Ui에서 댓글갯수를 1개 증가시키는 함수
        private void commentCountUpUi()
        {


            TextView commentCount=(TextView)content_header.findViewById(R.id.commentNumber);
            String textComment=commentCount.getText().toString();

            final int index1=textComment.indexOf(" ")+1;
            final int index2=textComment.indexOf("개");

            String commentnumber=textComment.substring(index1,index2);
            commentnumber=""+(Integer.parseInt(commentnumber)+1);

            textComment="댓글 "+commentnumber+"개";
            commentCount.setText(textComment);
        }
    }



    // 클래스는 설명
    // 1. 이 클래스는 서버로 comment_id와 commentText값을 전송한 뒤
    // 2. 서버에서 DB업데이트가 완료되면 확인을 해주는 작업을 백그라운드로 실행하는 클래스이다.

    public class commentUpdateTask extends AsyncTask<String,Void,String>
    {
        String comment;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... string) {


            SharedPreferences shared=getSharedPreferences("logininfo",Activity.MODE_PRIVATE);
            String userid=shared.getString("userid",null);

            final String INSERT_COMMENT_URL="http://"+global.getIP()+"/edit_comment.php";

            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("comment_text",string[0])
                    .add("comment_number",string[1])
                    .build();

            Request request=new Request.Builder()
                    .url(INSERT_COMMENT_URL)
                    .header("Content-Type","text/html")
                    .post(body)
                    .build();


            try {

                Response response=client.newCall(request).execute();
                return response.body().string();

                //client.newCall(request).enqueue(callback);
            }catch (Exception e)
            {
                Log.e("VideoPreviewActivity","댓글 입력 query오류"+e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            Toast.makeText(getApplicationContext(),result+"",Toast.LENGTH_LONG).show();
            Log.e("VideoPreviewActivity","comment update작업 결과 : "+result);

            Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();

        }
    }

    ////////////////////////////////////댓글 삭제 부분///////////////////////////////////



    public class commentDeleteTask extends AsyncTask<String,Void,String>
    {
        String comment;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... string) {


            SharedPreferences shared=getSharedPreferences("logininfo",Activity.MODE_PRIVATE);
            String userid=shared.getString("userid",null);

            final String INSERT_COMMENT_URL="http://"+global.getIP()+"/delete_comment.php";

            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                     .add("comment_number",string[0])
                     .build();

            Request request=new Request.Builder()
                    .url(INSERT_COMMENT_URL)
                    .header("Content-Type","text/html")
                    .post(body)
                    .build();


            try {

                Response response=client.newCall(request).execute();
                return response.body().string();

                //client.newCall(request).enqueue(callback);
            }catch (Exception e)
            {
                Log.e("VideoPreviewActivity","댓글 입력 query오류"+e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            Toast.makeText(getApplicationContext(),result+"",Toast.LENGTH_LONG).show();
            Log.e("VideoPreviewActivity","comment update작업 결과 : "+result);
            Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();

            if(result.equals("Db delete Success")) {
                commentCountDownUi();
            }

        }


        //Ui에서 댓글갯수를 1개 감소시키는 함수
        private void commentCountDownUi()
        {


            TextView commentCount=(TextView)content_header.findViewById(R.id.commentNumber);
            String textComment=commentCount.getText().toString();

            final int index1=textComment.indexOf(" ")+1;
            final int index2=textComment.indexOf("개");

            String commentnumber=textComment.substring(index1,index2);
            commentnumber=""+(Integer.parseInt(commentnumber)-1);

            textComment="댓글 "+commentnumber+"개";
            commentCount.setText(textComment);
        }
    }


    public class commentLikeUpTask extends AsyncTask<String,Void,String>
    {

        int position;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... string) {

            position=Integer.parseInt(string[2]);


            SharedPreferences shared=getSharedPreferences("logininfo",Activity.MODE_PRIVATE);
            String userid=shared.getString("userid",null);

            final String INSERT_COMMENT_URL="http://"+global.getIP()+"/likeup_comment.php";

            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("post_id",string[0])
                    .add("id",string[1])
                    .build();

            Request request=new Request.Builder()
                    .url(INSERT_COMMENT_URL)
                    .header("Content-Type","text/html")
                    .post(body)
                    .build();


            try {

                Response response=client.newCall(request).execute();
                return response.body().string();

                //client.newCall(request).enqueue(callback);
            }catch (Exception e)
            {
                Log.e("VideoPreviewActivity","댓글 입력 query오류"+e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            Toast.makeText(getApplicationContext(),result+"",Toast.LENGTH_LONG).show();
            Log.e("VideoPreviewActivity","commentLike 작업 결과 : "+result);

            if(result.equals("댓글에 하트 성공~"))
            {
                Comment_ListData item=(Comment_ListData)comment_adapter.getItem(position-1);
                int like=Integer.parseInt(item.like);
                like=like+1;
                item.like=like+"";
                comment_adapter.notifyDataSetChanged();

            }

            Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();

        }
    }


}
