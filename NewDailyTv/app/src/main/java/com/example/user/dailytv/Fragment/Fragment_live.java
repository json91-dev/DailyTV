package com.example.user.dailytv.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ListView;

import com.example.user.dailytv.Activities.ExoPlayer2Activity;
import com.example.user.dailytv.Activities.TabviewActivity;
import com.example.user.dailytv.Adapter.Tv_adapter;
import com.example.user.dailytv.ListData.TV_ListData;
import com.example.user.dailytv.R;
import com.quickblox.sample.groupchatwebrtc.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by user on 2018-01-05.
 */

public class Fragment_live extends Fragment {

    View view;

    // IP를 얻기위한 전역변수를 설정한다.
    final App.GlobalVariable global=App.getGlobal();

    //TV 리스트뷰 변수를 선언한다.
    private Tv_adapter tv_adapter=null;
    ListView tv_listview;
    SwipeRefreshLayout swipe_tvlistview;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.f_live,null);

        InitTvListView();

        return view;
    }

    private void InitTvListView()
    {
        //////////////리스트뷰 처리하는 부분/////////////////////////


        swipe_tvlistview=(SwipeRefreshLayout)view.findViewById(R.id.swipe_tvlistview);
        swipe_tvlistview.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new tvlistchecktask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });


        tv_listview =(ListView)view.findViewById(R.id.tvlistview);
        tv_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SharedPreferences shared=getActivity().getSharedPreferences("logininfo", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor=shared.edit();
                editor.remove("bjnickname");
                editor.putString("bjnickname","펭귄");
                editor.commit();

                //exoplayer로 이동하는 인텐트
                Intent intent=new Intent(getActivity(),ExoPlayer2Activity.class);
                //startActivityForResult(intent,1001);

                Log.e("리스트뷰의 포지션값",position+"");

                TV_ListData item=(TV_ListData)tv_adapter.getItem(position);


                //Toast.makeText(getApplicationContext(),item.publisherid,Toast.LENGTH_LONG).show();


                intent.putExtra("publisherid",item.publisherid);
                startActivity(intent);


            }
        });

        //어뎁터 초기화

        tv_adapter=new Tv_adapter(getActivity());
        tv_listview.setAdapter(tv_adapter);


        new tvlistchecktask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


    }





    //컨펌 완료
    private class tvlistchecktask extends AsyncTask<Void,Void,String> {

        String TVLISTCHECKURL="http://"+global.getIP()+"/tvlistcheck.php";

        //사실 실제로 userid값은 필요가 없는 부분이다.
        String userid="user1";
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
