package com.example.user.dailytv.Module;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by user on 2017-12-20.
 */

public class BitmapModule2 {

    //1. 저장될 파일의 경로를 설정한다.
    //2. 넘겨받은 bitmap을 outputstream을 이용하여 png파일로 저장한다.
    //3. 파일의 저장경로를 반환한다.

    public static String saveBitmapToPng(Context context, Bitmap bitmap, String name){

        File storage = Environment.getExternalStorageDirectory(); // 이 부분이 임시파일 저장 경로


        Log.e("캐시파일저장경로",Environment.getExternalStorageDirectory()+"");

        String fileName = name + ".png";  // 파일이름은 마음대로!

        File tempFile = new File(storage,fileName);

        try{
            tempFile.createNewFile();  // 파일을 생성해주고

            FileOutputStream out = new FileOutputStream(tempFile);

            bitmap.compress(Bitmap.CompressFormat.PNG, 90 , out);  // 넘거 받은 bitmap을 png(손실압축)으로 저장해줌

            out.close(); // 마무리로 닫아줍니다.

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("tempfile.getAbsoutePath",tempFile.getAbsolutePath());

        return tempFile.getAbsolutePath();   // 임시파일 저장경로를 리턴해주면 끝!
    }
}
