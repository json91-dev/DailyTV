package com.example.user.dailytv.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.user.dailytv.ListData.Comment_ListData;
import com.example.user.dailytv.ListData.TV_ListData;
import com.example.user.dailytv.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by user on 2017-12-19.
 */


//이 Adapter는 VideoPreview엑티비티의 댓글 목록을 표시해줄때 사용되는 어뎁터 클래스이다.

public class Comment_adapter extends BaseAdapter
{
    private Context context=null;
    private ArrayList<Comment_ListData> arraylist=new ArrayList<>();

    //뷰홀더 설정
    private static class ViewHolder{

        public CircleImageView circleimage;
        public TextView nickname;
        public TextView date;
        public TextView like;
        public TextView commentText;

    }


    public Comment_adapter(Context context)
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

    public void addItem(String commentNumber,String circleimageurl,String nickname,String date,String like,String commentText,String userid)
    {
        Comment_ListData item=null;
        item=new Comment_ListData();

        item.commentNumber=commentNumber;
        item.circleimageurl=circleimageurl;
        item.commentText=commentText;
        item.date=date;
        item.like=like;
        item.nickname=nickname;
        item.userid=userid;

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
        final ViewHolder holder;

        //뷰에 아무것도 없으면
        if(view==null)
        {
            //홀더 초기화를 한다.
            holder=new ViewHolder();

            //리스트뷰 레이아웃을 객체화(실제 메모리에 로드)

            LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view=inflater.inflate(R.layout.comment_listitem,null);

            holder.circleimage=(CircleImageView)view.findViewById(R.id.circleimage);
            holder.commentText=(TextView)view.findViewById(R.id.commentText);
            holder.date=(TextView)view.findViewById(R.id.c_date);
            holder.like=(TextView)view.findViewById(R.id.c_like);
            holder.nickname=(TextView)view.findViewById(R.id.c_nickname);

            view.setTag(holder);
        }
        else
        {
            holder=(ViewHolder)view.getTag();
        }

        final Comment_ListData commentItem=arraylist.get(position);


        holder.nickname.setText(commentItem.nickname);
        holder.like.setText("하트 "+commentItem.like+"개");
        // 시간 설정
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss");
        String date= dayTime.format(new Date(Long.parseLong(commentItem.date)));
        holder.date.setText(date);
        holder.commentText.setText(commentItem.commentText);

        //이부분 null오류 뜨는데 뭐가 문제인지
        Log.e("Comment_adapter",commentItem.circleimageurl+"");

        Glide.with(context).load(commentItem.circleimageurl).thumbnail(0.3f).into(holder.circleimage);


        return view;
    }
}