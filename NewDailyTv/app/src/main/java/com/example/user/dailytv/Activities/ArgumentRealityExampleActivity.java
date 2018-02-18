package com.example.user.dailytv.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.user.dailytv.Module2.DrawSurfaceView;
import com.example.user.dailytv.Module2.LocationUtils;
import com.example.user.dailytv.Module2.Point;
import com.example.user.dailytv.R;
import com.google.gson.JsonObject;
import com.skt.Tmap.TMapTapi;

import org.jivesoftware.smack.util.Async;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by user on 2018-01-14.
 */

public class ArgumentRealityExampleActivity extends Activity{


    private static final String TAG = "Compass";
    private static boolean DEBUG = false;
    private SensorManager mSensorManager;
    //최신 api에서 센서정보를 얻어오는 변수이다.
    Sensor accelerometer;
    Sensor magnetometer;


    private Sensor mSensor;
    private DrawSurfaceView mDrawView;
    LocationManager locMgr;

    Location currentLocation;



    //tmap 호출 이후 drawSurfaceView로 Location값을 넘겨줘야 되기 떄문에 다음 플레그를 이용한다.
    boolean isOkayGetRoute=false;


    String endLat,endLng;

    LocationListener mLocationListner;






    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);


        //Fragment_googlemap에서 목적지에 대한 위도 경도를 받아와서 지정 변수에 입력시킨다.
        Intent endIntent=getIntent();
        endLat=endIntent.getExtras().getString("lat");
        endLng=endIntent.getExtras().getString("lng");



        InitTmapApi();

        InitCompassSensor();

        Log.e("네비게이션 순서","111111111111111");


    }

    private void InitTmapApi()
    {
        TMapTapi tmaptapi = new TMapTapi(this);
        tmaptapi.setSKPMapAuthentication ("f33cf743-4f85-4a42-9ffa-5ae8622c5897");
    }

    private void InitCompassSensor()
    {

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        //  센서를 초기화한다.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        // 가속도계 센서를 얻어온다.
        // 중력가속도를 측정하는 센서이다.
        // 중력방향에 있을때 9.8m/s2을 출력한다.
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 자기장 센서로 지구자기장을 통해 동서남북을 측정한다.
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        setContentView(R.layout.a_argumentrealityexample);

        // SurfaceView를 초기화한다.
        mDrawView = (DrawSurfaceView) findViewById(R.id.drawSurfaceView);

        // LocationManager 초기화
        locMgr = (LocationManager) this.getSystemService(LOCATION_SERVICE); // <2>


        // LocationProvider 초기화
        LocationProvider high = locMgr.getProvider(locMgr.getBestProvider(
                LocationUtils.createFineCriteria(), true));



        mLocationListner=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // do something here to save this new location
                Log.d(TAG, "Location Changed : 위도 : "+location.getLatitude()+"경도 : "+location.getLongitude());

                //Log.e("네비게이션 순서","22222222222222222222");

                //현재 자신의 위치에 대한 위도경도를 surfaceview로 보내준다.

                if(isOkayGetRoute) {
                    mDrawView.setMyLocation(location.getLatitude(), location.getLongitude());
                    mDrawView.invalidate();
                }
                else
                {
                    currentLocation=new Location(location);
                    new GetRouteFromTmap().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // using high accuracy provider... to listen for updates
        locMgr.requestLocationUpdates(high.getName(), 0, 0f,mLocationListner);




    }

    private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (DEBUG)
                Log.d(TAG, "sensorChanged (" + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ")");
            if (mDrawView != null) {
                mDrawView.setOffset(event.values[0]);
                mDrawView.invalidate();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    @Override
    protected void onResume() {
        if (DEBUG)
            Log.d(TAG, "onResume");
        super.onResume();

        mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME);

        //mSensorManager.registerListener(mListener,magnetometer,SensorManager.SENSOR_DELAY_UI);
        //mSensorManager.registerListener(mListener,accelerometer,SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("ArgementActivity","액티비티 onDestroy호출");
        locMgr.removeUpdates(mLocationListner);
        locMgr=null;
    }

    @Override
    protected void onStop() {
        if (DEBUG)
            Log.d(TAG, "onStop");
        mSensorManager.unregisterListener(mListener);
        super.onStop();
    }


    private class GetRouteFromTmap extends AsyncTask<Void,Void,String>
    {

        // 각 경로 Point의 lat lng description을 저장하는 Hash를 관리하는 ArrayList를 선언한다.
        ArrayList <Point> tmapPointArrayList=new ArrayList<>();


        @Override
        protected String doInBackground(Void... voids) {


            // google과 다르게 x가 longitude y가 latitude이다.

            Log.e("네비게이션 순서","333333333333333");


            //내 위치에서
            final String startX=currentLocation.getLongitude()+"";
            final String startY=currentLocation.getLatitude()+"";

            Log.e("GetRouteFromTmap","startX"+startX);
            Log.e("GetRouteFromTmap","startY"+startY);


            //문덕 초등학교 37.484513, 127.127562
            // 카페베네 37.482386, 127.126084
            // 지오커피 37.482657, 127.124630
            // 지오커피2 37.482753, 127.124628

            //final String endX="127.124628";
            //final String endY="37.482753";

            final String endX=endLng;
            final String endY=endLat;




            final String appKey="f33cf743-4f85-4a42-9ffa-5ae8622c5897";

            //startName=출발지 endName=도착지 =>UTF8형식으로 입력한다.
            String TmapRouteGetURL="https://api2.sktelecom.com/tmap/routes/pedestrian?version=1&startX="+startX+"&startY="+startY+"&endX="+endX+"&endY="+endY+"&startName=%EC%B6%9C%EB%B0%9C%EC%A7%80&endName=%EB%8F%84%EC%B0%A9%EC%A7%80&appKey="+appKey;


            OkHttpClient client=new OkHttpClient();

            RequestBody body=new FormBody.Builder()
                    .build();

            Request request=new Request.Builder()
                    .url(TmapRouteGetURL)
                    .post(body)
                    .build();

            try {
                Response response=client.newCall(request).execute();
                return response.body().string();

            }catch (Exception e)
            {
                Log.e("ArgumentReality","tmapError : "+e.getMessage());
            }
            return null;
        }


        // Tmap에서 받아온 json값을 파싱하여 저장한다.
        // 저장할 값은
        // 1.각 지점의 위도와 경도정보  2.각 지점의 간단한 description 을 받아온다.(완료)

        // 현재위치에서 첫번째 지점까지의 방향을 SurfaceView에 표시한다.
        // 현재 위치 정보를 계속 받아오고 첫번쨰 지점까지의 거리를 계속 받아와서 SurfaceView에 표시한다.
        // 첫번째 지점 근처에 도착하게되면 두번째 지점까지의 방향을 SurfaceView에 표시한다.
        // 위의 작업을 반복한다.
        // 마지막 지점에 도착하게되면 도착했다는 메세지를 반환한다.

        @Override
        protected void onPostExecute(String jsonString) {

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray FeaturesJsonArray=new JSONArray(jsonObject.getString("features"));




                for(int i=0;i<FeaturesJsonArray.length();i++)
                {
                    JSONObject featuresJsonObject=FeaturesJsonArray.getJSONObject(i);

                    JSONObject geometryJson=featuresJsonObject.getJSONObject("geometry");


                    String geometryType=geometryJson.getString("type");

                    // 1. 서버에서 coordinates값이 Point일때 [127.xxxxxxx,36.xxxxxxx]형식으로 들어온다.
                    // 2. 위의 값에서 longitude와 latitude를 추출해서 경로를 저장하는 Hashmap값에 저장한뒤
                    //     Hashmap을 관리하는 ArrayList로 add시킨다.
                    if(geometryType.equals("Point"))
                    {
                        HashMap<String,String> hashRouteItem=new HashMap<>();

                        String latlngString=geometryJson.getString("coordinates");
                        latlngString.replaceAll(" ","");

                        //위도와 경도를 입력한다.
                        String longitude=latlngString.substring(1,latlngString.indexOf(","));
                        String latitude=latlngString.substring(latlngString.indexOf(",")+1,latlngString.length()-1);

                        hashRouteItem.put("lat",latitude);
                        hashRouteItem.put("lng",longitude);

                        //설명을 입력한다.
                        JSONObject propertiesJsonObject=featuresJsonObject.getJSONObject("properties");
                        String description=propertiesJsonObject.getString("description");
                        hashRouteItem.put("des",description);


                        Log.e("tmap 입력값","lat ="+latitude);
                        Log.e("tmap 입력값","lng ="+longitude);
                        Log.e("tmap 입력값","des ="+description);

                        Point pointItem=new Point(Double.parseDouble(latitude),Double.parseDouble(longitude),description);
                        tmapPointArrayList.add(pointItem);




                    }else if(geometryType.equals("LineString"))
                    {

                    }
                }



            }catch (JSONException e)
            {
                Log.e("Tmap json 에러 : ",e.getMessage());
            }

            //여기서 SurfaceView에 방향과 description을 설정한다.


            mDrawView.setTmapPointArrayList(tmapPointArrayList);

            isOkayGetRoute=true;
            super.onPostExecute(jsonString);
        }
    }



}
