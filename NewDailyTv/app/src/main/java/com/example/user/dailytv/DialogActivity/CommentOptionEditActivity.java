package com.example.user.dailytv.DialogActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.user.dailytv.Activities.CommentEditActivity;
import com.example.user.dailytv.R;

/**
 * Created by user on 2017-12-22.
 */

public class CommentOptionEditActivity extends Activity implements Button.OnClickListener{

    final int FromCommentEditActivity=1004;

    //이전 액티비티에서 넘어온 포지션값
    int position;
    String origintext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commentoptionactivity_edit);

        Button deleteButton=(Button)findViewById(R.id.deleteButton);
        Button editButton=(Button)findViewById(R.id.editButton);
        Button cancelButton=(Button)findViewById(R.id.cancelButton);

        deleteButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        Intent received=getIntent();
        position=received.getExtras().getInt("position");
        origintext=received.getExtras().getString("origintext");

        Log.e("CommentOption","VideoActivity로부터 넘어온 position값"+position);
        Log.e("CommentOption","VideoActivity로부터 넘어온 origintext값"+origintext);
        if(v.getId()==R.id.deleteButton)
        {
            Intent intent=new Intent();
            intent.putExtra("option","delete");
            intent.putExtra("position",position);
            setResult(RESULT_OK,intent);
            finish();
        }
        else if(v.getId()==R.id.editButton){
            Intent intent=new Intent(CommentOptionEditActivity.this, CommentEditActivity.class);
            intent.putExtra("origintext",origintext);
            intent.putExtra("position",position);

            startActivityForResult(intent,FromCommentEditActivity);
        }
        else
        {
            //Intent intent=new Intent();
            //setResult(RESULT_OK);

            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode!=RESULT_OK)
        {
            return;
        }
        switch (requestCode)
        {

            //CommentEditActivity에서 받아온 텍스트 값을 VideoPreviewActivity로 다시 넘겨준다.

            case FromCommentEditActivity:{



                String text=data.getExtras().getString("text");

                Log.e("OptionEditActivity","CommentEditActivity에서 받아온 텍스트값 : "+text);

                Intent intent=new Intent();
                intent.putExtra("option","edit");
                intent.putExtra("position",position);
                intent.putExtra("text",text);

                setResult(RESULT_OK,intent);
                finish();

            }
        }
    }
}
