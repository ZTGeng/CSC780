package com.example.geng.streetsweeping;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by geng on 10/6/15.
 */
public class Street {

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
        this.weekday = new boolean[8]; // weekday[7] <- holiday
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
        for (int i = 1; i < weekOfMonth.length; i++) {
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
            for (int i = 1; i < weekOfMonth.length; i++) {
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
        int timeFromInt = Integer.parseInt(this.timeFrom.substring(0, 2));
        int timeToInt = Integer.parseInt(this.timeTo.substring(0, 2));
        String AM_PM_From = timeFromInt < 12 ? AM : PM;
        String AM_PM_To = timeToInt < 12 ? AM : PM;
        timeFromInt = (timeFromInt - 1) % 12 + 1;
        timeToInt = (timeToInt - 1) % 12 + 1;
        sweepTime.append(timeFromInt).append(AM_PM_From).append(" - ").append(timeToInt).append(AM_PM_To);
        sweepingTime = sweepTime.toString().trim();
        return sweepingTime;
    }

    public String getTimeTillNext(Context context) {
        if (timeFrom == null || timeTo == null) return "";
        Calendar now = Calendar.getInstance();
        int currentWeekOfMonth = now.get(Calendar.WEEK_OF_MONTH);
        int currentWeekday = now.get(Calendar.DAY_OF_WEEK) - 1; // Calendar.DAY_OF_WEEK starts with 1 (Sunday)
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int timeFromInt = Integer.parseInt(this.timeFrom.substring(0, 2));
        int timeToInt = Integer.parseInt(this.timeTo.substring(0, 2));
        if (weekOfMonth[currentWeekOfMonth] && weekday[currentWeekday] &&
                currentHour >= timeFromInt && currentHour < timeToInt) {
            return context.getString(R.string.NOW);
        }

        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);

        Calendar sweepDate = getNextCalendar(now, year, month, timeFromInt);
        if (sweepDate == null) {
            month = (month + 1) % 12;
            if (month == 0) year++;
            sweepDate = getNextCalendar(now, year, month, timeFromInt);
        }
        if (sweepDate == null) return "";

        int period = (int) (sweepDate.getTimeInMillis() / 1000 - now.getTimeInMillis() / 1000);
        int days = period / (60 * 60 * 24);
        period %= (60 * 60 * 24);
        int hrs = period / (60 * 60);
        period %= (60 * 60);
        int mins = period / 60;
        return String.format(context.getString(R.string.next_time), days, hrs, mins);
    }

    private Calendar getNextCalendar(Calendar now, int year, int month, int hour) {
        Calendar sweepDate = Calendar.getInstance();
        sweepDate.set(year, month, 1, hour, 0);
        int[] weekdayCount = new int[7];
        for (int day = 1; day <= sweepDate.getActualMaximum(Calendar.DATE); day++) {
            sweepDate.set(Calendar.DATE, day);
            int dayName = sweepDate.get(Calendar.DAY_OF_WEEK) - 1;
            weekdayCount[dayName]++;
            if (weekOfMonth[weekdayCount[dayName] - 1] && weekday[dayName]) {
                if (sweepDate.after(now)) {
                    return sweepDate;
                }
            }
        }
        return null;
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
