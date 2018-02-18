package com.quickblox.sample.groupchatwebrtc.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.example.user.dailytv.R;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.services.SubscribeService;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.core.utils.Toaster;

import com.quickblox.sample.groupchatwebrtc.adapters.OpponentsAdapter;
import com.quickblox.sample.groupchatwebrtc.db.QbUsersDbManager;
import com.quickblox.sample.groupchatwebrtc.services.CallService;
import com.quickblox.sample.groupchatwebrtc.util.qbUserCustomClass;
import com.quickblox.sample.groupchatwebrtc.utils.CollectionsUtils;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.utils.PermissionsChecker;
import com.quickblox.sample.groupchatwebrtc.utils.PushNotificationSender;
import com.quickblox.sample.groupchatwebrtc.utils.UsersUtils;
import com.quickblox.sample.groupchatwebrtc.utils.WebRtcSessionManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;

/**
 * QuickBlox team
 */

//반대편 사용자들을 보여주는 엑티비티라서 Opponents 엑티비티라고 이름지은듯 하다..


public class OpponentsActivity extends BaseActivity {
    private static final String TAG = OpponentsActivity.class.getSimpleName();

    private static final long ON_ITEM_CLICK_DELAY = TimeUnit.SECONDS.toMillis(10);

    private OpponentsAdapter opponentsAdapter;
    private ListView opponentsListView;
    private QBUser currentUser;
    private ArrayList<QBUser> currentOpponentsList;
    private QbUsersDbManager dbManager;
    private boolean isRunForCall;
    private WebRtcSessionManager webRtcSessionManager;

    private PermissionsChecker checker;


    //oncreate를 시작하기 전에 기본적인 환경설정을 위한 스테틱 변수이다.
    public static void start(Context context, boolean isRunForCall) {
        //Log.e("퀵3","1~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","1~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        Intent intent = new Intent(context, OpponentsActivity.class);
        //addFlags옵션은 안드로이드의 엑티비티 관리를 도와주는 함수이다..
        //안드로이드의 태스크는 하나의 스택에 액티비티가 올라간다고 보면 된다.
        //이 부분에서 Flag_ACTIVITY_new_task 는 새로운 태스크를 생성하고 그 위에 액티비티를 추가하는 옵션이다.
        //FLAG_ACTIVITY_RECORDED_TO_FRONT 는 호출하려는 Activity가 스택에 존재할 경우 최상위로 올려주는 옵션이다.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Consts.EXTRA_IS_STARTED_FOR_CALL, isRunForCall);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_opponents);

        //Log.e("퀵3","2~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","2[onCreate]~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        initFields();


        //이 부분은 BaseActivity에서 상속받은 함수이다.
        //BaseActivity 는 CorebaseActivity라는 quickblox에서 만든 엑티비티를 상속받는다.
        //Actionbar을 초기화하고 title이름등을 변경하는기능을 AppcompatActivity에 추가한 클래스이다.
        //이부분은 default로 액션바를 추가할수 있도록 구현된 함수이다.
        initDefaultActionBar();


        initUi();


        //이 부분은
        startLoadUsers();

        if (isRunForCall && webRtcSessionManager.getCurrentSession() != null) {
            CallActivity.start(OpponentsActivity.this, true);
        }

        checker = new PermissionsChecker(getApplicationContext());
    }




    // 현재 로컬 디비에 저장된 QBUser정보를 저장하는 변수인 currentUser을 초기화한다.
    // QBUser이 저장된 ArrayList를 가져오는 DBManager을 초기화한다.
    // WebRtcSession을 관리하는 webRtcSessionManager을 초기화한다.
    private void initFields() {

        //Log.e("퀵3","7~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","3~~~~~~~~~~~~~~~~~~~~~~~~~~~~");


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isRunForCall = extras.getBoolean(Consts.EXTRA_IS_STARTED_FOR_CALL);
        }

        currentUser = sharedPrefsHelper.getQbUser();
        dbManager = QbUsersDbManager.getInstance(getApplicationContext());
        webRtcSessionManager = WebRtcSessionManager.getInstance(getApplicationContext());
    }


    //리스트뷰를 초기화한다.
    private void initUi() {
        //Log.e("퀵3","9~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","4~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        opponentsListView = (ListView) findViewById(R.id.list_opponents);
    }

    //현재 접속된 유저의 정보를 확인한 뒤 dbManger을 통해 저장한다.

    private void startLoadUsers() {

        //Log.e("퀵3","8~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","5~~~~~~~~~~~~~~~~~~~~~~~~~~~~");


        showProgressDialog(R.string.dlg_loading_opponents);
        //SharedPrefsHelper는 SharedPreperence를 사용하기 쉽게 커스터마이징 한 클래스이다
        //이 부분을 이용해서 qb라는 로컬 데이터베이스에서 key값이 PREF_CURR,,인 값을 얻어온다.

        String currentRoomName = SharedPrefsHelper.getInstance().get(Consts.PREF_CURREN_ROOM_NAME);
        requestExecutor.loadUsersByTag(currentRoomName, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                hideProgressDialog();
                //dbManager는 QbUserDbManager의 인스턴스이다.
                //이 함수는 groupchatwebrtcDB라는 로컬데이터베이스에 User들의 정보를 저장한다.
                //방정보로 부터 유저의 정보를 얻어와서 데이터베이스에 저장한다.
                dbManager.saveAllUsers(result, true);
                initUsersList();
            }

            @Override
            public void onError(QBResponseException responseException) {
                hideProgressDialog();
                showErrorSnackbar(R.string.loading_users_error, responseException, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startLoadUsers();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {

        //Log.e("퀵3","3[onResume]~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","6[onResume]~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        super.onResume();
        initUsersList();
    }


    //초기화된 리스트뷰에
    private void initUsersList() {
        //Log.e("퀵3","11~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","7~~~~~~~~~~~~~~~~~~~~~~~~~~~~");


//      checking whether currentOpponentsList is actual, if yes - return
        if (currentOpponentsList != null) {

            //dbManager를 통해 현제 접속된 유저의 정보를 가지고 있는 ArrayList를 가져온다.
            ArrayList<QBUser> actualCurrentOpponentsList = dbManager.getAllUsers();

            //현재 저장된 QbUser의 정보를 삭제한다
            actualCurrentOpponentsList.remove(sharedPrefsHelper.getQbUser());



            if (isCurrentOpponentsListActual(actualCurrentOpponentsList)) {
                return;
            }
        }

        // Arraylist 현재유저 받아온유저 정보를 비교하고 둘이 같다면 굳이 UserList를 업데이트하지는 않는다
        // 하지만 다르다면 밑의 함수를 진행한다.
        // SHaredPreperencesHelper 데이터베이스에는 현재 유저의 정보만 저장되어 있다
        proceedInitUsersList();
    }

    private void proceedInitUsersList() {

        //Log.e("퀵3","12~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","8~~~~~~~~~~~~~~~~~~~~~~~~~~~~");


        currentOpponentsList = dbManager.getAllUsers();
        Log.d(TAG, "proceedInitUsersList currentOpponentsList= " + currentOpponentsList);


        //현재 저장된 QBUser은 삭제한다. 왜냐하면 자기 자신은 리스트뷰에 포함시키면 안되기 때문이다.
        currentOpponentsList.remove(sharedPrefsHelper.getQbUser());

        //어뎁터에 최종적으로 confirm된 currenopponetsList를 할당한다
        opponentsAdapter = new OpponentsAdapter(this, currentOpponentsList);
        opponentsAdapter.setSelectedItemsCountsChangedListener(new OpponentsAdapter.SelectedItemsCountsChangedListener() {
            @Override
            public void onCountSelectedItemsChanged(int count) {
                updateActionBar(count);
            }
        });

        //ListView에 어뎁터를 할당한다.
        opponentsListView.setAdapter(opponentsAdapter);


        //여기 부분에서 테스트를 진행한다.
        /*
        if(currentOpponentsList.size()>0) {

            QBUser item=(QBUser) currentOpponentsList.get(0);
            qbUserCustomClass info=(qbUserCustomClass) item.getCustomDataAsObject();

            Toast.makeText(getApplicationContext(),info.hobby,Toast.LENGTH_LONG).show();

        }
        */
        ///////////////////////////////////////////////////////////



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Log.e("퀵3","13[onCreateOption]~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","9[onCreateOption]~~~~~~~~~~~~~~~~~~~~~~~~~~~~");


        if (opponentsAdapter != null && !opponentsAdapter.getSelectedItems().isEmpty()) {
            getMenuInflater().inflate(R.menu.activity_selected_opponents, menu);
        } else {
            getMenuInflater().inflate(R.menu.activity_opponents, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }


    //actual은 실제의,정말의 라는 뜻이다
    //
    private boolean isCurrentOpponentsListActual(ArrayList<QBUser> actualCurrentOpponentsList) {

        //Log.e("퀵3","10~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","11~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        //실제 인터넷에서 가져온 QBUser정보를 가진 ArrayList가
        //retainAll 일종의 합집합의 역활로 동시에 저장된 객체들을 저장한다.
        //따라서 이부분은 현재 로컬에 저장된 QBUser와 인터넷에서 가져온 QBUser을 동기화해주는 부분이라고 볼 수 있다.
        //retainAll은

        boolean equalActual = actualCurrentOpponentsList.retainAll(currentOpponentsList);
        boolean equalCurrent = currentOpponentsList.retainAll(actualCurrentOpponentsList);
        return !equalActual && !equalCurrent;
    }


    ////////////////////////////////////이 위까지 로그인후 화면세팅///////////////////////////////




    ///리스트뷰 아이템 클릭시 => 12,14,9
    //다시 눌렀을시 => 12,9

    private void updateActionBar(int countSelectedUsers) {

        //Log.e("퀵3","20~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        Log.e("퀵3","12~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        if (countSelectedUsers < 1) {
            initDefaultActionBar();
        } else {
            removeActionbarSubTitle();
            initActionBarWithSelectedUsers(countSelectedUsers);
        }

        invalidateOptionsMenu();
    }


    private void initActionBarWithSelectedUsers(int countSelectedUsers) {


        //Log.e("퀵3","19~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","14~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        setActionBarTitle(String.format(getString(
                countSelectedUsers > 1
                        ? R.string.tile_many_users_selected
                        : R.string.title_one_user_selected),
                countSelectedUsers));
    }


    //통화 or 영상통화 눌렀을시 15,16,17


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Log.e("퀵3","14~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","15~~~~~~~~~~~~~~~~~~~~~~~~~~~~");


        int id = item.getItemId();

        switch (id) {
            case R.id.update_opponents_list:
                startLoadUsers();
                return true;

            case R.id.settings:
                showSettings();
                return true;

            case R.id.log_out:
                logOut();
                return true;

            case R.id.start_video_call:
                if (isLoggedInChat()) {
                    startCall(true);
                }
                if (checker.lacksPermissions(Consts.PERMISSIONS)) {
                    startPermissionsActivity(false);
                }
                return true;

            case R.id.start_audio_call:
                if (isLoggedInChat()) {
                    startCall(false);
                }
                if (checker.lacksPermissions(Consts.PERMISSIONS[1])) {
                    startPermissionsActivity(true);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isLoggedInChat() {

        //Log.e("퀵3","15~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","16~~~~~~~~~~~~~~~~~~~~~~~~~~~~");



        if (!QBChatService.getInstance().isLoggedIn()) {
            Toaster.shortToast(R.string.dlg_signal_error);
            tryReLoginToChat();
            return false;
        }

        return true;

    }


    private void startCall(boolean isVideoCall) {

        //Log.e("퀵3","18~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","17~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        if (opponentsAdapter.getSelectedItems().size() > Consts.MAX_OPPONENTS_COUNT) {
            Toaster.longToast(String.format(getString(R.string.error_max_opponents_count),
                    Consts.MAX_OPPONENTS_COUNT));
            return;
        }

        Log.d(TAG, "startCall()");
        ArrayList<Integer> opponentsList = CollectionsUtils.getIdsSelectedOpponents(opponentsAdapter.getSelectedItems());
        QBRTCTypes.QBConferenceType conferenceType = isVideoCall
                ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
                : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

        QBRTCClient qbrtcClient = QBRTCClient.getInstance(getApplicationContext());

        QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);

        WebRtcSessionManager.getInstance(this).setCurrentSession(newQbRtcSession);

        PushNotificationSender.sendPushMessage(opponentsList, currentUser.getFullName());

        CallActivity.start(this, false);
        Log.d(TAG, "conferenceType = " + conferenceType);
    }



    // Setting 버튼 눌렀을시 15,18

    private void showSettings() {
        //Log.e("퀵3","17~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","18~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        SettingsActivity.start(this);
    }




    // 로그아웃 버튼 눌렀을시 15,19,20,21,22,23


    private void logOut() {

        //Log.e("퀵3","21~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","19~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        unsubscribeFromPushes();
        startLogoutCommand();
        removeAllUserData();
        startLoginActivity();
    }

    private void unsubscribeFromPushes() {

        //Log.e("퀵3","23~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","20~~~~~~~~~~~~~~~~~~~~~~~~~~~~");


        SubscribeService.unSubscribeFromPushes(this);
    }

    private void startLogoutCommand() {

        //Log.e("퀵3","22~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","21~~~~~~~~~~~~~~~~~~~~~~~~~~~~");


        CallService.logout(this);
    }



    private void removeAllUserData() {

        //Log.e("퀵3","24~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","22~~~~~~~~~~~~~~~~~~~~~~~~~~~~");


        UsersUtils.removeUserData(getApplicationContext());
        requestExecutor.deleteCurrentUser(currentUser.getId(), new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                Log.d(TAG, "Current user was deleted from QB");
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "Current user wasn't deleted from QB " + e);
            }
        });
    }

    private void startLoginActivity() {

        //Log.e("퀵3","25~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","23~~~~~~~~~~~~~~~~~~~~~~~~~~~~");



        LoginActivity.start(this);
        finish();
    }





    @Override
    protected void onNewIntent(Intent intent) {

        //4444
        Log.e("퀵3","호출안됨");

        super.onNewIntent(intent);
        if (intent.getExtras() != null) {
            isRunForCall = intent.getExtras().getBoolean(Consts.EXTRA_IS_STARTED_FOR_CALL);
            if (isRunForCall && webRtcSessionManager.getCurrentSession() != null) {
                CallActivity.start(OpponentsActivity.this, true);
            }
        }
    }

    @Override
    protected View getSnackbarAnchorView() {

        //5555
        Log.e("퀵3","호출안됨");

        return findViewById(R.id.list_opponents);
    }

    private void startPermissionsActivity(boolean checkOnlyAudio) {

        //6666
        Log.e("퀵3","호출안됨");

        PermissionsActivity.startActivity(this, checkOnlyAudio, Consts.PERMISSIONS);
    }

    private void tryReLoginToChat() {

        //Log.e("퀵3","16~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.e("퀵3","호출안됨");


        if (sharedPrefsHelper.hasQbUser()) {
            QBUser qbUser = sharedPrefsHelper.getQbUser();
            CallService.start(this, qbUser);
        }
    }

}