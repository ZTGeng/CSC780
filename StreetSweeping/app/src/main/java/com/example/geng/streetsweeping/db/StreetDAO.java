package com.example.geng.streetsweeping.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.geng.streetsweeping.Street;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.lang.System.*;

/**
 * Created by geng on 10/6/15.
 */
public class StreetDAO implements StreetDAOInterface {

    private static HashMap<String, Integer> WEEKDAYS_HASH = new HashMap<>();
    private static String[] WEEK_OF_MONTH = {"Week1OfMonth", "Week2OfMonth", "Week3OfMonth", "Week4OfMonth", "Week5OfMonth"};
    private static String table = "StreetSweepData";
    private static String[] columns = {"GeneralInfo", "Weekday", "BlockSide", "CNNRightLeft", "Corridor",
            "FromHour", "ToHour", "Week1OfMonth", "Week2OfMonth", "Week3OfMonth", "Week4OfMonth", "Week5OfMonth",
            "LF_FADD", "LF_TOADD", "RT_TOADD", "RT_FADD", "ZipCode", "Coordinates"};
    private static final List<String> _numbers = Arrays.asList("1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th");
    private final static String YES = "Yes";
    private final static String LETTER_L = "L";
    private final static String LETTER_R = "R";
    private final static String SPACE = " ";
    private final static String COMMA = ",";

    static {
        WEEKDAYS_HASH.put("SUN", 0);
        WEEKDAYS_HASH.put("MON", 1);
        WEEKDAYS_HASH.put("TUES", 2);
        WEEKDAYS_HASH.put("WED", 3);
        WEEKDAYS_HASH.put("THU", 4);
        WEEKDAYS_HASH.put("FRI", 5);
        WEEKDAYS_HASH.put("SAT", 6);
        WEEKDAYS_HASH.put("HOLIDAY", 7);
    }

    DBHelper dbHelper;
    SQLiteDatabase database;

    public StreetDAO(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        database = dbHelper.openDataBase();
        //testDB();
    }


    public Street getStreetsByAddress(String numberAndName) {
        String[] strArray = numberAndName.trim().split(" ", 2);
        if (strArray.length < 2) return null;
        String[] numStrings = strArray[0].split("-");
        int houseNumber;
        try {
            if (numStrings.length == 1) {
                houseNumber = Integer.parseInt(numStrings[0]);
            } else if (numStrings.length == 2) {
                int num0 = Integer.parseInt(numStrings[0]);
                int num1 = Integer.parseInt(numStrings[1]);
                houseNumber = (num0 + num1) / 2;
                if (num0 % 2 != houseNumber % 2) houseNumber--;
            } else {
                return null;
            }
            // !!!!!!! "1st St" -> "01st St"
            if (_numbers.contains(strArray[1].substring(0, 3))) {
                strArray[1] = "0".concat(strArray[1]);
            }
            // !!!!!!! Delete it after modified the database
        } catch (NumberFormatException e) {
            return null;
        }

        String streetName = strArray[1];
        //null input checking
        if(streetName == null || streetName.isEmpty()) return null;

        Cursor resultCursor;
        String selection = "Corridor = ?";
        String[] selectionArgs = {streetName};
        String side = null;
        String timeFrom = null;
        String timeTo = null;
        // House number matches the number on the same side
        boolean[] weekOfMonthsSame = new boolean[5];
        boolean[] weekdaysSame = new boolean[8];
        ArrayList<LatLng> latLngsSame = new ArrayList<>();
        // House number matches the number on the other side
        boolean[] weekOfMonthsOther = new boolean[5];
        boolean[] weekdaysOther = new boolean[8];
        ArrayList<LatLng> latLngsOther = new ArrayList<>();
        boolean useOtherSide = true;
//        String groupBy = null;
//        String having = null;
//        String orderBy = null;
        if(database.isOpen()) {
            resultCursor = database.query(table, columns, selection, selectionArgs, null, null, null);
            System.out.println("Total records of "+" "+ streetName+" is: " +resultCursor.getCount());
            resultCursor.moveToFirst();
            if(!resultCursor.isAfterLast()) {
                int cnnRightLeftIndex = resultCursor.getColumnIndex("CNNRightLeft");
                int left_from_index = resultCursor.getColumnIndex("LF_FADD");
                do {
                    int lf_from = Integer.parseInt(resultCursor.getString(left_from_index    ));
                    int lf_to   = Integer.parseInt(resultCursor.getString(left_from_index + 1));
                    int rt_to   = Integer.parseInt(resultCursor.getString(left_from_index + 2));
                    int rt_from = Integer.parseInt(resultCursor.getString(left_from_index + 3));

                    // Decide the house number is on the left or right side
                    if (side == null) {
                        boolean leftIsOdd;
                        if (lf_from == 0 && lf_to == 0) {
                            if (rt_from == 0 && rt_to == 0) {
                                continue;
                            } else {
                                leftIsOdd = rt_from % 2 == 0;
                            }
                        } else {
                            leftIsOdd = lf_from % 2 != 0;
                        }
                        if (houseNumber % 2 == 0) {
                            side = leftIsOdd ? LETTER_R : LETTER_L;
                        } else {
                            side = leftIsOdd ? LETTER_L : LETTER_R;
                        }
                    }

                    // Filter the information of the other side
                    if (!resultCursor.getString(cnnRightLeftIndex).equals(side)) {
                        continue;
                    }

                    int lower_same, upper_same, lower_other, upper_other;
                    if (side.equals(LETTER_L)) {
                        lower_same = lf_from;
                        upper_same = lf_to;
                        lower_other = rt_from;
                        upper_other = rt_to;
                    } else {
                        lower_same = rt_from;
                        upper_same = rt_to;
                        lower_other = lf_from;
                        upper_other = lf_to;
                    }

                    if (lower_same > 0 || upper_same > 0) {
                        if (houseNumber >= lower_same && houseNumber <= upper_same) {
                            // Matches the number on the same side. Use the information
                            if (useOtherSide || timeFrom == null) { // If useOtherSide is true, we don't have correct information
                                timeFrom = resultCursor.getString(resultCursor.getColumnIndex("FromHour"));
                                timeTo = resultCursor.getString(resultCursor.getColumnIndex("ToHour"));
                                for (int i = 0; i < WEEK_OF_MONTH.length; i++) {
                                    String weekOfMonth = resultCursor.getString(resultCursor.getColumnIndex(WEEK_OF_MONTH[i]));
                                    if (weekOfMonth.equals(YES)) weekOfMonthsSame[i] = true;
                                }
                            }
                            String weekday = resultCursor.getString(resultCursor.getColumnIndex("Weekday")).toUpperCase();
                            weekdaysSame[WEEKDAYS_HASH.get(weekday)] = true;
                            addLatLng(resultCursor, latLngsSame);
                            useOtherSide = false; // No need to use the information below
                        }
                    } else { // lower_same == 0 && upper_same == 0
                        if (lower_other == 0 && upper_other == 0) {
                            continue;
                        }
                        if (houseNumber >= lower_other && houseNumber <= upper_other) {
                            // Doesn't match the number on the same side, but matches the other side. Save the information in case
                            if (timeFrom == null) {
                                timeFrom = resultCursor.getString(resultCursor.getColumnIndex("FromHour"));
                                timeTo = resultCursor.getString(resultCursor.getColumnIndex("ToHour"));
                                for (int i = 0; i < WEEK_OF_MONTH.length; i++) {
                                    String weekOfMonth = resultCursor.getString(resultCursor.getColumnIndex(WEEK_OF_MONTH[i]));
                                    if (weekOfMonth.equals(YES)) weekOfMonthsOther[i] = true;
                                }
                            }
                            String weekday = resultCursor.getString(resultCursor.getColumnIndex("Weekday")).toUpperCase();
                            weekdaysOther[WEEKDAYS_HASH.get(weekday)] = true;
                            addLatLng(resultCursor, latLngsOther);
                        }
                    }
                } while (resultCursor.moveToNext());

                if (useOtherSide) {
                    weekdaysSame = weekdaysOther;
                    weekOfMonthsSame = weekOfMonthsOther;
                    latLngsSame = latLngsOther;
                }
            }
            resultCursor.close();
        }

        return new Street(numberAndName, weekdaysSame, weekOfMonthsSame, side, timeFrom, timeTo, latLngsSame);
    }

    private void addLatLng(Cursor resultCursor, ArrayList<LatLng> latLngs) {
        String coordinates = resultCursor.getString(resultCursor.getColumnIndex("Coordinates"));
        for (String s : coordinates.split(SPACE)) {
            String[] sInside = s.split(COMMA);
            LatLng latLng = new LatLng(Double.parseDouble(sInside[1]), Double.parseDouble(sInside[0]));
            latLngs.add(latLng);
        }
    }

}
