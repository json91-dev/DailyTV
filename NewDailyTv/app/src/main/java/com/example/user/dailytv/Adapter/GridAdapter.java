package com.example.user.dailytv.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.user.dailytv.ListData.Video_ListData;
import com.example.user.dailytv.R;

import org.w3c.dom.Text;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by user on 2017-12-18.
 */


//TabViewActivity에서 내가 방송한 동영상란 밑에 있는 그리드뷰를 나타낼때 사용되는 어뎁터이다.
public class GridAdapter extends BaseAdapter
{
    private Context context=null;
    private ArrayList <Video_ListData> arraylist=new ArrayList<>();

    //뷰홀더 설정
    private static class ViewHolder2 {

        public ImageView videoimage;
        public TextView roomtitle;
        public TextView date;
        public CircleImageView pub_circle;
        public TextView pub_nickname;
        public ImageButton xbutton;

    }


    public GridAdapter(Context context)
    {
        super();
        this.context=context;
    }



    @Override
    public int getCount() {
        return arraylist.size();
    }

    @Override
    public Object getItem(int i) {
        return arraylist.get(i);
    }



    @Override
    public long getItemId(int i) {
        return i;
    }



    public void addItem(String roomtitle,String date,String videourl,String videoimageurl,String nickname,String profilepath,String v_userid)
    {
        Video_ListData item=null;
        item=new Video_ListData();
        item.roomtitle=roomtitle;
        item.date=date;
        item.videoimageurl=videoimageurl;
        item.videourl=videourl;

        item.nickname=nickname;
        item.profilepath=profilepath;
        item.v_userid=v_userid;


        arraylist.add(item);
    }

    public void remove(int postion)
    {

        try {
            arraylist.remove(postion);
        }catch (IndexOutOfBoundsException e)
        {
            e.printStackTrace();
            Log.e("[자바 인덱스 에러]"," --> Remove 자바 인덱스 에러 발생 : "+e.getMessage());
        }

        dataChange();
    }

    public void removeAll()
    {
        arraylist=null;
        arraylist=new ArrayList<>();
        //dataChange();
    }

    public void Sort(){
        //Collection.sort(arraylist,TV_ListData.aasdasd_Comparator)
        dataChange();

    }

    public void dataChange(){
        //tv_adapter.notifyDataSetChanged();
    }




    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final ViewHolder2 holder;

        //뷰에 아무것도 없으면
        if(view==null)
        {
            //홀더 초기화를 한다.
            holder=new ViewHolder2();

            //리스트뷰 레이아웃을 객체화(실제 메모리에 로드)
            LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view=inflater.inflate(R.layout.videogridview_item,null);

            holder.videoimage=(ImageView)view.findViewById(R.id.videoimage);
            holder.roomtitle=(TextView)view.findViewById(R.id.roomtitle);
            holder.date=(TextView)view.findViewById(R.id.date);

            holder.pub_circle=(CircleImageView)view.findViewById(R.id.publisherimage);
            holder.pub_nickname=(TextView)view.findViewById(R.id.nickname);

            holder.xbutton=(ImageButton)view.findViewById(R.id.xbutton);
            holder.xbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context.getApplicationContext(),"xButton눌림",Toast.LENGTH_LONG).show();
                }
            });


            //이부분 나중에 다시 공부할것 .. 태그란 ??
            view.setTag(holder);
        }
        else
        {
            holder=(ViewHolder2)view.getTag();
        }

        final Video_ListData videodata=arraylist.get(position);

            /*이미지 부분 추가할것
            if(tvdata.screenimage!=null)
            */

        //방제목, 날짜, 이미지 url
        holder.roomtitle.setText(videodata.roomtitle);

        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss");
        String date= dayTime.format(new Date(Long.parseLong(videodata.date)));
        holder.date.setText(date);
        //holder.videoimage.setImageBitmap();

        try {
            Glide.with(context).load(new URL(videodata.videoimageurl)).thumbnail(0.1f).into(holder.videoimage);

            //CircleImage부분을 Glide로 처리한다.
            Glide.with(context).load(new URL(videodata.profilepath)).thumbnail(0.1f).into(holder.pub_circle);
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        //닉네임부분과 CircleImage부분을 실행한다.
        holder.pub_nickname.setText(videodata.nickname);

        return view;
    }
}
