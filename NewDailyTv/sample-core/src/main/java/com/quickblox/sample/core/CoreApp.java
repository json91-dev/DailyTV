package com.quickblox.sample.core;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.auth.session.QBSession;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.auth.session.QBSessionParameters;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.ServiceZone;
import com.quickblox.sample.core.models.QbConfigs;
import com.quickblox.sample.core.utils.configs.CoreConfigUtils;


    public class CoreApp extends Application {

    //어떠한 태그가 필요하다..
    public static final String TAG = CoreApp.class.getSimpleName();

    // 코어앱의 인스턴스를 얻는다..(??)
    private static CoreApp instance;

    // 어떤 json 파일이 필요하다.
    private static final String QB_CONFIG_DEFAULT_FILE_NAME = "qb_config.json";

    //QbConfigs 가 필요하다. quickblox의 설정파일일 확률이 높다.
    private QbConfigs qbConfigs;

    @Override
    public void onCreate() {
        super.onCreate();
        //Coreapp=Coreapp
        instance = this;

        //quickblox의 연결 매니저와 설정파일과 권한(?) 설정 매니저를 초기화한다.
        initQBSessionManager();
        initQbConfigs();
        initCredentials();
    }

    //설정파일 초기화
    private void initQbConfigs() {
        Log.e(TAG, "QB CONFIG FILE NAME: " + getQbConfigFileName());
        qbConfigs = CoreConfigUtils.getCoreConfigsOrNull(getQbConfigFileName());
    }

    public static synchronized CoreApp getInstance() {
        return instance;
    }



    public void initCredentials(){
        if (qbConfigs != null) {
            //설정파일 세팅 +계정 키값 입력
            QBSettings.getInstance().init(getApplicationContext(), qbConfigs.getAppId(), qbConfigs.getAuthKey(), qbConfigs.getAuthSecret());
            QBSettings.getInstance().setAccountKey(qbConfigs.getAccountKey());

            if (!TextUtils.isEmpty(qbConfigs.getApiDomain()) && !TextUtils.isEmpty(qbConfigs.getChatDomain())) {
                QBSettings.getInstance().setEndpoints(qbConfigs.getApiDomain(), qbConfigs.getChatDomain(), ServiceZone.PRODUCTION);
                QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
            }
        }
    }

    public QbConfigs getQbConfigs(){
        return qbConfigs;
    }

    protected String getQbConfigFileName(){
        return QB_CONFIG_DEFAULT_FILE_NAME;
    }


    //세션 연결 성공시, 실패시 업데이트시 Log에 저장되도록 설정한 부분
    private void initQBSessionManager() {
        QBSessionManager.getInstance().addListener(new QBSessionManager.QBSessionListener() {
            @Override
            public void onSessionCreated(QBSession qbSession) {
                Log.d(TAG, "Session Created");
            }

            @Override
            public void onSessionUpdated(QBSessionParameters qbSessionParameters) {
                Log.d(TAG, "Session Updated");
            }

            @Override
            public void onSessionDeleted() {
                Log.d(TAG, "Session Deleted");
            }

            @Override
            public void onSessionRestored(QBSession qbSession) {
                Log.d(TAG, "Session Restored");
            }

            @Override
            public void onSessionExpired() {
                Log.d(TAG, "Session Expired");
            }

            @Override
            public void onProviderSessionExpired(String provider) {
                Log.d(TAG, "Session Expired for provider:" + provider);
            }
        });
    }
}