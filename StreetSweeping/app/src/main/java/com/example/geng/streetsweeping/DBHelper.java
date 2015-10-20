package com.example.geng.streetsweeping;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.File;
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
    public static String DB_PATH;
    public static final String DB_NAME = "StreetSweepDB.sqlite";
    public SQLiteDatabase database;
    private Context myContext;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.myContext = context;
        String packageName = myContext.getPackageName();
        DB_PATH = String.format("/data/data/%s/databases/", packageName);
        openDataBase();
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    //This piece of code will create a database if it’s not yet created
    public void createDataBase() {
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                Log.e(this.getClass().toString(), "Copying error");
                throw new Error("Error copying database!");
            }
        } else {
            Log.i(this.getClass().toString(), "Database already exists");
        }
    }

    //Performing a database existence check
    private boolean checkDataBase() {
        SQLiteDatabase checkDb = null;
        String path = DB_PATH + DB_NAME;
        File file = new File(path);
        if(file.exists()) {
            try {
                checkDb = SQLiteDatabase.openDatabase(path, null,
                        SQLiteDatabase.OPEN_READONLY);
            } catch (SQLException e) {
                // Log.e(this.getClass().toString(), e.getMessage());
                // Log.e(this.getClass().toString(), "Error while checking db");

            }
            //Android doesn’t like resource leaks, everything should
            // be closed
            if (checkDb != null) {
                checkDb.close();
            }
            return checkDb != null;
        }
        else {
            return false;
        }
    }

    //Method for copying the database
    private void copyDataBase() throws IOException {
        //Open a stream for reading from our ready-made database
        //The stream source is located in the assets
        InputStream externalDbStream = myContext.getAssets().open(DB_NAME);

        //Path to the created empty database on your Android device
        String outFileName = DB_PATH + DB_NAME;

        //Now create a stream for writing the database byte by byte
        OutputStream localDbStream = new FileOutputStream(outFileName);

        //Copying the database
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = externalDbStream.read(buffer)) > 0) {
            localDbStream.write(buffer, 0, bytesRead);
        }
        //Don’t forget to close the streams
        localDbStream.close();
        externalDbStream.close();
    }

    public SQLiteDatabase openDataBase() throws SQLException {
        String path = DB_PATH + DB_NAME;
        if (database == null) {
            createDataBase();
            database = SQLiteDatabase.openDatabase(path, null,
                    SQLiteDatabase.OPEN_READWRITE);
        }
        return database;
    }

    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
 }

