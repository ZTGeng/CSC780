package com.example.geng.streetsweeping;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * Created by geng on 10/6/15.
 */
public class StreetViewer {
    GoogleMap mMap;

    public StreetViewer(GoogleMap map) {
        mMap = map;
    }

    void addStreets(List<Street> streets) {
        for (Street street : streets) {
            addStreet(street);
        }
    }

    void addStreet(Street street, boolean active) {
        mMap.addPolyline(new PolylineOptions().add(street.latLngs).color(active ? 0 : 1));
    }

    void addStreet(Street street) {
        addStreet(street, false);
    }

    void switchStreet(Street prev, Street post) {
        addStreet(prev);
        addStreet(post, true);
    }
}
