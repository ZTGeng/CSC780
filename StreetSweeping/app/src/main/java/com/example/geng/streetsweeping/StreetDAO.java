package com.example.geng.streetsweeping;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geng on 10/6/15.
 */
public class StreetDAO{

    DBHelper dbHelper;

    public StreetDAO(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    List<Street> getStreetsByLatLng(LatLng point) {
        List<Street> streets = new ArrayList<Street>();

        return streets;
    }

    List<Street> getStreetsOnScreen(LatLngBounds latLngBounds) {
        List<Street> streets = new ArrayList<Street>();

        // for (LatLng latLng : LatLngs) {
        //    if (latLngBounds.contains(latLng) && ) {
        //        streets.add(new Street());
        //    }
        // }

        return streets;
    }

}
