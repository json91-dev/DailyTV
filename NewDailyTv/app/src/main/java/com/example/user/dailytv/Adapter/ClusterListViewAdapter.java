package com.example.user.dailytv.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.user.dailytv.ListData.MarkerClusterItem;
import com.example.user.dailytv.ListData.TV_ListData;
import com.example.user.dailytv.R;


import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by user on 2018-01-10.
 */

public class ClusterListViewAdapter extends BaseAdapter {

    private Context context=null;
    private ArrayList<MarkerClusterItem> arraylist=new ArrayList<>();

    //뷰홀더 설정
    private static class ViewHolder{

        public ImageView screenimage;
        public ImageView mark;

        public TextView roomtitle;
        public TextView bjnickname;
        public TextView viewernumber;
        public TextView viewernumberTag;

        public ImageButton optionmenubutton;

    }


    public ClusterListViewAdapter(Context context)
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

    public void addItem(ArrayList<MarkerClusterItem> clusterItems)
    {


        for(MarkerClusterItem item: clusterItems)
        {
                arraylist.add(item);
        }
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
        notifyDataSetChanged();
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
            view=inflater.inflate(R.layout.i_clusterlistview,null);

            holder.screenimage=(ImageView)view.findViewById(R.id.screenimage);
            holder.roomtitle=(TextView)view.findViewById(R.id.roomtitle);
            holder.bjnickname=(TextView)view.findViewById(R.id.bjnickname);
            holder.viewernumber=(TextView)view.findViewById(R.id.viewernumber);
            holder.optionmenubutton=(ImageButton)view.findViewById(R.id.optionmenubutton);
            holder.mark=(ImageView)view.findViewById(R.id.mark);
            holder.viewernumberTag=(TextView)view.findViewById(R.id.viewernumbertag);

            //이부분 나중에 다시 공부할것 .. 태그란 ??
            view.setTag(holder);
        }
        else
        {
            holder=(ViewHolder)view.getTag();
        }


        final MarkerClusterItem markerItem=arraylist.get(position);

        // 리스트뷰 아이템의 이미지와 타이틀, 닉네임 추가 정보를 표시한다.

        holder.screenimage.setImageDrawable(markerItem.getImageDrawable_listview());



        holder.roomtitle.setText(markerItem.getTitle());
        holder.bjnickname.setText(markerItem.getNickname());


        // 라이브 방송일때와 Vod일때 각각 리스트뷰의 레이아웃을 다르게 바꿔준다.

        if(markerItem.getType().equals("live"))
        {
            holder.viewernumberTag.setVisibility(View.VISIBLE);

            holder.viewernumber.setText(markerItem.getViwernumber());
            holder.viewernumber.setTextColor(Color.parseColor("#BDBDBD"));

            Drawable drawableImage= ResourcesCompat.getDrawable(context.getResources(),R.drawable.livemark,null);
            holder.mark.setImageDrawable(drawableImage);


        }
        else if(markerItem.getType().equals("vod"))
        {


            holder.viewernumberTag.setVisibility(View.INVISIBLE);


            SimpleDateFormat dayTime = new SimpleDateFormat("yyyy년 MM월 dd일 hh:mm:ss");
            String dateString= dayTime.format(new Date(Long.parseLong(markerItem.getLongdate())));

            holder.viewernumber.setText(dateString);
            holder.viewernumber.setTextColor(Color.parseColor("#FF0000"));


            Drawable drawableImage= ResourcesCompat.getDrawable(context.getResources(),R.drawable.vodmark,null);
            holder.mark.setImageDrawable(drawableImage);

        }



        holder.optionmenubutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("optionmenubutton","1111111");
                PopupMenu popup=new PopupMenu(context,v);

                MenuInflater inflater=popup.getMenuInflater();
                Menu menu=popup.getMenu();
                inflater.inflate(R.menu.optionmenu,menu);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        //Intent intent=new Intent(getApplicationContext(),)
                        Log.e("optionmunubutton","2222222");
                        return false;
                    }
                });

                popup.show();

            }
        });



        return view;
    }


}
