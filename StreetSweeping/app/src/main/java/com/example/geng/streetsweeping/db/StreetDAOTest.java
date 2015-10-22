package com.example.geng.streetsweeping.db;

import com.example.geng.streetsweeping.Street;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geng on 10/22/15.
 */
public class StreetDAOTest implements StreetDAOInterface {

    @Override
    public List<Street> getStreetsByStreetName(String name) {
        List<Street> streets = new ArrayList<>();
        Street street = new Street();
        //street.
        streets.add(street);
        return null;
    }

    @Override
    public List<Street> getStreetsOnScreen(LatLngBounds latLngBounds) {
        return null;
    }
}
