package com.example.user.dailytv.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.user.dailytv.Activities.ArgumentRealityExampleActivity;
import com.example.user.dailytv.Activities.ExoPlayer2Activity;
import com.example.user.dailytv.Activities.ExoPlayerCusterActivity;
import com.example.user.dailytv.Activities.SimpleVrPanoramaActivity;
import com.example.user.dailytv.Activities.VideoPreviewActivity;
import com.example.user.dailytv.Activities.YaseaStreamActivity;
import com.example.user.dailytv.Adapter.ClusterListViewAdapter;
import com.example.user.dailytv.ListData.MarkerClusterItem;
import com.example.user.dailytv.ListData.Marker_ListData;
import com.example.user.dailytv.ListData.TV_ListData;
import com.example.user.dailytv.ListData.Video_ListData;
import com.example.user.dailytv.Module.GpsInfo;
import com.example.user.dailytv.Module.MultiDrawable;
import com.example.user.dailytv.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.quickblox.sample.groupchatwebrtc.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by user on 2018-01-07.
 */

public class Fragment_googlemap extends Fragment implements OnMapReadyCallback,ClusterManager.OnClusterClickListener<MarkerClusterItem>, GoogleMap.OnCameraMoveStartedListener,GoogleMap.OnMapClickListener ,ClusterManager.OnClusterItemClickListener{

    View view;

    GoogleMap m_googleMap;
    MapView mapView;

    final App.GlobalVariable global=App.getGlobal();

    //클러스터링 렌터링을 위한 변수이다.

    private ClusterManager<MarkerClusterItem> mClusterManager;
    private Random mRandom = new Random(1984);

    //클러스터링된 정보를 보여주는 리스트뷰의 Adapter이다.

    ClusterListViewAdapter mClusterAdapter;
    ListView mClusterListView;

    boolean isDragMap=false;

    GpsInfo gpsInfo;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //맵뷰 객체가 메모리에 존재한다면 다음을 실행한다.
        if(mapView!=null)
        {
            mapView.onCreate(savedInstanceState);
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.f_googlemap,null);

        gpsInfo=new GpsInfo(getActivity());

        mapView=(MapView)view.findViewById(R.id.googlemap);
        mapView.getMapAsync(this);


        InitClustering();




        return view;
    }


    private void InitClustering()
    {
        mClusterListView=view.findViewById(R.id.clusterListView);
        mClusterAdapter=new ClusterListViewAdapter(getActivity());
        mClusterListView.setAdapter(mClusterAdapter);
        mClusterListView.setVisibility(View.GONE);

        mClusterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MarkerClusterItem markerItem= (MarkerClusterItem) mClusterAdapter.getItem(position);

                if(markerItem.getType().equals("live"))
                {
                    SharedPreferences shared=getActivity().getSharedPreferences("logininfo", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor=shared.edit();
                    editor.remove("bjnickname");
                    editor.putString("bjnickname","펭귄");
                    editor.commit();

                    Intent intent=new Intent(getActivity(),ExoPlayer2Activity.class);


                    MarkerClusterItem item=(MarkerClusterItem) mClusterAdapter.getItem(position);

                    //이 변수는 스트리밍을 하는 스트리머에게 선물을 보내거나 추천 횟수를 알아오는데 쓰인다.
                    intent.putExtra("publisherid",item.publisherid);
                    Log.e("퍼블리셔아이디 추적","publisherid="+item.getPublisherid());
                    startActivity(intent);
                }

                else if(markerItem.getType().equals("vod"))
                {
                    Intent intent=new Intent(getActivity(),ExoPlayerCusterActivity.class);

                    intent.putExtra("videourl",markerItem.getVideourl());
                    startActivity(intent);

                }
            }
        });
    }

    @Override

    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        mapView.onSaveInstanceState(outState);

    }


    @Override

    public void onResume() {

        super.onResume();

        mapView.onResume();

    }

    @Override

    public void onPause() {

        super.onPause();

        mapView.onPause();

    }

    @Override

    public void onDestroy() {

        super.onDestroy();

        mapView.onDestroy();

    }

    @Override

    public void onLowMemory() {

        super.onLowMemory();

        mapView.onLowMemory();

    }


    //구글맵이 화면에 표시되기전에 마커작업등 지도에 추가해야할 작업들을 처리하는 함수이다.
    //클러스터링된 마커와 기존의 마커를 구별 할 수 있도록 플레그에 따라 작업 내용을 설정한다.

    @Override
    public void onMapReady(GoogleMap googleMap) {

        m_googleMap=googleMap;


        //맨처음 찍을 마커를 설정한다.
        ADmarkerJob(m_googleMap);

        m_googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {


                String adKey=marker.getTitle().substring(0,3);
                String landKey=marker.getTitle().substring(0,5);

                View v=getActivity().getLayoutInflater().inflate(R.layout.custom_marker,null);


                final TextView title=(TextView)v.findViewById(R.id.title);
                final ImageView image=(ImageView)v.findViewById(R.id.image);

                //마커 형식이 광고일때의 InfoWindow 커스텀 부분이다.
                if(adKey.equals("광고:"))
                {
                    String [] adToken=marker.getTitle().split(":");
                    title.setText(adToken[1]);

                    if(adToken[2].equals("cutlet"))
                    {
                        Drawable cutletDrawable=getResources().getDrawable(R.drawable.cutlet);
                        image.setImageDrawable(cutletDrawable);

                    }
                    else if(adToken[2].equals("caffebene"))
                    {
                        Drawable caffebeneDrawable=getResources().getDrawable(R.drawable.caffebene);
                        image.setImageDrawable(caffebeneDrawable);
                    }
                    else if(adToken[2].equals("marketcutlet"))
                    {
                        Drawable marketcutletDrawable=getResources().getDrawable(R.drawable.marketcutlet);
                        image.setImageDrawable(marketcutletDrawable);
                    }
                    else if(adToken[2].equals("snow"))
                    {
                        Drawable snowDrawable=getResources().getDrawable(R.drawable.snow);
                        image.setImageDrawable(snowDrawable);

                    }

                    return v;
                }


                //마커 형식이 랜드마크일때의 InfoWindow 커스텀 부분이다.
                if(landKey.equals("랜드마크:"))
                {

                    String [] adToken=marker.getTitle().split(":");
                    title.setText(adToken[1]);

                    if(adToken[2].equals("gwanghwamun"))
                    {
                        Drawable cutletDrawable=getResources().getDrawable(R.drawable.gwanghwamun);
                        image.setImageDrawable(cutletDrawable);

                    }
                    else if(adToken[2].equals("cheonggye"))
                    {
                        Drawable caffebeneDrawable=getResources().getDrawable(R.drawable.cheonggye);
                        image.setImageDrawable(caffebeneDrawable);
                    }


                    return v;
                }

                //현재 나의 위치입니다. 라는 title이 있다면 기본 마커를 표시한다.
                if(marker.getTitle().equals("현재 나의 위치입니다."))
                {
                    return null;
                }

                //만약
                if(!adKey.equals("광고:")&&!landKey.equals("랜드마크:"))
                {

                    View v2=getActivity().getLayoutInflater().inflate(R.layout.custom_marker2,null);
                    return v2;
                }


                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });

        m_googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                Log.e("Fragment_googlemap","인포윈도우 클릭");

                String titleKey=marker.getTitle();

                Log.e("Fragment_googlemap","인포윈도우 title: "+titleKey);

                //현재 위도와 경도를 길안내 Activity로 보내기 위한 변수이다.
                final LatLng myLatLng=marker.getPosition();



                if(titleKey.contains("광고:")||titleKey.contains("랜드마크:"))

                {

                    Log.e("Fragment_googlemap","인포윈도우 클릭222222");



                    Intent intent=new Intent(getActivity(), SimpleVrPanoramaActivity.class);
                    String []adLandToken=titleKey.split(":");
                    intent.putExtra("imagename",adLandToken[2]);

                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(getActivity(),"clusterItem클릭",Toast.LENGTH_LONG).show();

                    AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(getActivity());
                    dialogBuilder.setTitle("길안내 대화상자");
                    dialogBuilder.setMessage("해당 위치로 도보 경로 안내를 시작할까요?");
                    dialogBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    });
                    dialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {

                            Intent intent=new Intent(getActivity(), ArgumentRealityExampleActivity.class);

                            intent.putExtra("lat",myLatLng.latitude+"");
                            intent.putExtra("lng",myLatLng.longitude+"");



                            startActivity(intent);

                        }
                    });
                    dialogBuilder.show();
                }

            }
        });


        //현재 서버에 있는 방송 목록의 값을 가져와서 googlemap에 insert 한다.
        new MapMarkerGetTask(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    //광고 마커를 삽입하는 작업을 수행한다.
    private void ADmarkerJob(GoogleMap googleMap)
    {
        LatLng currenLatLng=new LatLng(gpsInfo.getLatitude(),gpsInfo.getLongitude());

        //광고의 위도 경도를 표시한다.
        //LatLng cutletLatLng=new LatLng(37.534805, 126.994558);

        LatLng cafebenaLatLng=new LatLng(37.582982,127.001409);
        LatLng marketcutletLatLng=new LatLng(37.4830135,126.9737894);
        LatLng snowLatLng=new LatLng(37.5485027,126.9207486);

        //랜드마크의 위도 경도를 표시한다. cheonggye gwanghwamun
        LatLng gwanghwamunLatLng=new LatLng(37.5705517,126.9769254);
        LatLng cheonggyeLatLng=new LatLng(37.5689931,126.9799687);


        //구글맵에 표시할 마커에대한 옵션을 설정한다.

        MarkerOptions markerOptionsCutlet=new MarkerOptions();
        markerOptionsCutlet.position(currenLatLng).title("현재 나의 위치입니다.");
        googleMap.addMarker(markerOptionsCutlet);


        MarkerOptions markerOptionsCafebene=new MarkerOptions();
        markerOptionsCafebene.position(cafebenaLatLng).title("광고:카페베네 광고입니ㄹ다.:caffebene");
        googleMap.addMarker(markerOptionsCafebene);

        MarkerOptions markerOptionsMarketcutlet=new MarkerOptions();
        markerOptionsMarketcutlet.position(marketcutletLatLng).title("광고:장터돈까스 광고입니다.:marketcutlet");
        googleMap.addMarker(markerOptionsMarketcutlet);

        MarkerOptions markerOptionsSnow=new MarkerOptions();
        markerOptionsSnow.position(snowLatLng).title("광고:설빙(홍대) 광고입니다.:snow");
        googleMap.addMarker(markerOptionsSnow);

        MarkerOptions markerOptionsLandmark1 =new MarkerOptions();
        markerOptionsLandmark1.position(gwanghwamunLatLng).title("랜드마크:광화문광장[랜드마크]:gwanghwamun");
        googleMap.addMarker(markerOptionsLandmark1);

        MarkerOptions markerOptionsLandmark2=new MarkerOptions();
        markerOptionsLandmark2.position(cheonggyeLatLng).title("랜드마크:청계광장[랜드마크]:cheonggye");
        googleMap.addMarker(markerOptionsLandmark2);



        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currenLatLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));


    }



    public class MapMarkerGetTask extends AsyncTask<Void,Void,String> {

        //String password -> 만약에 비밀방 설정할떄 .. 이부분은 나중에 추가

        String userid = "user1"; //userid는 user1으로 통일
        String result;

        Context m_context;

        public MapMarkerGetTask(Context context)
        {
            m_context=context;
        }

        ArrayList <MarkerClusterItem> markerArray =new ArrayList<MarkerClusterItem>();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            RequestBody body = new FormBody.Builder()
                    //.add("userid",userid)
                    .build();

            Request request = new Request.Builder()
                    .url("http://" + global.getIP() + "/select_marker.php")
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();

            } catch (Exception e) {
                Log.e("Fragment_googlemap", "googlemarker network error" + e.getMessage());
            }
            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            Log.e("googleMap","2222222222");
            Log.e("Fragment_googlemap", " :결과값 출력" + result);

            initMapInfo(result);


        }

        // 1. 서버에서 가져온 Json값을 MapClusterItem에 저장한다.
        // 2. Glide로 가져온 Bitmap값을 Drawable로 변환하여 MapClusterItem에 저장한다.
        private void initMapInfo(String result)
        {
            try {
                final JSONObject jsonobj = new JSONObject(result);
                final JSONArray markerInfoArray = jsonobj.getJSONArray("result");


                for (int i = 0; i < markerInfoArray.length(); i++) {

                    final JSONObject markerJson = markerInfoArray.getJSONObject(i);

                    final String type= markerJson.getString("type");
                    final String title= markerJson.getString("title");
                    final String nickname= markerJson.getString("nickname");
                    final String imagepath= markerJson.getString("imagepath");
                    final String lat= markerJson.getString("lat");
                    final String lng= markerJson.getString("lng");
                    final String viewernumber= markerJson.getString("viewernumber");
                    final String publisherid= markerJson.getString("publisherid");

                    String videourl_= markerJson.getString("videourl");

                    final String videourl="http://"+global.getIP()+"/videos/"+videourl_;

                    final String longdate=markerJson.getString("longdate");

                    Marker_ListData markerItem=new Marker_ListData();
                    markerItem.type=type;
                    markerItem.title=title;
                    markerItem.nickname=nickname;
                    markerItem.imagepath=imagepath;
                    markerItem.lat=lat;
                    markerItem.lng=lng;
                    markerItem.viwernumber=viewernumber;
                    markerItem.publisherid=publisherid;
                    markerItem.videourl=videourl;
                    markerItem.longdate=longdate;


                    //마커 클러스터가 구현된 객체를 저장하는 ArrayList에 값을 추가한다.

                    final LatLng latlng=new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));

                    String imageUri="";

                    //Live방송인지 Vod인지에 따라 서버에서 가져올 이미지의 경로를 다르게 설정한다..
                    if(type.equals("live"))
                    {
                        imageUri="http://"+global.getIP()+imagepath;
                    }
                    else if(type.equals("vod"))
                    {
                        imageUri="http://"+global.getIP()+"/videos/"+imagepath;
                    }


                    final int setGoogleMapFlag= i;
                    Glide.with(getActivity()).asBitmap().load(imageUri)
                            .into(new SimpleTarget<Bitmap>()
                            {
                                @Override
                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                                    Drawable drawableImage_cluster=new BitmapDrawable(getResources(),resource);
                                    MarkerClusterItem markerClusterItem=new MarkerClusterItem(latlng,title,drawableImage_cluster);

                                    Drawable drawableImage_listview=new BitmapDrawable(getResources(),resource);
                                    markerClusterItem.setImageDrawable_listview(drawableImage_listview);

                                    markerClusterItem.setType(type);
                                    markerClusterItem.setNickname(nickname);
                                    markerClusterItem.setPublisherid(publisherid);
                                    markerClusterItem.setVideourl(videourl);
                                    markerClusterItem.setViwernumber(viewernumber);
                                    markerClusterItem.setLongdate(longdate);




                                    markerArray.add(markerClusterItem);


                                    Log.e("Fragment_googlemap","Drawable값 입력완료11111111");


                                    //비동기로 Bitamp값을 모두 변환한 후에 클러스터링에 대한 설정 작업을 시작한다.
                                    if(setGoogleMapFlag==markerInfoArray.length()-1) {
                                        setGoogleMapClustering();
                                    }
                                }
                            });
                }

            } catch (JSONException e) {
                Log.e("[Tabviewactivity]JSON에러", e.getMessage());
            } catch (NullPointerException e)
            {
                Log.e("Fragment_googlemap","null 에러 발생"+e.getMessage());
            }




        }

        // 구글맵에대한 클러스터링 작업을 설정하는 함수이다.
        private void setGoogleMapClustering()
        {

            Log.e("Fragment_googlemap","Drawable값 입력 후 setGoogleMapClustering 호출");



            mClusterManager =new ClusterManager<MarkerClusterItem>(getActivity(),m_googleMap);
            mClusterManager.setRenderer(new PersonRenderer());

            m_googleMap.setOnCameraIdleListener(mClusterManager);
            m_googleMap.setOnMarkerClickListener(mClusterManager);

            //m_googleMap.setOnInfoWindowClickListener(mClusterManager);

            m_googleMap.setOnCameraMoveStartedListener(Fragment_googlemap.this);
            m_googleMap.setOnMapClickListener(Fragment_googlemap.this);





            mClusterManager.setOnClusterClickListener(Fragment_googlemap.this);
            mClusterManager.setOnClusterItemClickListener(Fragment_googlemap.this);


            //mClusterManager.setOnClusterInfoWindowClickListener(this);
            //mClusterManager.setOnClusterItemInfoWindowClickListener(this);

            //markerClusterItem에서 각 항목의 값을 마커에 추가한다.
            for(int i=0;i<markerArray.size();i++) {


                mClusterManager.addItem(markerArray.get(i));

            }

            mClusterManager.cluster();
        }


    }

    //마커를 랜더링하는 클래스


    private class PersonRenderer extends DefaultClusterRenderer<MarkerClusterItem> {
        private final IconGenerator mIconGenerator = new IconGenerator(getActivity());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getActivity());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PersonRenderer() {
            super(getActivity(), m_googleMap, mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getActivity());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }


        // 레이아웃 랜더링 전에 클러스터링할 아이템의 레이아웃을 초기화한다
        @Override
        protected void onBeforeClusterItemRendered(MarkerClusterItem clusterItem, MarkerOptions markerOptions) {

            // Draw a single person.
            // Set the info window to show their name.

            mImageView.setImageDrawable(clusterItem.getImageDrawable_cluster());
            //mImageView.setImageResource(person.profilePhoto);

            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(clusterItem.getTitle());

        }

        // 레이아웃 렌더링 후 클러스터링 전에 해야할 작업 목록이다.

        @Override
        protected void onBeforeClusterRendered(Cluster<MarkerClusterItem> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).

            //1. 한 지정된 공간의 cluster된 아이템들의 갯수중에 최대 4개의 사진파일을 Drawable로 저장한다.
            //2. Drawable의 가로와 세로의 사이즈를 지정한다.
            //3. setBounds함수로 이미지의 크기를 조정한다.

            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;



            for (MarkerClusterItem p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                //Drawable drawable = getResources().getDrawable(p.profilePhoto);


                Drawable drawable = p.getImageDrawable_cluster();

                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));



        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }


    private void addItems() {

        // http://www.flickr.com/photos/usnationalarchives/4726892651/
        //mClusterManager.addItem(new Person(position(), "Teach", R.drawable.teacher));
    }





    @Override
    public boolean onClusterClick(Cluster<MarkerClusterItem> cluster) {

        // 클러스터가 클릭되면 토스트 메세지를 출력시킨다.
        // Show a toast with some info when the cluster is clicked.

        //String firstName = cluster.getItems().iterator().next().name;
        Toast.makeText(getActivity(), "클러스터 클릭", Toast.LENGTH_SHORT).show();


        // 클러스터를 확대하십시오. LatLngBounds를 만들고 경계 안에 모든 클러스터 항목을 포함시킨 다음
        // 경계의 중심에 애니메이션을 적용해야합니다.
        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // 경계에 대한 모든 필수 클러스터 항목을 수집하도록 빌더를 작성하십시오.
        // Create the builder to collect all essential cluster items for the bounds.

        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            mClusterListView.setVisibility(View.VISIBLE);
            isDragMap=false;
            m_googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));

        } catch (Exception e) {
            e.printStackTrace();
        }




        // 1. 현재 선택된 마커들에대한 정보를 가지고있는 MarkerClusterItem 배열을 ClusterListViewAdapter로 넘겨준다.

        ArrayList <MarkerClusterItem> markerClusterItems=new ArrayList<>();

        for(ClusterItem item: cluster.getItems())
        {
            markerClusterItems.add((MarkerClusterItem)item);
        }


        mClusterAdapter.removeAll();
        mClusterAdapter.addItem(markerClusterItems);
        mClusterAdapter.notifyDataSetChanged();


        return true;
    }


    @Override
    public void onCameraMoveStarted(int i) {

        Log.e("Fragment_googlemap","카메라 이동 시작전 호출되는 메서드 isDragMap="+isDragMap);

        // 손으로 드래그 하게되면 true가 호출되어 리스트뷰가 안보이게된다.
        // 여러개의 아이템을 가진 클러스터를 누르게되면 false가 호출되어 리스트뷰가 사라지지 않게된다.
        // 하지만 한개의 아이템을 가진 클러스터를 누르게 되면 처음에는 false[startAnimation] 두번쨰는 true가 호출되어 보였다가 마지막에 사라지게 된다.
        // 만약 아이템의 갯수가 1개라면

        if(isDragMap)
        {
            //mClusterListView.setVisibility(View.GONE);
        }

        isDragMap=true;


    }

    @Override
    public void onMapClick(LatLng latLng) {
        Toast.makeText(getActivity(),"맵 클릭",Toast.LENGTH_LONG).show();
        mClusterListView.setVisibility(View.GONE);
    }

    @Override
    public boolean onClusterItemClick(ClusterItem clusterItem) {



        Toast.makeText(getActivity(),"클러스터 아이템 클릭",Toast.LENGTH_LONG).show();
        ArrayList <MarkerClusterItem> markerClusterItems=new ArrayList<>();

        markerClusterItems.add((MarkerClusterItem)clusterItem);

        try {

            Log.e("Fragment_googlemap","클러스터 아이템에서 애니메이션 이동 시작");

            mClusterListView.setVisibility(View.VISIBLE);
            isDragMap=false;
            m_googleMap.animateCamera(CameraUpdateFactory.newLatLng(((MarkerClusterItem) clusterItem).getLatlng()));

        } catch (Exception e) {
            e.printStackTrace();
        }


        mClusterAdapter.removeAll();
        mClusterAdapter.addItem(markerClusterItems);
        mClusterAdapter.notifyDataSetChanged();

        //이부분 true일때와 false일때의 차이 알아보자.
        return false;
    }
}
