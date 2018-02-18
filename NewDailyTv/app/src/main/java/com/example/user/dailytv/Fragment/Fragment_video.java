package com.example.user.dailytv.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.user.dailytv.Activities.ExoPlayerCusterActivity;
import com.example.user.dailytv.Activities.VideoPreviewActivity;
import com.example.user.dailytv.Adapter.GridAdapter;
import com.example.user.dailytv.ListData.Video_ListData;
import com.example.user.dailytv.R;
import com.quickblox.sample.groupchatwebrtc.App;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageClickListener;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by user on 2018-01-05.
 */

public class Fragment_video extends Fragment{


    View view;
    View headerView;

    //그리드뷰 설정부분
    GridViewWithHeaderAndFooter video_gridview;
    GridAdapter video_gridadapter;

    //리사이클러뷰를 설정한다.
    SwipeRefreshLayout videoSwipeLayout;

    //글로벌 IP함수
    final App.GlobalVariable global=App.getGlobal();




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.f_video,null);
        headerView=inflater.inflate(R.layout.f_video_header,null);

        //그리드뷰를 초기화하고 헤더를 설정한다.
        initVideoGridView();

        InitCarouselView();



        InitScrollVideo();

        return view;
    }

    private void InitScrollVideo()
    {
        HorizontalScrollView videoScrollView=(HorizontalScrollView)headerView.findViewById(R.id.videoScrollView);



        // exoplyaer엑티비티로 넘어가는 이벤트를 등록한다.

        final View.OnClickListener mClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.v1_video)
                {
                    Toast.makeText(getActivity(),"첫번쨰",Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(getActivity(),ExoPlayerCusterActivity.class);
                    intent.putExtra("videourl","http://"+global.getIP()+"/scrollvideo/v1.mp4");
                    startActivity(intent);

                }else if(v.getId()==R.id.v2_video)
                {
                    Toast.makeText(getActivity(),"두번쨰",Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(getActivity(),ExoPlayerCusterActivity.class);
                    intent.putExtra("videourl","http://"+global.getIP()+"/scrollvideo/v2.mp4");
                    startActivity(intent);
                }else if(v.getId()==R.id.v3_video)
                {
                    Toast.makeText(getActivity(),"세번째",Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(getActivity(),ExoPlayerCusterActivity.class);
                    intent.putExtra("videourl","http://"+global.getIP()+"/scrollvideo/v3.mp4");
                    startActivity(intent);
                }else if(v.getId()==R.id.v4_video)
                {
                    Toast.makeText(getActivity(),"네번째",Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(getActivity(),ExoPlayerCusterActivity.class);
                    intent.putExtra("videourl","http://"+global.getIP()+"/scrollvideo/v4.mp4");
                    startActivity(intent);
                }
            }
        };

        //1. 각 scrollview안에 있는 이미지에 대한 클릭 이벤트를 등록한다.
        //2. 각각의 이미지를 클릭하게 되면 동영상의 url을 재생하는 exoplayeractivity로 넘어가게 된다.

        ImageButton v1_video=(ImageButton)headerView.findViewById(R.id.v1_video);
        ImageButton v2_video=(ImageButton)headerView.findViewById(R.id.v2_video);
        ImageButton v3_video=(ImageButton)headerView.findViewById(R.id.v3_video);
        ImageButton v4_video=(ImageButton)headerView.findViewById(R.id.v4_video);
        v1_video.setOnClickListener(mClickListener);
        v2_video.setOnClickListener(mClickListener);
        v3_video.setOnClickListener(mClickListener);
        v4_video.setOnClickListener(mClickListener);


    }


    // 캐러셀 뷰에대한 초기화 작업을 수행한다.
    private void InitCarouselView()
    {
        //캐러샐 뷰
        CarouselView carouselView;
        final int[] sampleImages = {R.drawable.image_11, R.drawable.image_2, R.drawable.image_3, R.drawable.image_4};
        final Bitmap[] sampleimagebitmap=new Bitmap[4];

        carouselView = (CarouselView) headerView.findViewById(R.id.carouselView);
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
                Toast.makeText(getActivity(), "Clicked item: "+ position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void initVideoGridView()
    {
        /////////////////////비디오 그리드뷰 처리하는 부분/////////////////////////////////


        video_gridview=(GridViewWithHeaderAndFooter)view.findViewById(R.id.videogridview);

        //그리드뷰의 헤더를 설정한다.
        video_gridview.addHeaderView(headerView);

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


        videoSwipeLayout=view.findViewById(R.id.swipe_videogridview);
        videoSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new videolistchecktask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });


        new videolistchecktask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public class videolistchecktask extends AsyncTask  <Void,Void,String> {

        //String password -> 만약에 비밀방 설정할떄 .. 이부분은 나중에 추가

        String userid = "user1"; //userid는 user1으로 통일
        String result;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            RequestBody body = new FormBody.Builder()
                    .add("userid", userid)
                    .build();

            Request request = new Request.Builder()
                    .url("http://" + global.getIP() + "/videofilecheck.php")
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();

            } catch (Exception e) {
                Log.e("TabviewActivity", "video file query exception" + e.getMessage());
            }
            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            Log.e("videolistcheck", " :결과값 출력" + result);

            //만약 조회했는데 방이 없다면
            if (result.equals("0")) {
                //tv_adapter.remove(0);
                //tv_listview.setAdapter(tv_adapter);
                Log.e("videolistcheck", " :방 목록 조회 실패" + result);

            } else {//if (tv_adapter.getCount()==0) { //방이 존재한다면

                Log.e("videolistcheck", " :방 목록 조회 성공" + result);
                //방목록 조회성공시 리스트뷰에 값입력

                try {
                    final JSONObject jsonobj = new JSONObject(result);
                    final JSONArray videolistarray = jsonobj.getJSONArray("result");

                    //리스트 모두 삭제
                    video_gridadapter.removeAll();

                    for (int i = 0; i < videolistarray.length(); i++) {
                        final String roomtitle, longdate;
                        String videourl, videoimageurl, v_userid, nickname, profilepath;

                        final JSONObject videolistjson = videolistarray.getJSONObject(i);

                        //새로 추가된 값들
                        v_userid = videolistjson.getString("v_userid");
                        nickname = videolistjson.getString("nickname");
                        profilepath = videolistjson.getString("profilepath");

                        ////////
                        roomtitle = videolistjson.getString("roomtitle");
                        longdate = videolistjson.getString("longdate");
                        videourl = videolistjson.getString("videourl");
                        videoimageurl = videolistjson.getString("videoimageurl");


                        //Log.e("videolistcheck","이미지 url "+"http://"+global.getIP()+"/videos/"+videourl);
                        //Log.e("videolistcheck","이미지 url "+"http://"+global.getIP()+"/videos/"+videoimageurl);

                        videourl = "http://" + global.getIP() + "/videos/" + videourl;
                        videoimageurl = "http://" + global.getIP() + "/videos/" + videoimageurl;
                        profilepath = "http://" + global.getIP() + "/" + profilepath;


                        video_gridadapter.addItem(roomtitle, longdate, videourl, videoimageurl, nickname, profilepath, v_userid);

                    }

                    //video_gridview.setAdapter(video_gridadapter);
                    video_gridadapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    Log.e("[Tabviewactivity]JSON에러", e.getMessage());
                }

            }

            videoSwipeLayout.setRefreshing(false);

            //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();

        }
    }

}
