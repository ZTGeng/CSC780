package com.example.geng.streetsweeping.db;

import com.example.geng.streetsweeping.Street;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;

/**
 * Created by geng on 10/22/15.
 */
public interface StreetDAOInterface {
    List<Street> getStreetsByStreetName(String name);
    List<Street> getStreetsOnScreen(LatLngBounds latLngBounds);
}
