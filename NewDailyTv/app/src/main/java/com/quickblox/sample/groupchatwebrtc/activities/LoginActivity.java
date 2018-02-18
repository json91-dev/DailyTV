package com.quickblox.sample.groupchatwebrtc.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.dailytv.R;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.helper.Utils;
import com.quickblox.sample.core.utils.KeyboardUtils;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.core.utils.Toaster;

import com.quickblox.sample.groupchatwebrtc.App;
import com.quickblox.sample.groupchatwebrtc.services.CallService;
import com.quickblox.sample.groupchatwebrtc.util.qbUserCustomClass;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.utils.QBEntityCallbackImpl;
import com.quickblox.sample.groupchatwebrtc.utils.UsersUtils;
import com.quickblox.sample.groupchatwebrtc.utils.ValidationUtils;
import com.quickblox.users.model.QBUser;



/*
    5: 옵션 아이템 클릭부분
    6,7 : 방제목, 유저이름 형식체크
    9 : 키보드 숨기기
    10 : QBUser 객체 반환 + String 값 넘겨주기
    11 : QBUser 객체 생성하는 부분
    12 : Device ID 생성하는 부분
    13 : 외부로 로그인 시도 요청 보내는 부분
    14:  로그인 성공시 QBUser 객체 저장 후에 로그인


 */

public class LoginActivity extends BaseActivity {


    private String TAG = LoginActivity.class.getSimpleName();

    private EditText userNameEditText;
    private EditText chatRoomNameEditText;

    // 이부분은 내가 추가한 에디트 텍스트이다.
    // 성별과, 나이와, 취미생활에대한 정보를 입력받는 EditText를 추가한다.
    private EditText userGenderEditText;
    private EditText userAgeEditText;
    private EditText userHobbyEditText;


    private QBUser userForSave;


    final App.GlobalVariable global=App.getGlobal();
    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_login);

        Log.e("퀵2","1111111111111111111111111~~~~~~~~~~~~~~~~~~~ㅠㅠ");

        initUI();
    }

    @Override
    protected View getSnackbarAnchorView() {

        Log.e("퀵2","22222222222222222");
        return findViewById(R.id.root_view_login_activity);
    }

    private void initUI() {

        //에디트 텍스트에 변화할떄 이벤트 감지할수 있도록 하는 부분..
        Log.e("퀵2","3333333333333333");
        setActionBarTitle(R.string.title_login_activity);
        userNameEditText = (EditText) findViewById(R.id.user_name);
        userNameEditText.addTextChangedListener(new LoginEditTextWatcher(userNameEditText));

        chatRoomNameEditText = (EditText) findViewById(R.id.chat_room_name);
        chatRoomNameEditText.addTextChangedListener(new LoginEditTextWatcher(chatRoomNameEditText));

        //이부분은 내가 추가적으로 등록한 EditText들의 이벤트 설정이다.
        //text가 변화할때 감지할수 있도록 TextWacher을 통해 이벤트를 등록시킨다.

        userGenderEditText=(EditText)findViewById(R.id.user_gender);
        userGenderEditText.addTextChangedListener(new LoginEditTextWatcher(userGenderEditText));

        userAgeEditText=(EditText)findViewById(R.id.user_age);
        userAgeEditText.addTextChangedListener(new LoginEditTextWatcher(userAgeEditText));

        userHobbyEditText=(EditText)findViewById(R.id.user_hobby);
        userHobbyEditText.addTextChangedListener(new LoginEditTextWatcher(userHobbyEditText));



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.e("퀵2","444444444444444444444");
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.e("퀵2","555555555555555");

        switch (item.getItemId()) {
            case R.id.menu_login_user_done:


                //hideKeyboard();
                //startSignUpNewUser(createUserWithEnteredData());



                if (isEnteredUserNameValid() && isEnteredRoomNameValid() && isEnteredGenderValid() && isEnteredAgeValid() &&isEnteredHobbyValid() ) {
                    hideKeyboard();
                    startSignUpNewUser(createUserWithEnteredData());
                }


                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isEnteredUserNameValid() {



        Log.e("퀵2","6666666666666666666");

        return ValidationUtils.isUserNameValid(this, userNameEditText);

    }

    private boolean isEnteredRoomNameValid() {


        Log.e("퀵2","7[RoomNameCheck]777777777777777777");

        return ValidationUtils.isRoomNameValid(this, chatRoomNameEditText);

    }

    //내가 추가적으로 유효성 검사를 위해 추가한 함수들이다.
    //VaildatinUtils를 통해 입력된 Text의 유효성을 검사하는 부분이다.

    private boolean isEnteredGenderValid() {


        Log.e("퀵2","7[GenderCheck]777777777777777777");

        return ValidationUtils.isGenderValid(this, userGenderEditText);

    }

    private boolean isEnteredAgeValid() {


        Log.e("퀵2","7[AgeCheck]777777777777777777");

        return ValidationUtils.isAgeValid(this, userAgeEditText);

    }

    private boolean isEnteredHobbyValid() {


        Log.e("퀵2","7[Hobby]777777777777777777");

        return ValidationUtils.ishobbyValid(this, userHobbyEditText);

    }



    //키보드를 숨기는 함수라고 하는데 아직 정확히 어디에 쓰이는 지는 잘 모르겠다..
    private void hideKeyboard() {



        Log.e("퀵2","9999999999999999999999999999");


        KeyboardUtils.hideKeyboard(userNameEditText);
        KeyboardUtils.hideKeyboard(chatRoomNameEditText);

        //이부분은 내가 추가한 EditText들이다
        KeyboardUtils.hideKeyboard(userGenderEditText);
        KeyboardUtils.hideKeyboard(userAgeEditText);
        KeyboardUtils.hideKeyboard(userHobbyEditText);


    }

    private QBUser createUserWithEnteredData() {




        Log.e("퀵2","10~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");


        return createQBUserWithCurrentData(String.valueOf(userNameEditText.getText()),
                String.valueOf(chatRoomNameEditText.getText()));
    }

    private QBUser createQBUserWithCurrentData(String userName, String chatRoomName) {

        //Log.e("퀵2","14~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        Log.e("퀵2","11슈발...~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("추적1","000000000000000000000000000000");

        //QBUser에는 유저 아이디 이메일 패스워드 접속시간 등등 여러가지 정보가 저장됨..
        QBUser qbUser = null;

        //TextUtil로 null 체크를 할 수 있는 부분이다.
        //이 조건문은 입력받은 유저의 이름과 아이디를 체크한 뒤에 QBUser을 생성하는 조건절이다.
        //유저 이름 => 각자의 방 title
        //채팅방 이름 => 유저가 접속한 방 제목 or 채널
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(chatRoomName)) {


            /*
            Log.e("추적1","썅1111111111111111111111111");

            StringifyArrayList<String> userTags = new StringifyArrayList<>();
            userTags.add(chatRoomName);

            qbUser = new QBUser();

            qbUser.setFullName(userName);
            qbUser.setLogin(getCurrentDeviceId());
            qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
            qbUser.setEmail("jjjjjw910911@naver.com");
            qbUser.setPhone("010-6284-8051");


            //태그부분이 이해가 잘 가지는 않는다...
            //아마 chatRoomName을 통해 방을 생성하는 부분이 아닐까 싶다.
            qbUser.setTags(userTags);

            Log.e("추적1","33333333333333333"+qbUser.getPhone()+"");



            //이부분은 내가 커스텀한 데이터 클래스를 QBUser에 담아서 상대방에게 전송하는 코드이다.

            //이부분에서 데이터가 전송이 되지않는다 .. 왜일까?? 음.... 추적을 해봐야겠다

            qbUserCustomClass item=new qbUserCustomClass();
            item.gender=userGenderEditText.getText().toString();
            item.age=userAgeEditText.getText().toString();
            item.hobby=userHobbyEditText.getText().toString();

            qbUser.setCustomDataAsObject(item);

            */
            //오리지날 코드..
            StringifyArrayList<String> userTags = new StringifyArrayList<>();
            userTags.add(chatRoomName);

            //이부분에서 userTag에 값을 집어넣는다
            //값을 집어넣는 순서는 성별 나이 취미순이다

            final String age=userAgeEditText.getText().toString();
            final String gender=userGenderEditText.getText().toString();
            final String hobby=userHobbyEditText.getText().toString();
            //userTags.add(age);
            //userTags.add(gender);
            //userTags.add(hobby);



            qbUser = new QBUser();
            qbUser.setFullName(userName+"*"+age+"*"+gender+"*"+hobby+"*"+"http://"+global.getIP()+"/image/penquin.png");
            qbUser.setLogin(getCurrentDeviceId());
            qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);

            qbUser.setCustomData(userName);
            qbUser.setTags(userTags);
        }

        return qbUser;
    }


    private String getCurrentDeviceId() {


        Log.e("퀵2","12~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");



        return Utils.generateDeviceId(this);
    }


    private void startSignUpNewUser(final QBUser newUser) {




        Log.e("퀵2","13~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        //


        showProgressDialog(R.string.dlg_creating_new_user);

        //requestExecutor은 quickblox에서 제공하는 외부로 qbuser에 대한 생성 요청을 보내는 메서드이다..
        //이 부분에서 외부로 새로운 유저의 생성을 요청한다.
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        loginToChat(result);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signInCreatedUser(newUser, true);
                        } else {
                            hideProgressDialog();
                            Toaster.longToast(R.string.sign_up_error);
                        }
                    }
                }
        );
    }

    private void loginToChat(final QBUser qbUser) {



        Log.e("퀵2","14~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
        //userForSave는
        userForSave = qbUser;
        startLoginService(qbUser);
    }

    private void startLoginService(QBUser qbUser) {


        Log.e("퀵2","15~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        //생성된 유저로 CallService를 등록하고 PendingIntent를 선언한다.

        Intent tempIntent = new Intent(this, CallService.class);
        PendingIntent pendingIntent = createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        CallService.start(this, qbUser, pendingIntent);
    }

    //////////////여기부터는 반환값//////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {



        Log.e("퀵2","16~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        //Consts 는 각종 설정 파일을 저장하는 메서드이다.
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Consts.EXTRA_LOGIN_RESULT_CODE) {

            hideProgressDialog();
            boolean isLoginSuccess = data.getBooleanExtra(Consts.EXTRA_LOGIN_RESULT, false);
            String errorMessage = data.getStringExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE);

            if (isLoginSuccess) {

                //로그인 성공시 유저정보를 저장한다.

                saveUserData(userForSave);
                signInCreatedUser(userForSave, false);

            } else {

                Toaster.longToast(getString(R.string.login_chat_login_error) + errorMessage);
                userNameEditText.setText(userForSave.getFullName());
                chatRoomNameEditText.setText(userForSave.getTags().get(0));

            }
        }
    }

    private void saveUserData(QBUser qbUser) {



        Log.e("퀵2","17~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");


        SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
        sharedPrefsHelper.save(Consts.PREF_CURREN_ROOM_NAME, qbUser.getTags().get(0));
        sharedPrefsHelper.saveQbUser(qbUser);
    }

    private void signInCreatedUser(final QBUser user, final boolean deleteCurrentUser) {



        Log.e("퀵2","18~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        requestExecutor.signInUser(user, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                if (deleteCurrentUser) {
                    removeAllUserData(result);
                } else {
                    startOpponentsActivity();
                }
            }

            @Override
            public void onError(QBResponseException responseException) {
                hideProgressDialog();
                Toaster.longToast(R.string.sign_up_error);
            }
        });
    }

    // opponets엑티비티로 넘어간다.
    private void startOpponentsActivity() {


        Log.e("퀵2","19~~~~~~~~~~~~~~~~~~~~~~~~~~~");


        OpponentsActivity.start(LoginActivity.this, false);
        finish();
    }








    private void removeAllUserData(final QBUser user) {

        //호출되지 않음....
        Log.e("퀵2","호출안됨~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        requestExecutor.deleteCurrentUser(user.getId(), new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                UsersUtils.removeUserData(getApplicationContext());
                startSignUpNewUser(createUserWithEnteredData());
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                Toaster.longToast(R.string.sign_up_error);
            }
        });
    }




    private class LoginEditTextWatcher implements TextWatcher {


        private EditText editText;

        private LoginEditTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            editText.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
