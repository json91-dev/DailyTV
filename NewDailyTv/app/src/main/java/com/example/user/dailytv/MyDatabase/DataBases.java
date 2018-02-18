package com.example.user.dailytv.MyDatabase;

import android.provider.BaseColumns;

/**
 * Created by user on 2017-06-01.
 */

public final class DataBases {
    public static final class CREATE_TABLE implements BaseColumns {



        public static final String message_CREATE="create table message "+
                "("+
                "_id integer not null primary key autoincrement," +
                "bjnickname varchar(20) not null,"+
                "nickname varchar(20) not null," +
                "text varchar(100) not null," +
                "color varchar(20) not null"+
                ");";
                //"time real not null)";

        public static final String roominfo_CREATE="create table roominfo "+
                "("+
                "_id integer not null primary key autoincrement," +
                "bjnickname varchar(2) not null,"+
                "imageurl varchar(50) not null,"+
                "title varchar(20) not null," +
                "viewernumberlimit integer not null default 100," +
                "viwernumber integer not null default 0," +
                "password varchar(10)"
                +");";

        /*
        public static final String firnedlist_CREATE="CREATE TABLE friendlist " +
                "(_id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "loginid VARCHAR(20) NOT NULL, " +
                "friendid VARCHAR(20) NOT NULL," +
                "statement INTEGER NOT NULL);";

        public static final String talk_CREATE="CREATE TABLE talk (" +
                "_id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "loginid VARCHAR(20)  NOT NULL," +
                "friendid VARCHAR(20)  NOT NULL," +
                "nickname VARCHAR(30) NOT NULL,"+
                "imageurl VARCHAR(100) NULL,"+
                "messagetype INTEGER  NOT NULL," +
                "message VARCHAR(150)  NULL," +
                "readcheck INTEGER  default '0' NULL,"+
                 "time TIMESTAMP  DEFAULT CURRENT_TIMESTAMP NOT NULL,"+
                "chatimageurl varchar(100) NULL "+
                ");";

          */
    }
}
