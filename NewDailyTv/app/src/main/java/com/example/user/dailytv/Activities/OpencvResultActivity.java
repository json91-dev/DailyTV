package com.example.user.dailytv.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.user.dailytv.R;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by user on 2017-11-24.
 */

public class OpencvResultActivity extends AppCompatActivity {

    //opencv에서 넘어온 이미지
    ImageView pictureimage;

    Button okbutton,cancelbutton;

    String pngfilepath;

    Bitmap picturebitmap;

    LinearLayout filter1,filter2,filter3,filter4;
    LinearLayout filterorgin;

    //JNI관련 함수
    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);
    public native void ConvertRGBtoFilter2(long matAddrInput, long matAddrResult);
    public native void ConvertRGBtoFilter3(long matAddrInput, long matAddrResult);
    public native void ConvertRGBtoFilter4(long matAddrInput, long matAddrResult);



    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.opencvresultactivity);



        pictureimage=(ImageView)findViewById(R.id.pictureimage);


        //이전 액티비티에서 Png파일의 Uri를 받아왔음.
        Intent intent=getIntent();
        pngfilepath=intent.getExtras().getString("pngfilepath");

        //Uri를 bitmap값으로 바꿈.


        picturebitmap= BitmapFactory.decodeFile(pngfilepath);
        pictureimage.setImageBitmap(picturebitmap);




        /////////////////////////버튼 이미지 처리하는 부분///////////////////////////////
        okbutton=(Button)findViewById(R.id.okbutton);
        cancelbutton=(Button)findViewById(R.id.cancelbutton);



        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent();
                intent.putExtra("pngfilepath",pngfilepath);
                setResult(RESULT_OK,intent);

                Log.e("OpencvResultActivity",pngfilepath);
                finish();
            }
        });

        cancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });


   //////////////////////////////////////필터 터치 이벤트//////////////////////////////////

        filterorgin=(LinearLayout)findViewById(R.id.filterorgin);
        filterorgin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureimage.setImageBitmap(picturebitmap);

                final File filteredfile=new File(pngfilepath);
                saveBitmapToPng(picturebitmap,filteredfile);

            }
        });


        filter1=(LinearLayout)findViewById(R.id.filter1);
        filter1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"필터1 클릭",Toast.LENGTH_LONG).show();

                //png bitmap값 얻어오기
                final Bitmap inputbitmap=picturebitmap;

                //Mat변수 초기화
                Mat inputmat=new Mat(inputbitmap.getWidth(),inputbitmap.getHeight(), CvType.CV_8SC4);
                Mat outputmat=new Mat(inputbitmap.getWidth(),inputbitmap.getHeight(), CvType.CV_8SC4);

                //pngbitmap을 Mat으로 변환
                Utils.bitmapToMat(inputbitmap,inputmat);

                //이부분에서 outputmat에 mat값이 저장됨
                ConvertRGBtoGray(inputmat.getNativeObjAddr(),outputmat.getNativeObjAddr());

                //outputbitmap초기화
                final Bitmap outputbitmap;
                outputbitmap=Bitmap.createBitmap(outputmat.cols(),outputmat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(outputmat,outputbitmap);
                pictureimage.setImageBitmap(outputbitmap);

                //비트맵을 png파일로 저장해야함 ....

                final File filteredfile=new File(pngfilepath);
                saveBitmapToPng(outputbitmap,filteredfile);




            }
        });

        filter2=(LinearLayout)findViewById(R.id.filter2);
        filter2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //png bitmap값 얻어오기
                final Bitmap inputbitmap=picturebitmap;

                //Mat변수 초기화
                Mat inputmat=new Mat(inputbitmap.getWidth(),inputbitmap.getHeight(), CvType.CV_8SC4);
                Mat outputmat=new Mat(inputbitmap.getWidth(),inputbitmap.getHeight(), CvType.CV_8SC4);

                //pngbitmap을 Mat으로 변환
                Utils.bitmapToMat(inputbitmap,inputmat);

                //이부분에서 outputmat에 mat값이 저장됨
                ConvertRGBtoFilter2(inputmat.getNativeObjAddr(),outputmat.getNativeObjAddr());

                //outputbitmap초기화
                final Bitmap outputbitmap;
                outputbitmap=Bitmap.createBitmap(outputmat.cols(),outputmat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(outputmat,outputbitmap);

                pictureimage.setImageBitmap(outputbitmap);

                final File filteredfile=new File(pngfilepath);
                saveBitmapToPng(outputbitmap,filteredfile);


            }
        });

        filter3=(LinearLayout)findViewById(R.id.filter3);
        filter3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //png bitmap값 얻어오기
                final Bitmap inputbitmap=picturebitmap;

                //Mat변수 초기화
                Mat inputmat=new Mat(inputbitmap.getWidth(),inputbitmap.getHeight(), CvType.CV_8SC4);
                Mat outputmat=new Mat(inputbitmap.getWidth(),inputbitmap.getHeight(), CvType.CV_8SC4);

                //pngbitmap을 Mat으로 변환
                Utils.bitmapToMat(inputbitmap,inputmat);

                //이부분에서 outputmat에 mat값이 저장됨
                ConvertRGBtoFilter3(inputmat.getNativeObjAddr(),outputmat.getNativeObjAddr());

                //outputbitmap초기화
                final Bitmap outputbitmap;
                outputbitmap=Bitmap.createBitmap(outputmat.cols(),outputmat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(outputmat,outputbitmap);

                pictureimage.setImageBitmap(outputbitmap);

                final File filteredfile=new File(pngfilepath);
                saveBitmapToPng(outputbitmap,filteredfile);

            }
        });

        filter4=(LinearLayout)findViewById(R.id.filter4);
        filter4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //png bitmap값 얻어오기
                final Bitmap inputbitmap=picturebitmap;

                //Mat변수 초기화
                Mat inputmat=new Mat(inputbitmap.getWidth(),inputbitmap.getHeight(), CvType.CV_8SC4);
                Mat outputmat=new Mat(inputbitmap.getWidth(),inputbitmap.getHeight(), CvType.CV_8SC4);

                //pngbitmap을 Mat으로 변환
                Utils.bitmapToMat(inputbitmap,inputmat);

                //이부분에서 outputmat에 mat값이 저장됨
                ConvertRGBtoFilter4(inputmat.getNativeObjAddr(),outputmat.getNativeObjAddr());

                //outputbitmap초기화
                final Bitmap outputbitmap;
                outputbitmap=Bitmap.createBitmap(outputmat.cols(),outputmat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(outputmat,outputbitmap);

                pictureimage.setImageBitmap(outputbitmap);

                final File filteredfile=new File(pngfilepath);
                saveBitmapToPng(outputbitmap,filteredfile);



            }
        });




        ///////////////////////////////////////////필터부분 종료 /////////////////////////////////////////

    }


    ///////////////////////////////////////Bitmap-> png저장하는 함수//////////////////////////////

    public static String saveBitmapToPng(Bitmap bitmap, File file_){

        File file=file_;




        try{
            //tempFile.createNewFile();  // 파일을 생성해주고

            FileOutputStream out = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG, 90 , out);  // 넘거 받은 bitmap을 png(손실압축)으로 저장해줌

            out.close(); // 마무리로 닫아줍니다.

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("tempfile.getAbsoutePath",file.getAbsolutePath());

        return file.getAbsolutePath();   // 임시파일 저장경로를 리턴해주면 끝!
    }

}
