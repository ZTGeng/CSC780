package com.example.geng.streetsweeping.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.geng.streetsweeping.Street;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

import static java.lang.System.*;

/**
 * Created by geng on 10/6/15.
 */
public class StreetDAO implements StreetDAOInterface {

    private static HashMap<String, Integer> WEEKDAYS = new HashMap<>();
    private static String[] WEEK_OF_MONTH = {"Week1OfMonth", "Week2OfMonth", "Week3OfMonth", "Week4OfMonth", "Week5OfMonth"};
    private static String table = "StreetSweepData";
    private static String[] columns = {"GeneralInfo", "Weekday", "BlockSide", "CNNRightLeft", "Corridor",
            "FromHour", "ToHour", "Week1OfMonth", "Week2OfMonth", "Week3OfMonth", "Week4OfMonth", "Week5OfMonth",
            "LF_FADD", "LF_TOADD", "RT_TOADD", "RT_FADD", "ZipCode", "Coordinates"};
    private final static String YES = "Yes";
    private final static String LETTER_L = "L";
    private final static String LETTER_R = "R";
    private final static String SPACE = " ";
    private final static String COMMA = ",";

    static {
        WEEKDAYS.put("SUN", 0);
        WEEKDAYS.put("MON", 1);
        WEEKDAYS.put("TUES", 2);
        WEEKDAYS.put("WED", 3);
        WEEKDAYS.put("THU", 4);
        WEEKDAYS.put("FRI", 5);
        WEEKDAYS.put("SAT", 6);
    }

    DBHelper dbHelper;
    SQLiteDatabase database;

    public StreetDAO(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        database = dbHelper.openDataBase();
        //testDB();
    }

    public void testDB() {
        out.println("testing db here");
        out.println(this.getClass()+ " gets " +database.getPath());
        out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        Street test = getStreetsByAddress("10th Ave", 99);
        out.println(test.toString());
        out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        return;
    }

    public Street getStreetsByAddress(String streetName, int houseNumber) {
        //null input checking
        if(streetName == null || streetName.isEmpty()) return null;

        Street street = null;
        Cursor resultCursor;
        String selection = "Corridor = ?";
        String[] selectionArgs = {streetName};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        street = new Street(streetName);
        if(database.isOpen()) {
            resultCursor = database.query(table, columns, selection, selectionArgs, null, null, null);
            System.out.println("Total records of "+" "+ streetName+" is: " +resultCursor.getCount());
            resultCursor.moveToFirst();
            if(!resultCursor.isAfterLast()) {
                int cnnRightLeftIndex = resultCursor.getColumnIndex("CNNRightLeft");
                int left_from_index = resultCursor.getColumnIndex("LF_FADD");
                do {
                    int lf_from =Integer.parseInt(resultCursor.getString(left_from_index));
                    int lf_to = Integer.parseInt(resultCursor.getString(left_from_index + 1));
                    int rt_to = Integer.parseInt(resultCursor.getString(left_from_index+2));
                    int rt_from = Integer.parseInt(resultCursor.getString(left_from_index+3));
                    int lower_address = Math.min(lf_from, rt_from);
                    int upper_address = Math.max(lf_to,rt_to);

                    if(houseNumber >= lower_address && houseNumber <= upper_address) {
                        String side = resultCursor.getString(cnnRightLeftIndex);
                        String weekday = resultCursor.getString(resultCursor.getColumnIndex("Weekday"));
                        if (houseNumber % 2 == lf_from % 2) {
                            if (side.equals(LETTER_L)) {
                                street.addWeekday(WEEKDAYS.get(weekday.toUpperCase()));
                                if (street.getBlockSide() == null) {
                                    constructStreet(resultCursor, street, lf_from, lf_to, side);
                                }
                            }
                        } else {
                            if (side.equals(LETTER_R)) {
                                street.addWeekday(WEEKDAYS.get(weekday.toUpperCase()));
                                if (street.getBlockSide() == null) {
                                    constructStreet(resultCursor, street, lf_from, lf_to, side);
                                }
                            }
                        }
                    }
                } while (resultCursor.moveToNext());
            }
        }
        return street;
    }
    private void constructStreet (Cursor resultCursor, Street street, int addressFrom, int addressTo, String side) {
        String blockSide = resultCursor.getString(resultCursor.getColumnIndex("BlockSide"));
        street.setBlockSide(blockSide);
        street.setSide(side);
        street.setTimeFrom(resultCursor.getString(resultCursor.getColumnIndex("FromHour")));
        street.setTimeTo(resultCursor.getString(resultCursor.getColumnIndex("ToHour")));
        for (int i = 0; i < WEEK_OF_MONTH.length; i++) {
            String weekOfMonth = resultCursor.getString(resultCursor.getColumnIndex(WEEK_OF_MONTH[i]));
            if (weekOfMonth.equals(YES)) street.addWeekOfMonth(i);
        }
        String coordinates = resultCursor.getString(resultCursor.getColumnIndex("Coordinates"));
        for (String s : coordinates.split(SPACE)) {
            String[] sInside = s.split(COMMA);
            LatLng latLng = new LatLng(Double.parseDouble(sInside[1]), Double.parseDouble(sInside[0]));
            street.addLatLngs(latLng);
        }
    }

//    public List<Street> getStreetsOnScreen(LatLngBounds latLngBounds) {
//        List<Street> streets = new ArrayList<>();
//
//        // for (LatLng latLng : LatLngs) {
//        //    if (latLngBounds.contains(latLng) && ) {
//        //        streets.add(new Street());
//        //    }
//        // }
//
//        return streets;
//    }

}
