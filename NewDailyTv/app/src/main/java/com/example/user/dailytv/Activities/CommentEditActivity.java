package com.example.user.dailytv.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.dailytv.R;

/**
 * Created by user on 2017-12-22.
 */

public class CommentEditActivity extends AppCompatActivity {

    EditText commentEdit;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commenteditactivity);

        initUi();
        initActionbar();

    }

    public void initUi()
    {

        commentEdit=findViewById(R.id.commentEdit);
        Intent intent=getIntent();
        String origintext=intent.getExtras().getString("origintext");
        commentEdit.setText(origintext);
        commentEdit.requestFocus();

        //키보드를 보이게 하는 부분
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void initActionbar()
    {
        ActionBar actionbar=getSupportActionBar();

        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeButtonEnabled(true);

        actionbar.setTitle("뒤로가기");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.newpost_write,menu);
        return true;
    }


    //댓글 입력기능
    //1. 메뉴에서 작성버튼을 누르게 되면 현재 EditText에 있는 값을 인텐트에 넣는다.
    //2. 인텐트에 저장된 String값을 CommentOptionEditAcvity로 전달한다.
    //3. 이후 CommentOptionActivity는 String값과 position값을 VideoPreviewActivity로 전달한다.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId=item.getItemId();

        if(itemId==R.id.newPost)
        {
            if(commentEdit.getText().toString().equals(""))
            {
                Toast.makeText(getApplicationContext(),"내용을 입력해주세요",Toast.LENGTH_LONG).show();
            }
            else {

                String text = commentEdit.getText().toString();
                Intent intent = new Intent();
                intent.putExtra("text", text);
                setResult(RESULT_OK, intent);
                finish();
            }

        }else if(itemId==android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);

    }
}
