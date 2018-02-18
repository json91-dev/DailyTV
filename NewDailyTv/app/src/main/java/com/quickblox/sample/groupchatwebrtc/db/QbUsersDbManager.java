package com.quickblox.sample.groupchatwebrtc.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tereha on 17.05.16.
 */

//QB유저로부터 저장된 값을 가져오는 DB매니져

public class QbUsersDbManager {
    private static String TAG = QbUsersDbManager.class.getSimpleName();

    private static QbUsersDbManager instance;
    private Context mContext;

    private QbUsersDbManager(Context context) {
        this.mContext = context;
    }

    public static QbUsersDbManager getInstance(Context context) {
        if (instance == null) {
            instance = new QbUsersDbManager(context);
        }


        return instance;
    }


    //모든 유저에대한 QBUser 객체를 저장하는 ArrayList를 반환한다.


    public ArrayList<QBUser> getAllUsers() {
        ArrayList<QBUser> allUsers = new ArrayList<>();

        //DbHelper은 컬럼 정보, 유저 ID,Password, 등을 저장하여 참조하여 사용할수 있는 클래스이다.
        //SqliteOpenHelper을 상속받는다.
        //
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(DbHelper.DB_TABLE_NAME, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int userIdColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_ID);
            int userLoginColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_LOGIN);
            int userPassColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_PASSWORD);
            int userFullNameColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_FULL_NAME);
            int userTagColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_TAG);

            do {
                QBUser qbUser = new QBUser();

                qbUser.setFullName(c.getString(userFullNameColIndex));
                qbUser.setLogin(c.getString(userLoginColIndex));
                qbUser.setId(c.getInt(userIdColIndex));
                qbUser.setPassword(c.getString(userPassColIndex));

                StringifyArrayList<String> tags = new StringifyArrayList<>();
                tags.add(c.getString(userTagColIndex));
                qbUser.setTags(tags);

                allUsers.add(qbUser);
            } while (c.moveToNext());
        }

        c.close();
        dbHelper.close();

        return allUsers;
    }

    public QBUser getUserById(Integer userId) {
        QBUser qbUser = null;
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(DbHelper.DB_TABLE_NAME, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int userIdColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_ID);
            int userLoginColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_LOGIN);
            int userPassColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_PASSWORD);
            int userFullNameColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_FULL_NAME);
            int userTagColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_TAG);

            do {
                if (c.getInt(userIdColIndex) == userId) {
                    qbUser = new QBUser();
                    qbUser.setFullName(c.getString(userFullNameColIndex));
                    qbUser.setLogin(c.getString(userLoginColIndex));
                    qbUser.setId(c.getInt(userIdColIndex));
                    qbUser.setPassword(c.getString(userPassColIndex));

                    StringifyArrayList<String> tags = new StringifyArrayList<>();
                    tags.add(c.getString(userTagColIndex).split(","));
                    qbUser.setTags(tags);
                    break;
                }
            } while (c.moveToNext());
        }

        c.close();
        dbHelper.close();

        return qbUser;
    }

    public void saveAllUsers(ArrayList<QBUser> allUsers, boolean needRemoveOldData) {
        if (needRemoveOldData) {
            clearDB();
        }

        for (QBUser qbUser : allUsers) {
            saveUser(qbUser);
        }
        Log.d(TAG, "saveAllUsers");
    }

    //ContentValues는 DB에 자료를 쉽게 입력하기위해 key value값으로 자료를 설정한뒤
    //cursor와는 반대방식으로 값을 데이터베이스에 입력하는 방식이다.

    public void saveUser(QBUser qbUser) {
        ContentValues cv = new ContentValues();
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        cv.put(DbHelper.DB_COLUMN_USER_FULL_NAME, qbUser.getFullName());
        cv.put(DbHelper.DB_COLUMN_USER_LOGIN, qbUser.getLogin());
        cv.put(DbHelper.DB_COLUMN_USER_ID, qbUser.getId());
        cv.put(DbHelper.DB_COLUMN_USER_PASSWORD, qbUser.getPassword());
        cv.put(DbHelper.DB_COLUMN_USER_TAG, qbUser.getTags().getItemsAsString());

        db.insert(DbHelper.DB_TABLE_NAME, null, cv);
        dbHelper.close();
    }

    public void clearDB() {
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DbHelper.DB_TABLE_NAME, null, null);
        dbHelper.close();
    }

    public ArrayList<QBUser> getUsersByIds(List<Integer> usersIds) {
        ArrayList<QBUser> qbUsers = new ArrayList<>();

        for (Integer userId : usersIds) {
            if (getUserById(userId) != null) {
                qbUsers.add(getUserById(userId));
            }
        }

        return qbUsers;
    }
}

