package com.example.user.dailytv.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.annotation.TargetApi;


import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.dailytv.ListData.MASK_ListData;
import com.example.user.dailytv.ListData.TV_ListData;
import com.example.user.dailytv.Module.BitmapModule;
import com.example.user.dailytv.Module.BitmapModule2;
import com.example.user.dailytv.R;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class OpencvActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;


    //JNI 관련 함수
    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);
    public static native long loadCascade(String cascadeFileName );
    public static native void detect(long cascadeClassifier_face, long cascadeClassifier_eye, long matAddrInput, long matAddrResult,String maskname,double sizeratio,double heightratio);

    //openCV 관련 변수

    private Mat matInput;
    private Mat matResult;
    public long cascadeClassifier_face = 0;
    public long cascadeClassifier_eye = 0;
    boolean CameraFrontDirection=true;


    String maskname="xbutton";


    //DB부분

    SharedPreferences shared;
    double sizeratio=1,heightratio=0;



    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    /////////마스크 이미지 부분/////////

    ListView masklistview;
    MASK_adapter maskadapter;

    /////////카메라 부분/////////////
    ImageButton camerabutton;
    boolean camerabuttonclicked=false;

    Bitmap outputbitmap;

    ///////////Seekbar설정 부분///////////////////


    SeekBar sizeseekbar,heightseekbar;

    //////////////resizeflag부분////////////////


    boolean resizedflag=true;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //안드로이드 화면에 대한 설정..
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //타이틀바 없애기

        requestWindowFeature(Window.FEATURE_NO_TITLE);


        setContentView(R.layout.opencvactivity);


        //휴대폰의 앱 버전이 스튜디오의 빌드 버전보다 높다면
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {
                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            } else  read_cascade_file();
        }
        else  read_cascade_file();

        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);


        //이부분 클릭 이벤트 만들것//
        mOpenCvCameraView.setCameraIndex(1);
        //mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)


        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);



        ////////////////마스크 부분 //////////////////////////////



        masklistview=(ListView)findViewById(R.id.masklistview);
        masklistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MASK_ListData item=(MASK_ListData) maskadapter.getItem(position);
                String mask=item.maskname;

                if(mask.equals("xbutton"))
                {
                    //외부의 mask이름
                    maskname="xbutton";
                }
                else {
                    read_mask_file(mask);
                }
            }
        });

        maskadapter=new MASK_adapter(getApplicationContext());
        maskadapter.addItem(ResourcesCompat.getDrawable(getResources(),R.drawable.glassmask,null),"glassmask.png");
        maskadapter.addItem(ResourcesCompat.getDrawable(getResources(),R.drawable.tigermask,null),"tigermask.png");
        maskadapter.addItem(ResourcesCompat.getDrawable(getResources(),R.drawable.captinmask,null),"captinmask.png");
        maskadapter.addItem(ResourcesCompat.getDrawable(getResources(),R.drawable.ironmask,null),"ironmask.png");
        maskadapter.addItem(ResourcesCompat.getDrawable(getResources(),R.drawable.betmanmask,null),"betmanmask.png");
        maskadapter.addItem(ResourcesCompat.getDrawable(getResources(),R.drawable.xbutton,null),"xbutton");



        masklistview.setAdapter(maskadapter);



        ////////////////////////////////카메라 버튼 클릭 이벤트//////////////////////////

        camerabutton=(ImageButton)findViewById(R.id.camerabutton);
        camerabutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camerabuttonclicked=true;
            }
        });

        shared=getSharedPreferences("logininfo", Activity.MODE_PRIVATE);


        //Seekbar이벤트 설정

        sizeseekbar=(SeekBar)findViewById(R.id.sizeseekbar);
        sizeseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                sizeratio=(seekBar.getProgress()+70)/100.0;

            }
        });

        heightseekbar=(SeekBar)findViewById(R.id.heightseekbar);
        heightseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {



            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                heightratio=(double)(150-seekBar.getProgress());
            }
        });



    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        matInput = inputFrame.rgba();




        //Mat행렬의 메모리 해제
        if ( matResult != null ) matResult.release();

        //해상도 가로,세로,타입
        matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());

        //ConvertRGBtoGray(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());

        //전면카메라일 경우 이부분 실행시켜줘야함
        Core.flip(matInput, matInput, 1);


        // 2. detect함수는 항상 켜진 상태로 값을 받아들인다..
        detect(cascadeClassifier_face,cascadeClassifier_eye, matInput.getNativeObjAddr(),
                matResult.getNativeObjAddr(),maskname,sizeratio,heightratio);

        if(camerabuttonclicked)
        {
            //Frame(MAT)값을 bitmap값으로 변환하는 부분.
            outputbitmap=Bitmap.createBitmap(matResult.cols(),matResult.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(matResult,outputbitmap);

            //Bitmap값을 png파일로 변환하고 경로를 intent로 넘겨주는 부분
            Intent intent=new Intent(getApplicationContext(),OpencvResultActivity.class);


            String pngfilepath= BitmapModule2.saveBitmapToPng(getApplicationContext(),outputbitmap,System.currentTimeMillis()+"");
            intent.putExtra("pngfilepath",pngfilepath);

            startActivityForResult(intent,1001);

            camerabuttonclicked=false;

        }



        resizedflag=true;

        return matResult;


    }



    //여기서부턴 퍼미션 관련 메소드
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};


    private boolean hasPermissions(String[] permissions) {
        int result;

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){

            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션 발견
                return false;
            }
        }

        //모든 퍼미션이 허가되었음
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        switch(requestCode){

            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {

                    //0일떄가 권한이 주어진 것이고 1일때가 권한이 주어진 것이 아니다.

                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

                    boolean writePermissionAccepted = grantResults[1]
                            == PackageManager.PERMISSION_GRANTED;

                    if(!cameraPermissionAccepted || !writePermissionAccepted){
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                    }else
                    {
                        read_cascade_file();
                    }

               }
                break;
        }
    }



    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder( OpencvActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }

    //////////////////////////////////OpenCV Asset에서 xml파일 가져오기 ////////////////////////






    ////////////////////////////////xml 파일을 가져오기 위한 함수//////////////

    private void read_cascade_file(){
        //copyFile 메소드는 Assets에서 해당 파일을 가져와
        //외부 저장소 특정위치에 저장하도록 구현된 메소드입니다.
        copyFile("haarcascade_frontalface_alt.xml");
        copyFile("haarcascade_eye_tree_eyeglasses.xml");

        Log.d(TAG, "read_cascade_file:");


        //loadCascade 메소드는 외부 저장소의 특정 위치에서 해당 파일을 읽어와서
        //CascadeClassifier 객체로 로드합니다.
        cascadeClassifier_face = loadCascade( "haarcascade_frontalface_alt.xml");
        Log.d(TAG, "read_cascade_file:");

        cascadeClassifier_eye = loadCascade( "haarcascade_eye_tree_eyeglasses.xml");
    }


    //asset영역에 있는 파일을 공용영역에 저장하는 함수//
    private void copyFile(String filename) {
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }
    }

    //리스트뷰에서 클릭 이벤트가 발생하면 이 함수가 실행된다.
    private void read_mask_file(String maskname)
    {

        String baseDir = Environment.getExternalStorageDirectory().getPath();
        final File maskfolder=new File(baseDir+File.separator+"masks");
        baseDir=baseDir+File.separator+"masks";

        if(!maskfolder.mkdir())
            Log.e("OpencvActivity","Mask폴더 생성 실패(이미존재)");
        else
            Log.e("OpencvActivity","Mask폴더 생성 성공!");

        // 1. 마스크를 누르게 되면 asset의 이미지가 외부저장소로 복사가 된다
        copyFile2(maskname);

        Log.d(TAG, "read_Mask_file:");


    }

    //asset영역에 있는 파일을 공용영역에 저장하는 함수//
    private void copyFile2(String filename) {
        String baseDir = Environment.getExternalStorageDirectory().getPath()+File.separator+"masks";
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }

        maskname=filename;
    }








    /////////////////////////////////마스크 부분 어뎁터 ///////////////////////////////////////////


    //뷰홀더 설정
    private class ViewHolder{
        public ImageView maskimage;
    }

    //tvlistview의 어뎁터 설정
    private class MASK_adapter extends BaseAdapter
    {
        private Context context=null;
        private ArrayList<MASK_ListData> arraylist=new ArrayList<>();


        public MASK_adapter(Context context)
        {
            super();
            this.context=context;
        }



        @Override
        public int getCount() {
            return arraylist.size();
        }

        @Override
        public Object getItem(int i) {
            return arraylist.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public void addItem(Drawable screenimage,String maskname)
        {
            MASK_ListData item=null;
            item=new MASK_ListData();
            item.maskimage=screenimage;
            item.maskname=maskname;

            arraylist.add(item);
        }

        public void remove(int postion)
        {

            try {
                arraylist.remove(postion);
            }catch (IndexOutOfBoundsException e)
            {
                e.printStackTrace();
                Log.e("[자바 인덱스 에러]"," --> Remove 자바 인덱스 에러 발생 : "+e.getMessage());
            }

            dataChange();
        }

        public void Sort(){
            //Collection.sort(arraylist,TV_ListData.aasdasd_Comparator)
            dataChange();

        }

        public void dataChange(){
            maskadapter.notifyDataSetChanged();
        }




        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ViewHolder holder;

            //뷰에 아무것도 없으면
            if(view==null)
            {
                //홀더 초기화를 한다.
                holder=new ViewHolder();

                //리스트뷰 레이아웃을 객체화(실제 메모리에 로드)
                LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view=inflater.inflate(R.layout.masklistview_item,null);

                holder.maskimage=(ImageView)view.findViewById(R.id.maskimage);


                //이부분 나중에 다시 공부할것 .. 태그란 ??
                view.setTag(holder);
            }
            else
            {
                holder=(ViewHolder)view.getTag();
            }

            MASK_ListData maskdata=arraylist.get(position);

            /*이미지 부분 추가할것
            if(tvdata.screenimage!=null)
            */

            holder.maskimage.setImageDrawable(maskdata.maskimage);

            return view;
        }
    }

    ///////////////////////////////bitmap을 png파일로 저장하는 함수//////////////////////



    /////////////////////////onActivityResult부분///////////////////////////////////////////////


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT).show();
            return;
        }

        //1001은 onTouch에서 보낸 값이다.
        if(requestCode==1001)
        {
            String pngfilepath=data.getExtras().getString("pngfilepath");
            Intent intent=new Intent();
            intent.putExtra("pngfilepath",pngfilepath);
            setResult(RESULT_OK,intent);
            finish();

        }
    }

}
