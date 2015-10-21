package com.example.geng.streetsweeping;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.*;

/**
 * Created by geng on 10/6/15.
 */
public class StreetDAO{

    DBHelper dbHelper;
    SQLiteDatabase database;

    public StreetDAO(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        database = dbHelper.openDataBase();
        testDB();
    }

    public void testDB() {
        out.println("testing db here");
        out.println(this.getClass()+ " gets " +database.getPath());
        out.println(testingQuery("10th Ave"));
        out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        return;
    }

    private int testingQuery(String name) {
        out.println("Calling testing query here");
        Cursor resultCursor;
        String table = "StreetSweepData";
        String[] columns = {"GeneralInfo","Weekday","BlockSide","CNNRightLeft","Corridor","FromHour","ToHour","Week1OfMonth",
                "Week2OfMonth","Week3OfMonth","Week4OfMonth","Week5OfMonth","LF_FADD","LF_TOADD","RT_TOADD","RT_FADD","ZipCode",
                "Coordinates"};
        String selection = "Corridor = ?";
        String[] selectionArgs = {name};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        if(database.isOpen()) {
            resultCursor = database.query(table, columns, selection, selectionArgs, null, null, null);
            resultCursor.moveToFirst();
            return resultCursor.getCount();
        }
        else {
            out.println("no open db");
        }
        return 0;
    }

    List<Street> getStreetsByStreetName(String name) {
        List<Street> streets = new ArrayList<>();
        Cursor resultCursor;
        String table = "StreetSweepData";
        String[] columns = {"GeneralInfo","Weekday","BlockSide","CNNRightLeft","Corridor","FromHour","ToHour","Week1OfMonth",
                "Week2OfMonth","Week3OfMonth","Week4OfMonth","Week5OfMonth","LF_FADD","LF_TOADD","RT_TOADD","RT_FADD","ZipCode",
                "Coordinates"};
        String selection = "Cooridor = ?";
        String[] selectionArgs = {name};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        if(database.isOpen()) {
            resultCursor = database.query(table, columns, selection, selectionArgs, null, null, null);
            resultCursor.moveToFirst();
            out.println(resultCursor.getCount());
        }
        return streets;
    }

    List<Street> getStreetsOnScreen(LatLngBounds latLngBounds) {
        List<Street> streets = new ArrayList<>();

        // for (LatLng latLng : LatLngs) {
        //    if (latLngBounds.contains(latLng) && ) {
        //        streets.add(new Street());
        //    }
        // }

        return streets;
    }

}
