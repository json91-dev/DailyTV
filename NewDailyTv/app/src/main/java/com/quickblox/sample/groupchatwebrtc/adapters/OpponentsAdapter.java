package com.quickblox.sample.groupchatwebrtc.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.dailytv.R;
import com.quickblox.sample.core.ui.adapter.BaseSelectableListAdapter;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.sample.core.utils.UiUtils;

import com.quickblox.users.model.QBUser;

import org.w3c.dom.Text;

import java.util.List;

/**
 * QuickBlox team
 */
public class OpponentsAdapter extends BaseSelectableListAdapter<QBUser> {

    private SelectedItemsCountsChangedListener selectedItemsCountChangedListener;

    public OpponentsAdapter(Context context, List<QBUser> users) {
        super(context, users);
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_opponents_list, null);
            holder = new ViewHolder();
            holder.opponentIcon = (ImageView) convertView.findViewById(R.id.image_opponent_icon);
            holder.opponentName = (TextView) convertView.findViewById(R.id.opponentsName);

            //밑에부터 커스텀한부분
            holder.gender=(TextView)convertView.findViewById(R.id.gender);
            holder.age=(TextView)convertView.findViewById(R.id.age);
            holder.hobby=(TextView)convertView.findViewById(R.id.hobby);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBUser user = getItem(position);

        String imageurl="";
        if (user != null) {

            try {
                //토큰인덱스
                // 대화명=>0
                // 성별 =>1
                // 나이 =>2
                // 취미 =>3
                String [] token=user.getFullName().split("\\*");

                holder.opponentName.setText(token[0]);
                holder.gender.setText(token[1]);
                holder.age.setText(token[2]);
                holder.hobby.setText(token[3]);
                imageurl=token[4];

            }catch (Exception e)
            {
                e.printStackTrace();
            }



            if (selectedItems.contains(user)){
                //선택되었을때
                // 현재 뷰에 선택되었다는 전체 회색 표시를 한다.
                convertView.setBackgroundResource(R.color.background_color_selected_user_item);

                //아이콘의 배경색을 회색창으로 바꾼다.
                holder.opponentIcon.setBackgroundDrawable(
                        UiUtils.getColoredCircleDrawable(ResourceUtils.getColor(R.color.icon_background_color_selected_user)));

                //아이콘에 체크표시를 한다.
                holder.opponentIcon.setImageResource(R.drawable.ic_checkmark);

            } else {
                //전체 배경을 원래의 흰색상태로 되돌린다
                convertView.setBackgroundResource(R.color.background_color_normal_user_item);


                //아이콘의 배경을 흰색으로 전환시킨다.
                //holder.opponentIcon.setBackgroundDrawable(UiUtils.getColorCircleDrawable(user.getId()));
                //holder.opponentIcon.setImageResource(R.drawable.ic_person);
                holder.opponentIcon.setBackgroundColor(Color.parseColor("#ffffff"));

                //글라이드 이미지를 추가한다.
                //Glide.with(getApplicationContext()).load(urlstring).thumbnail(0.3f).into(imageview);
                //과연 이게 잘 될지는 모르겠당..
                Glide.with(context).load(imageurl).apply(RequestOptions.circleCropTransform()).thumbnail(0.3f).into(holder.opponentIcon);

            }
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelection(position);
                selectedItemsCountChangedListener.onCountSelectedItemsChanged(selectedItems.size());
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        ImageView opponentIcon;
        TextView opponentName;

        //이부분은 내가 임의로 추가한 홀더 패턴이다
        TextView gender;
        TextView age;
        TextView hobby;

    }

    public void setSelectedItemsCountsChangedListener(SelectedItemsCountsChangedListener selectedItemsCountsChanged){
        if (selectedItemsCountsChanged != null) {
            this.selectedItemsCountChangedListener = selectedItemsCountsChanged;
        }
    }

    public interface SelectedItemsCountsChangedListener{
        void onCountSelectedItemsChanged(int count);
    }
}
