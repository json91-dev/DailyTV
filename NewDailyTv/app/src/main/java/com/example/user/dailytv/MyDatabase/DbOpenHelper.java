package com.example.user.dailytv.MyDatabase;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DbOpenHelper {

    private static final String DATABASE_NAME = "dailytv";
    private static final int DATABASE_VERSION = 2;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    //SQLiteOpenHelper는 db

    private class DatabaseHelper extends SQLiteOpenHelper {

        //SQliteopenHelper는 DB를 생성하고 버전 업데이트를 하도록 도와주는 아이이다.

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //db.execSQL(DataBases.CREATE_TABLE.firnedlist_CREATE);
            //db.execSQL(DataBases.CREATE_TABLE.talk_CREATE);


            //DBopenhelper 초기화시 생성된다.
            db.execSQL(DataBases.CREATE_TABLE.message_CREATE);
            db.execSQL(DataBases.CREATE_TABLE.roominfo_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {

            //db.execSQL("delete from talk");
            //db.execSQL("delete from friendlist");


            db.execSQL("DROP TABLE IF EXISTS message");
            db.execSQL("DROP TABLE IF EXISTS roominfo");


            db.execSQL(DataBases.CREATE_TABLE.message_CREATE);
            db.execSQL(DataBases.CREATE_TABLE.roominfo_CREATE);


            //db.execSQL("delete from message");
            //db.execSQL("delete from romminfo");



            /*
            Log.e("db 업데이트 버전 3","");
            */
        }
    }

    public DbOpenHelper(Context context) {
        this.mCtx = context;
    }

    public DbOpenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();

        return this;
    }

    public SQLiteDatabase getSQLiteDb() {
        return mDBHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDb(){ return mDBHelper.getReadableDatabase();}

}





