package com.example.geng.streetsweeping;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by geng on 10/6/15.
 */
public class Street {

    String name;
    int numberFrom;
    int numberTo;
    String bound;
    String sweepingDate;
    String timeFrom;
    String timeTo;
    LatLng[] latLngs;
    boolean selected;

    public Street() {

    }

    public double distance(LatLng point) {
        return 0;
    }


}
