package com.example.geng.streetsweeping;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by geng on 10/6/15.
 */
public class Street {

    String name;
    int numberFrom;
    int numberTo;
    String bound;
    String sweepingDate; // use for show in TextView
    int weekday; // 0 - 6
    int[] weekOfMonth; // 1 - 5
    int timeFrom;
    int timeTo;
    List<LatLng> latLngs;

    public Street() {

    }

    public double distance(LatLng point) {
        return 0;
    }


}
