package com.example.geng.streetsweeping;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * Created by geng on 10/6/15.
 */
public class StreetDrawer {
    public static int streetWidth = 5;
    private final static int RED = 0xffff0000;
    private final static int GREY = 0xff808080;

    GoogleMap mMap;

    public StreetDrawer(GoogleMap map) {
        mMap = map;
    }

    void addStreet(Street street, boolean active) {
        List<LatLng> latLngs = street.getLatLngs();
        for (int i = 1; i < latLngs.size(); i++) {
            mMap.addPolyline(new PolylineOptions().add(latLngs.get(i - 1), latLngs.get(i))
                    .width(streetWidth)
                    .color(active ? RED : GREY));
        }
    }

}
