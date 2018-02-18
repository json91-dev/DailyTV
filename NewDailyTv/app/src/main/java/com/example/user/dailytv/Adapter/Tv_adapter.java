package com.example.user.dailytv.Adapter;

import android.content.Context;
import android.net.Uri;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.dailytv.Activities.TabviewActivity;
import com.example.user.dailytv.ListData.TV_ListData;
import com.example.user.dailytv.R;

import java.util.ArrayList;

/**
 * Created by user on 2017-12-19.
 */

//TabviewActivity의 2번째 Tab에 있는 리스트뷰의 어뎁터 설정
public class Tv_adapter extends BaseAdapter
{
    private Context context=null;
    private ArrayList<TV_ListData> arraylist=new ArrayList<>();

    //뷰홀더 설정
    private static class ViewHolder{

        public ImageView screenimage;
        public TextView roomtitle;
        public TextView bjnickname;
        public TextView viewernumber;
        public ImageButton optionmenubutton;

    }


    public Tv_adapter(Context context)
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

    public void addItem(String roomtitle,String roompassword,String bjnickname,String viewernumberlimit,String viewernumber,String screenimage,String publisherid)
    {
        TV_ListData item=null;
        item=new TV_ListData();

        item.roomtitle=roomtitle;
        item.roompassword=roompassword;
        item.bjnickname=bjnickname;
        item.viewernumberlimit=viewernumberlimit;
        item.viewernumber=viewernumber;
        item.screenimage=screenimage;

        item.publisherid=publisherid;


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
            view=inflater.inflate(R.layout.tvlistview_item,null);

            holder.screenimage=(ImageView)view.findViewById(R.id.screenimage);
            holder.roomtitle=(TextView)view.findViewById(R.id.roomtitle);
            holder.bjnickname=(TextView)view.findViewById(R.id.bjnickname);
            holder.viewernumber=(TextView)view.findViewById(R.id.viewernumber);
            holder.optionmenubutton=(ImageButton)view.findViewById(R.id.optionmenubutton);

            //이부분 나중에 다시 공부할것 .. 태그란 ??
            view.setTag(holder);
        }
        else
        {
            holder=(ViewHolder)view.getTag();
        }

        final TV_ListData tvdata=arraylist.get(position);

            /*이미지 부분 추가할것
            if(tvdata.screenimage!=null)
            */

        holder.roomtitle.setText(tvdata.roomtitle);
        //비밀번호자리
        holder.bjnickname.setText(tvdata.bjnickname);
        //시청인원 제한 자리
        holder.viewernumber.setText(tvdata.viewernumber);

        //이부분은 작업이 오래걸리므로 별도의 쓰레드를 써서 처리한다.

        if(tvdata.publisherid.equals("user1"))
        {

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
            requestOptions.skipMemoryCache(true);


            Glide.with(context).load(tvdata.screenimage).apply(requestOptions).thumbnail(0.3f).into(holder.screenimage);

        }
        else{
            Glide.with(context).load(tvdata.screenimage).thumbnail(0.3f).into(holder.screenimage);
        }

        //new TabviewActivity.imagetaskinlistview().execute(new TabviewActivity.imagetaskinlistviewinput(tvdata.screenimage,holder.screenimage));
        //holder.screenimage.setImageDrawable(tvdata.screenimage);

        /////////////리스트뷰 옵션 메뉴 이벤트 등록///////////////////

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