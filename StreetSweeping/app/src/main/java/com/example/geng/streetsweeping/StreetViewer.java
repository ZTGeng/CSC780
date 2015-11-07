package com.example.geng.streetsweeping;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * Created by geng on 10/6/15.
 */
public class StreetViewer {
    public static int streetWidth = 5;
    private final static int RED = 0xffff0000;
    private final static int GREY = 0xff808080;

    GoogleMap mMap;

    public StreetViewer(GoogleMap map) {
        mMap = map;
    }

    void addStreets(List<Street> streets) {
        if (mMap.getCameraPosition().zoom < 15) return;

        for (Street street : streets) {
            addStreet(street);
        }
    }

    void addStreet(Street street, boolean active) {
        PolylineOptions polylineOptions = new PolylineOptions();
        for (LatLng latLng : street.getLatLngs()) {
            polylineOptions.add(latLng);
        }
        polylineOptions.width(streetWidth); // default: 10
        polylineOptions.color(active ? RED : GREY);
        mMap.addPolyline(polylineOptions);
    }

    void addStreet(Street street) {
        addStreet(street, false);
    }

    void activeStreet(Street street) {
        addStreet(street, true);
    }

    void switchStreet(Street prev, Street post) {
        if (prev != null) {
            addStreet(prev);
        }
        addStreet(post, true);
    }

}
