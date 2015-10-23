package com.example.geng.streetsweeping;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by geng on 10/6/15.
 */
public class Street {

    final static boolean LEFT = true;
    final static boolean RIGHT = false;

    String name;
    int addressFrom;
    int addressTo;
    String blockSide;
    String sweepingDate; // use for show in TextView
    int weekday; // 0 - 6
    int[] weekOfMonth; // 1 - 5
    String timeFrom;
    String timeTo;
    List<LatLng> latLngs;
    boolean side;

    public Street(String name, int addressFrom, int addressTo, String blockSide,String sweepingDate, int weekday,
                  int[] weekOfMonth, String timeFrom, String timeTo, List<LatLng> latLngs, String side) {
        this.name = name;
        this.addressFrom = addressFrom;
        this.addressTo = addressTo;
        this.blockSide = blockSide;
        this.sweepingDate = sweepingDate;
        this.weekday = weekday;
        this.weekOfMonth = weekOfMonth;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.latLngs = latLngs;
        if(side.equals("R"))
            this.side = RIGHT;
        else
            this.side = LEFT;

    }

    public double distance(LatLng point) {
        return 0;
    }


}
