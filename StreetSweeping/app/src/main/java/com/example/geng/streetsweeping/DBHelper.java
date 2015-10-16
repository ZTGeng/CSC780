package com.example.geng.streetsweeping;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Created by geng on 10/6/15.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String DB_PATH = "/data/data/com.example.geng.streetsweeping/databases/";
    public static final String DB_NAME = "StreetSweepDB";
    public SQLiteDatabase sqLiteDatabase;

    private Context myContext;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.myContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void createDatabase() {
        createDB();
    }

    private void createDB() {
        boolean dbExits = DBExits();
        if(!dbExits) {
            this.getReadableDatabase();
            copyDBFromResouses();
        }
    }

    private boolean DBExits() {
        SQLiteDatabase db = null;

        try{
            String database_path = DB_PATH+DB_NAME;
            db = SQLiteDatabase.openDatabase(database_path,null,SQLiteDatabase.OPEN_READWRITE);
            db.setLocale(Locale.getDefault());
            db.setVersion(1);

        }catch (SQLiteException e) {
            Log.e("dbHelper:","db not found!");
        }

        if(db != null) {
            db.close();
        }

        return db != null ? true : false;
    }

    private void copyDBFromResouses() {
        InputStream inputStream = null;
        OutputStream outStream = null;
        String db_path = DB_PATH+DB_NAME;

        try{
            inputStream = myContext.getAssets().open(DB_NAME);
            outStream = new FileOutputStream(db_path);

            byte[] buffer = new byte[1024];
            int length = 0;
            while((length = inputStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);

            }
            outStream.flush();
            outStream.close();
            inputStream.close();
        }catch(IOException e){
            throw new Error("Problem copying database from resource file");
        }

    }
 }

