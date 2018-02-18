package com.example.user.dailytv.Activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.dailytv.Module.RadioGridGroup;
import com.example.user.dailytv.R;
import com.quickblox.sample.groupchatwebrtc.App;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by user on 2017-12-31.
 */

public class BuyMoonActivity extends AppCompatActivity implements RadioButton.OnClickListener,RadioButton.OnCheckedChangeListener {

    final App.GlobalVariable global=App.getGlobal();

    EditText moonCountEdit,moonPriceEdit;
    EditText phoneNumberEdit,certNumberEdit;

    int certificationNum;

    boolean isPayOkay=false;






    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.buymoonactivity);

        initMoonPirce();

        initActionbar();

        initPaymentUi();



    }

    private void initPaymentUi()
    {

        //주문 정보창에 현재 시간을 설정한다.
        final TextView timeTv=(TextView)findViewById(R.id.time_tv);
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
        String timeStr= timeFormat.format(new Date(System.currentTimeMillis()));
        timeTv.setText(timeStr);

        Button cancelBtn=(Button)findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(this);

        Button certRequestBtn=(Button)findViewById(R.id.certRequestBtn);
        certRequestBtn.setOnClickListener(this);

        Button certConfirmBtn=(Button)findViewById(R.id.certConfirmBtn);
        certConfirmBtn.setOnClickListener(this);

        Button okButton=(Button)findViewById(R.id.okButton);
        okButton.setOnClickListener(this);


        //숫자만 입력하는 필터와, edittext의 최대길이를 제한하는 필터를 적용한다.

        phoneNumberEdit=(EditText)findViewById(R.id.phoneNumber_edit);
        phoneNumberEdit.setFilters(new InputFilter[]{filterAlphaNum,new InputFilter.LengthFilter(8)});

        certNumberEdit=(EditText)findViewById(R.id.certNumber_edit);
        certNumberEdit.setFilters(new InputFilter[]{filterAlphaNum,new InputFilter.LengthFilter(6)});

    }



    // 영문 + 숫자 만 입력 되도록
    public InputFilter filterAlphaNum = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[0-9]$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };


    // SMS 문자를 전송하고 BroadcastReceiver로 전송을 확인할 수 있는 메서드이다.

    private void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        //1. pending intent는 3가지 상태가 있다. 액티비티실행, Broadcasting, 서비스 실행
        //2. 그중에 boradcast receiver을 수행하는 pending intent를 선언한다.

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);



        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "알림 문자 메시지가 전송되었습니다.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    public void initActionbar()
    {
        ActionBar actionbar=getSupportActionBar();

        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeButtonEnabled(true);

        actionbar.setTitle("뒤로가기");

    }


    @Override
    public void onClick(View v) {

        final RelativeLayout priceLayout=(RelativeLayout)findViewById(R.id.price_layout);
        final RelativeLayout priceHowLayout=(RelativeLayout)findViewById(R.id.pricehow_layout);
        final RelativeLayout paymentLayout=(RelativeLayout)findViewById(R.id.payment_layout);

        if(v.getId()==R.id.payBtn)
        {
            Spinner spinnerPay=(Spinner)findViewById(R.id.spinner1);
            String result= (String)spinnerPay.getSelectedItem();

            if(result.equals("선택"))
            {
                Toast.makeText(getApplicationContext(),"결제 방식을 선택해주세요.",Toast.LENGTH_LONG).show();
            }
            else
            {

                priceLayout.setVisibility(View.GONE);
                priceHowLayout.setVisibility(View.GONE);
                paymentLayout.setVisibility(View.VISIBLE);


                String moonPrice=moonPriceEdit.getText().toString();
                final TextView paymentAmountTv=(TextView)findViewById(R.id.paymentAmount_tv);
                paymentAmountTv.setText(moonPrice+"원");
            }
        }
        //취소 버튼을 누르면 별풍선 수량 입력 페이지가 보여진다.
        else if(v.getId()==R.id.cancelBtn)
        {

            priceLayout.setVisibility(View.VISIBLE);
            priceHowLayout.setVisibility(View.VISIBLE);
            paymentLayout.setVisibility(View.GONE);
        }
        //1. 휴대폰 인증 버튼을 누르게 되면 현재 입력한 번호로 sms 메세지가 전송되게 된다.
        //2. 랜덤으로 지정된 인증번호 6자리를 전송한다.
        //3. 이후 인증번호를 입력하여 지정된 인증번호가 일치하면 별풍선 갯수를 늘려주고 서버로 업로드한다.
        //4. 만약 틀리다면 결재 정보가 틀리다는 메세지와 함께 다음 화면으로 넘어가지 않는다.
        //5. (추가) 3분 안에 결재를 완료하지 못하였을때는 다시 인증해달라는 메세지와 함께 인증이 되지 않는다.

        else if(v.getId()==R.id.certRequestBtn)
        {


            certificationNum=randomRange(100000,999999);
            Toast.makeText(getApplicationContext(),"생성된 인증번호 : "+certificationNum+"",Toast.LENGTH_LONG).show();

            final Spinner frontNumSpinner=(Spinner)findViewById(R.id.spinner2);
            final String frontNum=(String)frontNumSpinner.getSelectedItem();

            final String phoneNumber=phoneNumberEdit.getText().toString();

            if(phoneNumberEdit.length()==8) {
                sendSMS(frontNum+phoneNumber,certificationNum+"");
            }
            else
            {
                Toast.makeText(getApplicationContext(),"휴대폰번호 8자리를 입력해주세요.",Toast.LENGTH_LONG).show();
            }

        }

        else if(v.getId()==R.id.certConfirmBtn)
        {
            certNumberEdit=(EditText)findViewById(R.id.certNumber_edit);
            int certificationNumCheck=Integer.parseInt(certNumberEdit.getText().toString());

            if(certNumberEdit.length()==6) {

                if(certificationNum==certificationNumCheck)
                {
                    Toast.makeText(getApplicationContext(),"인증 성공",Toast.LENGTH_LONG).show();
                    isPayOkay=true;

                    //new moonStarIncreaseTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
                else
                {
                    Toast.makeText(getApplicationContext(),"인증 번호가 일치하지 않습니다.",Toast.LENGTH_LONG).show();
                    isPayOkay=false;
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(),"인증번호 6자리를 입력해주세요.",Toast.LENGTH_LONG).show();
                isPayOkay=false;
            }

        }
        else if(v.getId()==R.id.okButton)
        {
            if(isPayOkay)
            {
                Toast.makeText(getApplicationContext(),"결제가 완료되었습니다.",Toast.LENGTH_LONG).show();
                new moonStarIncreaseTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            else
            {
                Toast.makeText(getApplicationContext(),"휴대폰 인증이 필요합니다.",Toast.LENGTH_LONG).show();

            }
        }
    }

    //랜덤한 범위안의 정수를 생성하는 함수이다.

    public static int randomRange(int n1, int n2) {
        return (int) (Math.random() * (n2 - n1 + 1)) + n1;
    }

    private void initMoonPirce()
    {
        RadioGridGroup radiogridgroup=(RadioGridGroup)findViewById(R.id.radioGridGroup);

        RadioButton rad1=(RadioButton)findViewById(R.id.rad1);
        rad1.setOnCheckedChangeListener(this);

        RadioButton rad2=(RadioButton)findViewById(R.id.rad2);
        rad2.setOnCheckedChangeListener(this);

        RadioButton rad3=(RadioButton)findViewById(R.id.rad3);
        rad3.setOnCheckedChangeListener(this);

        RadioButton rad4=(RadioButton)findViewById(R.id.rad4);
        rad4.setOnCheckedChangeListener(this);

        RadioButton rad5=(RadioButton)findViewById(R.id.rad5);
        rad5.setOnCheckedChangeListener(this);

        RadioButton rad6=(RadioButton)findViewById(R.id.rad6);
        rad6.setOnCheckedChangeListener(this);



        moonCountEdit=(EditText)findViewById(R.id.moonCount_edit);
        moonPriceEdit=(EditText)findViewById(R.id.moonPrice_edit);

        moonCountEdit.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            //1. 별풍선을 입력할때 문자열의 예외처리 동작을 선언한다.
            //2. 문자열이 변화하기 전 그리고 변화한 후에 동작하는 이벤트를 선언한다.
            //3. 문자열의 띄어쓰기시와 별풍선 갯수앞에 0이 들어갔을때 ex)0123,00101 의 예외처리 동작을 선언한다.
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String moonCountString=moonCountEdit.getText().toString().replaceAll(" ","");


                Log.e("moonCountString","두번째 : "+moonCountString);


                try {
                    //갯수 텍스트에서 가져온 값을 통해 가격 텍스트에 입력한다.
                    int moonCount = Integer.parseInt(moonCountString);
                    String moonPriceText = (moonCount * 110) + "";
                    moonPriceEdit.setText(moonPriceText);
                }catch (Exception e)
                {
                    Log.e("moonCount파싱에러",e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        Button payButton=(Button)findViewById(R.id.payBtn);
        payButton.setOnClickListener(this);
    }



    //이 클래스는 별풍선을 구매했을 때 인증요청이 성공하면 실제 DB의 별풍선의 갯수를 증가시켜주는 작업을
    //백그라운드로 처리하는 클래스이다.

    public class moonStarIncreaseTask extends AsyncTask<Void,Void,String>
    {

        int mooncount;
        //이 함수의 작업순서
        //1. post_stringkey를 얻어서 okhttp3로 서버로 전송한다
        //2. 서버의 필요한 정보를 json값으로 받아서 onpostExecute에서 파싱한다.
        //3. 파싱을 한 뒤에 리스트뷰에 현재 아이템을 입력시키고 어뎁터를 새로 생신한다.

        //이 함수의 작업순서
        //1. 현재 접속된 유저(id)의 id와 증가시킬 별풍선의 갯수(moonCount)를 서버로 전송한다.

        @Override
        protected String doInBackground(Void... voids) {



            final String INSERT_COMMENT_URL="http://"+global.getIP()+"/moonstar_increase.php";



            SharedPreferences shared=getSharedPreferences("logininfo",Activity.MODE_PRIVATE);
            String id=shared.getString("userid",null);

            String mooncountStr=moonCountEdit.getText().toString().replaceAll(" ","");
            mooncount=Integer.parseInt(mooncountStr);

            OkHttpClient client=new OkHttpClient();
            RequestBody body=new FormBody.Builder()
                    .add("id",id)
                    .add("mooncount",mooncount+"")
                    .build();

            Request request=new Request.Builder()
                    .url(INSERT_COMMENT_URL)
                    .header("Content-Type","text/html")
                    .post(body)
                    .build();


            try {

                Response response=client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e)
            {
                Log.e("VideoPreviewActivity","비디오 좋아요 업데이트 query오류"+e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //Toast.makeText(getApplicationContext(),"전송한 moonCount : "+mooncount,Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(),result+"",Toast.LENGTH_LONG).show();

            Log.e("BuyMoonActivity","별풍선 결제 작업 결과 : "+result);


            Intent intent=new Intent();
            intent.putExtra("mooncount",mooncount+"");
            setResult(RESULT_OK,intent);
            finish();


        }
    }




    //라디오 버튼이 변하게 되면 체크 체인지가 2번 변하게 된다.
    //첫번째가 눌린 check 체인지
    //두번째가 바뀐 check 체인지
    //바뀐 부분을 무효화 시킨다.

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        Toast.makeText(getApplicationContext(),"체크 체인지",Toast.LENGTH_LONG).show();
        int checkedId=buttonView.getId();
        final EditText moonCountEdit=(EditText)findViewById(R.id.moonCount_edit);

        if(isChecked) {
            if (checkedId == R.id.rad1) {
                moonCountEdit.setText("30");
            } else if (checkedId == R.id.rad2) {
                moonCountEdit.setText("50");
            } else if (checkedId == R.id.rad3) {
                moonCountEdit.setText("100");
            } else if (checkedId == R.id.rad4) {
                moonCountEdit.setText("300");
            } else if (checkedId == R.id.rad5) {
                moonCountEdit.setText("500");
            } else if (checkedId == R.id.rad6) {
                moonCountEdit.setText("1000");
            }
        }
    }




}
