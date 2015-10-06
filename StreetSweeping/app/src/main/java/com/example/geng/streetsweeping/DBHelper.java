package com.example.geng.streetsweeping;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by geng on 10/6/15.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 3;
    public static final String DB_NAME = "StreetSweepDB";
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
