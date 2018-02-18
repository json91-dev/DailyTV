package com.quickblox.sample.groupchatwebrtc.utils;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;


import com.example.user.dailytv.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tereha on 03.06.16.
 */
public class ValidationUtils {

    private static final int isGenderChecked=0;
    private static final int isAgeChecked=1;
    private static final int isHobbyChecked=2;

    //UserName부분이 방 타이틀부분이다.[주의]
    private static final int isUserNameChecked=3;

    //이부분에서 정규식으로 패턴을 검사한다.
    private static boolean isEnteredTextValid(Context context, EditText editText, int resFieldName, int maxLength, boolean checkName) {

        boolean isCorrect;
        Pattern p;
        if (checkName) {
            //대문자소분자숫자 2개에서 20글자 사이

            p = Pattern.compile("^[a-zA-Z][a-zA-Z 0-9]{2," + (maxLength - 1) + "}+$");



        } else {
            p = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{2," + (maxLength - 1) + "}+$");
        }

        Matcher m = p.matcher(editText.getText().toString().trim());
        isCorrect = m.matches();

        if (!isCorrect) {
            editText.setError(String.format(context.getString(R.string.error_name_must_not_contain_special_characters_from_app),
                    context.getString(resFieldName), maxLength));
            return false;
        } else {
            return true;
        }
    }

    public static boolean isUserNameValid(Context context, EditText editText) {
        return isEnteredTextValid2(context, editText, R.string.field_name_user_name, 20, isUserNameChecked);
    }

    public static boolean isRoomNameValid(Context context, EditText editText) {
        return isEnteredTextValid(context, editText, R.string.field_name_chat_room_name, 15, false);
    }


    //내가 임의로 만든 정규식을 통해 유효성을 검사할수 있도록 해주는 함수이다
    //내부적인 구조는 코딩을 하면서 파악할 계획이다.

    private static boolean isEnteredTextValid2(Context context, EditText editText, int resFieldName, int maxLength, int checkSequence) {

        boolean isCorrect;
        Pattern p;
        Matcher m;


        //정규표현식 정리
        // ^ : 문자열 시작
        // $ : 문자열의 끝
        // \d: [0-9]와 동일하다 =>[0-9]는 숫자 1개를 의미한다.

        //?  : 0 또는 1회
        //*  : 0회 이상
        //+  : 1회 이상

        if (checkSequence==isGenderChecked) {
            //대문자소분자숫자 2개에서 20글자 사이
            // 남자 또는 여자로만 입력받아야한다.
            p = Pattern.compile("");

            //이부분에서 남자인지 여자인지 판단하는 소스가 필요하다.
            if(!(editText.getText().toString().equals("남성") || editText.getText().toString().equals("여성")))
            {
                editText.setError("입력 형식이 잘못되었습니다. ex) 남성,여성");
                return false;

            }else
            {
                isCorrect=true;
            }


        } else if(checkSequence==isAgeChecked){
            //2자리의 숫자로만 입력받아야한다.
            p = Pattern.compile("\\d{2}");
            m = p.matcher(editText.getText().toString().trim());
            isCorrect = m.matches();

            if(!isCorrect)
            {
                editText.setError("입력 형식이 잘못되었습니다. ex) 10 - 99 ");
                return false;
            }


        }
        else if(checkSequence==isHobbyChecked) //isHobbyChecked
        {
            //3~20까지의 영문자,숫자,한글로 입력받아야한다.
            p= Pattern.compile("^[a-zA-Z가-힣ㄱ-ㅎ!@#$%^&*\\s]{2,15}"+"$");
            m = p.matcher(editText.getText().toString().trim());
            Log.e("퀵4","11111"+editText.getText().toString().trim());

            isCorrect = m.matches();
            if(!isCorrect)
            {

                editText.setError("입력 형식이 잘못되었습니다. ex) 영화보기 / 게임하기");
                return false;
            }

        }else{ //UserName 즉 방제목
            p = Pattern.compile("^[가-힣ㄱ-ㅎ!@#$%^&*\\s]{4,13}"+"$");
            m=p.matcher(editText.getText().toString().trim());

            Log.e("퀵4","11111"+editText.getText().toString().trim());


            isCorrect=m.matches();
            if(!isCorrect)
            {
                editText.setError("입력 형식이 잘못되었습니다. ex) 안녕하세요, 반가워요 [4~13글자 한글,특수문자]");
                return false;
            }
        }

        return true;

    }




    //이부분은 내가 임의로 성별과 나이와 취미를 입력받고
    //유효성 검사를 하기위해 추가한 함수이다.
    public static boolean isGenderValid(Context context, EditText editText) {
        return isEnteredTextValid2(context, editText, R.string.field_name_chat_room_name, 15, isGenderChecked);
    }
    public static boolean isAgeValid(Context context, EditText editText) {
        return isEnteredTextValid2(context, editText, R.string.field_name_chat_room_name, 15, isAgeChecked);
    }
    public static boolean ishobbyValid(Context context, EditText editText) {
        return isEnteredTextValid2(context, editText, R.string.field_name_chat_room_name, 15, isHobbyChecked);
    }

}
