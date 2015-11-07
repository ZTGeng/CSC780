package com.example.geng.streetsweeping;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geng on 10/6/15.
 */
public class Street {

//    private static final String[] WEEKDAYS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
//    private static final String[] WEEKDAYSALL = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
//    private static final String[] WEEKS = {"1st", "2nd", "3rd", "4th", "5th"};
    private static final String AM = "AM";
    private static final String PM = "PM";

    private String name;
    private int addressFrom;
    private int addressTo;
    private String blockSide;
    private boolean[] weekday;
    private boolean[] weekOfMonth; // 1 - 5
    private String timeFrom;
    private String timeTo;
    private List<LatLng> latLngs;
    private String side;
    private String sweepingDate;
    private String sweepingTime;

    public Street(String name) {
        this.name = name;
        this.weekday = new boolean[7];
        this.weekOfMonth = new boolean[5];
        this.latLngs = new ArrayList<>();
    }

    public boolean hasWeekday(int wday) {
        return weekday[wday];
    }

    public void addWeekday(int wday) {
        weekday[wday] = true;
    }

    public boolean hasWeekOfMonth(int wOfMonth) {
        return weekOfMonth[wOfMonth];
    }

    public void addWeekOfMonth (int week) {
        weekOfMonth[week] = true;
    }

    public String getSweepingDate (Context context) {
        if (sweepingDate != null) return sweepingDate;
        StringBuilder sweepDate = new StringBuilder();
        Resources res = context.getResources();
        boolean flag = true;
        for (int i = 0; i < weekOfMonth.length; i++) {
            if (!weekOfMonth[i]) {
                flag = false;
                break;
            }
        }
        if (flag) {
            for (int i = 0; i < weekday.length; i++) {
                if (weekday[i]) {
                    sweepDate.append(res.getStringArray(R.array.WEEKDAYSALL)[i]).append(" ");
                }
            }
        } else {
            for (int i = 0; i < weekOfMonth.length; i++) {
                if (weekOfMonth[i]) {
                    sweepDate.append(res.getStringArray(R.array.WEEKS)[i]).append(" ");
                }
            }
            for (int i = 0; i < weekday.length; i++) {
                if (weekday[i]) {
                    sweepDate.append(res.getStringArray(R.array.WEEKDAYS)[i]).append(" ");
                }
            }
        }
        sweepingDate = sweepDate.toString().trim();
        return sweepingDate;
    }

    public String getSweepingTime() {
        if (sweepingTime != null) return sweepingTime;
        StringBuilder sweepTime = new StringBuilder();
        if (this.timeFrom == null || this.timeTo == null) return sweepTime.toString();
        int timeFromInt = Integer.parseInt(this.timeFrom.substring(0,2));
        int timeToInt = Integer.parseInt(this.timeTo.substring(0,2));
        String halfDayFrom = timeFromInt < 12 ? AM : PM;
        String halfDayTo = timeFromInt < 12 ? AM : PM;
        timeFromInt = (timeFromInt - 1) % 12 + 1;
        timeToInt = (timeToInt - 1) % 12 + 1;
        sweepTime.append(timeFromInt).append(halfDayFrom).append(" - ").append(timeToInt).append(halfDayTo);
        sweepingTime = sweepTime.toString().trim();
        return sweepingTime;
    }

    public String getTimeTillNext() {
        return "";
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


    public String getTimeFrom () {
        return this.timeFrom;
    }

    public String getTimeTo () {
        return this.timeTo;
    }

    public List<LatLng> getLatLngs () {
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


    public void addLatLngs(LatLng latLng) {
        this.latLngs.add(latLng);
    }

    public double distance(LatLng point) {
        return 0;
    }

    public String toString() {
        String result =this.side+ " side of "+ this.name + "; time from " + this.timeFrom +" to " + this.timeTo + " on ";
//        if(!weekOfMonth.isEmpty()) {
//            for (Integer week : weekOfMonth) {
//                result += week.toString() + " ";
//            }
//        }
//        if(!weekday.isEmpty()) {
//            for (String s : weekday) {
//                result += s + " ";
//            }
//        }
        return result;
    }
}
