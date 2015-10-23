package com.example.geng.streetsweeping;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by geng on 10/6/15.
 */
public class Street {

    private String name;
    private int addressFrom;
    private int addressTo;
    private String blockSide;
    private String sweepingDate; // use for show in TextView
    private int[] weekday; // 0 - 6
    private int[] weekOfMonth; // 1 - 5
    private String timeFrom;
    private String timeTo;
    private List<LatLng> latLngs;
    private String side;

    public Street(String name, int addressFrom, int addressTo, String blockSide,String sweepingDate, int[] weekday,
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
        this.side = side;

    }
    public String getStreetName () {
        return this.name;
    }
    public int getAddressFrom () {
        return this.addressFrom;
    }
    public int getAddressTo () {
        return this.addressTo;
    }
    public String getBlockSide () {
        return this.blockSide;
    }
    public String getSweepingDate () {
        return this.sweepingDate;
    }
    public int[] getWeekday () {
        return this.weekday;
    }
    public int[] getWeekOfMonth () {
        return this.weekOfMonth;
    }
    public String getTimeFrom () {
        return this.timeFrom;
    }
    public String getTimeTo () {
        return this.timeTo;
    }
    public List<LatLng> getlatLngs () {
        return this.latLngs;
    }
    public String getSide () {
        return this.side;
    }

    public void setStreetName (String streetName) {
        this.name = streetName;
    }
    public void setAddressFrom (int addressFrom) {
        this.addressFrom = addressFrom;
    }
    public void setAddressTo (int addressTo) {
        this.addressTo = addressTo;
    }
    public void setBlockSide (String blockSide) {
        this.blockSide = blockSide;
    }
    public void setSweepingDate (String sweepingDate) {
        this.sweepingDate = sweepingDate;
    }
    public void setWeekday (int[] weekday) {
        this.weekday = weekday;
    }
    public void setWeekOfMonth (int[] weekOfMonth) {
        this.weekOfMonth = weekOfMonth;
    }
    public void setTimeFrom (String timeFrom) {
        this.timeFrom = timeFrom;
    }
    public void setTimeTo (String timeTo) {
        this.timeTo = timeTo;
    }
    public void setlatLngs (List<LatLng> latLngs) {
        this.latLngs = latLngs;
    }
    public void setSide (String side) {
        this.side = side;
    }
    public double distance(LatLng point) {
        return 0;
    }


}
