package com.example.user.dailytv.DialogActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.example.user.dailytv.R;

/**
 * Created by user on 2017-12-20.
 */

public class CommentOptionActivity extends Activity  implements Button.OnClickListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commentoptionactivity);

        initUi();
    }

    public void initUi()
    {
        Button likeButton=(Button)findViewById(R.id.likeButton);
        likeButton.setOnClickListener(this);
        Button reportButton=(Button)findViewById(R.id.reportButton);
        reportButton.setOnClickListener(this);
        Button cancelButton=(Button)findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        Intent received=getIntent();
        int position=received.getExtras().getInt("position");


        if(v.getId()==R.id.likeButton)
        {
            Intent intent=new Intent();
            intent.putExtra("option","like");
            intent.putExtra("position",position);
            setResult(RESULT_OK,intent);
            finish();
        }
        else if(v.getId()==R.id.reportButton)
        {
            Intent intent=new Intent();
            intent.putExtra("option","report");
            setResult(RESULT_OK,intent);
            finish();
        }
        else
        {
            finish();
        }

    }
}
