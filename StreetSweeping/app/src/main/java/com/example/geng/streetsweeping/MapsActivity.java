package com.example.geng.streetsweeping;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.example.geng.streetsweeping.db.DBHelper;
import com.example.geng.streetsweeping.db.StreetDAO;
import com.example.geng.streetsweeping.db.StreetDAOInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {



    private static final float BLUE = BitmapDescriptorFactory.HUE_AZURE;
    private static final float RED  = BitmapDescriptorFactory.HUE_RED;
    private static final String PREFERENCES_FILE_NAME = "MyAppPreferences";
    private static final String PARK_LAT_KEY = "parkLatKey";
    private static final String PARK_LNG_KEY = "parkLngKey";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    GoogleApiClient mGoogleApiClient;
    Marker mMarker;
    Street mStreet;
    Marker mParkMarker;
    LatLng mParkLocation;
    Street mParkStreet;
    SharedPreferences sharedPreferences;
//    SharedPreferences.Editor preferenceEditor;

    Toolbar toolbar;

    //AlarmHolder alarmHolder;

    StreetViewer streetViewer;
    StreetDAOInterface streetDAO;

//    TextView streetNameTextView;
//    TextView sweepDateTextView;

    // Alarm
    AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("-------------onCreate-------------");
        setContentView(R.layout.main_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUpMapIfNeeded();
        // mMap is supposed to be not null since this point.

        streetViewer = new StreetViewer(mMap);
        streetDAO = new StreetDAO(new DBHelper(this));
//        streetNameTextView = (TextView) findViewById(R.id.streetname);
//        sweepDateTextView = (TextView) findViewById(R.id.sweepdate);
        //alarmHolder = new AlarmHolder(this);

        buildGoogleApiClient(); // Once client connected, will center map and show street name
        mGoogleApiClient.connect();
        //setUpStreets();

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    public void showParkMarker() {
        double temp_lat = Double.valueOf(sharedPreferences.getString(PARK_LAT_KEY, null));
        double temp_lng = Double.valueOf(sharedPreferences.getString(PARK_LNG_KEY, null));
        addMarkerAndInfoWindow(true, new LatLng(temp_lat, temp_lng), 0);
        System.out.println("-------------inside showParkerMarker-------------");
        return;
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(37.75, -122.45), 12, 0, 0)));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                ((TextView) v.findViewById(R.id.streetname)).setText(marker.getSnippet());
                Street street;
                if (marker.equals(mParkMarker)) {
                    street = mParkStreet;
                    ((TextView) v.findViewById(R.id.click_tip)).setText(R.string.cancel_alarm);
                } else {
                    street = mStreet;
                    ((TextView) v.findViewById(R.id.click_tip)).setText(R.string.set_alarm);
                }
                if (street != null) {
                    String sweepDate = street.getSweepDate();
                    if (sweepDate.length() == 0)
                        sweepDate = getString(R.string.invalid_date);
                    ((TextView) v.findViewById(R.id.sweep_date)).setText(sweepDate);
                    String sweepTime = street.getSweepTime();
                    if (sweepTime.length() == 0)
                        sweepTime = getString(R.string.invalid_time);
                    ((TextView) v.findViewById(R.id.sweep_time)).setText(sweepTime);
                    String nextTime = getNextSweepString();
                    if (nextTime.length() == 0)
                        nextTime = getString(R.string.invalid_next_time);
                    ((TextView) v.findViewById(R.id.nextsweep)).setText(nextTime);
                    ((TextView) v.findViewById(R.id.streetname)).setText(street.getStreetName());

                }
                return v;
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                updateStreet(latLng, false);
                mMap.clear();
                addMarkerAndInfoWindow(false, latLng, RED);
                if (mStreet != null) {
                    streetViewer.addStreet(mStreet, true);
                }
//                showAddressAndMarker(latLng, RED);
                if(mParkLocation != null) {
                    addMarkerAndInfoWindow(true,mParkLocation,0);
                }
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mMarker != null) {
                    mMarker.hideInfoWindow();
                }
                if (mParkMarker != null) {
                    mParkMarker.hideInfoWindow();
                }
            }
        });
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                }
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    updateStreet(locToLat(mLastLocation), false);
                    mMap.clear();
                    addMarkerAndInfoWindow(false, locToLat(mLastLocation), BLUE);
//                    showAddressAndMarker(mLastLocation, BLUE);
                    centerMap(mLastLocation); // If we don't want to zoom to 16, comment this line and return false.
//                } else {
//                    streetNameTextView.setText(R.string.address_unavailable);
                }
                if(mParkMarker != null) {
                    addMarkerAndInfoWindow(true, mParkLocation, 0);
                }
                return true;
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //marker.showInfoWindow(); // If we don't want to center to marker, uncomment this line and return true;
                return false;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.equals(mParkMarker)) {
                    // cancel dialog
                } else {
                    showAlert(marker);
                }
            }
        });
    }

//    /**
//     * Call {@link #getStreetName(LatLng)} to get address of a location.
//     * Then clear the map and redraw the street lines, and active one of them.
//     * Then call {@link #addMarkerAndInfoWindow(boolean, LatLng, float)}
//     * to add a Marker and an InfoWindow at the location.
//     * @param latLng The location.
//     * @param color Color of the Marker. Azure(blue) if current location; red if user click.
//     */
//    private void showAddressAndMarker(LatLng latLng, float color) {
////        String streetName = getString(R.string.address_unavailable);
////        String sweepDate = getString(R.string.date_unavailable);
////
//        String[] address = getStreetName(latLng);
//        mMap.clear();
//        if (address.length != 0) {
////            streetName = TextUtils.join(", ", address);
////
////            // query database get sweepDate
////            // mStreet could be null
//            mStreet = getStreetByAddress(address[0]);
//            if (mStreet != null) {
////                sweepDate = mStreet.getSweepTime() + " " + mStreet.getSweepDate();
////                // draw street
//                streetViewer.addStreet(mStreet, true);
//            }
//        }
////        streetNameTextView.setText(streetName);
////        sweepDateTextView.setText(sweepDate);
//        addMarkerAndInfoWindow(false, latLng, address[0], color);
//    }

    // Good!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private void updateStreet(LatLng latLng, boolean isPark) {
        String[] address = getStreetName(latLng);
        if (address.length != 0) {
            if (isPark) {
                mParkStreet = getStreetByAddress(address[0]);
            } else {
                mStreet = getStreetByAddress(address[0]);
            }
        }

    }

//    private void showAddressAndMarker(Location location, float color) {
//        showAddressAndMarker(locToLat(location), color);
//    }

    /**
     * Center the map to the location, zoom to 16, without tilt or bearing.
     * @param location the new center of the map.
     */
    private void centerMap(Location location) {
        CameraPosition cameraPosition = new CameraPosition(locToLat(location), 16, 0, 0);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Get address information of a location by Geocoder service.
     * @param latLng A location on the map.
     * @return A String array containing address lines or non-found hint.
     */
    private String[] getStreetName(LatLng latLng) {
        String[] address;
        Geocoder geocoder = new Geocoder(MapsActivity.this.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addressList != null && !addressList.isEmpty()) {
                address = new String[addressList.get(0).getMaxAddressLineIndex()];
                for (int i = 0; i < address.length; i++) {
                    address[i] = addressList.get(0).getAddressLine(i);
                }
                return address;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    /**
     * Get Street object from database.
     * @param numberAndName Example: "221 Baker St", "100-130 19th Ave"
     * @return Street object
     */
    private Street getStreetByAddress(String numberAndName) {
        return streetDAO.getStreetsByAddress(numberAndName);
    }

    /**
     * Add a Marker at a location on the map and popup the InfoWindow.
     * @param latLng A location on the map.
     * @param color Color of the Marker.
     */
    private void addMarkerAndInfoWindow(boolean park, LatLng latLng, float color) {
        if (park) {

            mParkMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
        } else {
            mMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
            mMarker.showInfoWindow();
        }
    }

    private String getNextSweepString() {
        if (mStreet == null) return "";
        Calendar nextSweepCalendar = mStreet.getNextSweepCalendar();
        if (nextSweepCalendar == null) {
            return getString(R.string.invalid_next_time);
        }
        Calendar now = Calendar.getInstance();
        int period = (int) (nextSweepCalendar.getTimeInMillis() / 1000 - now.getTimeInMillis() / 1000);
        int days = period / (60 * 60 * 24);
        period %= (60 * 60 * 24);
        int hrs = period / (60 * 60);
        period %= (60 * 60);
        int mins = period / 60;
        return String.format(getString(R.string.next_time), days, hrs, mins);
    }

    private void showAlert(Marker marker) {

        /**
         * An AlertDialog builder is used to build up the details of the modal
         * dialog such as title, a message and an icon. It is possible to add
         * one or more buttons to the modal dialog.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.set_alarm_title)
                .setMessage(R.string.set_alarm_description)
                .setIcon(android.R.drawable.ic_lock_idle_alarm);
        AlertDialog dlg = builder.create();
        final Marker parkMarker = marker;
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // remove alarm
                removeAlarm();
                // set up alarm
                setAlarm();

                mParkStreet = mStreet;
                //remove park marker
                removeParkMarker();
                // set up park marker
                setParkMarker(parkMarker.getPosition());
            }
        });

        dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        /**
         * Show the modal dialog. Once the user has clicked on a button, the
         * dialog is automatically removed.
         */
        dlg.show();
    }

    private void setAlarm() {
        if (mStreet == null) return;
        Calendar calendar = mStreet.getNextSweepCalendar();
        if (calendar == null) return;
        Intent myIntent = new Intent(MapsActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MapsActivity.this, 0, myIntent, 0);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
    }

    private void removeAlarm() {
        if (pendingIntent != null)
            alarmManager.cancel(pendingIntent);
    }

    private void removeParkMarker() {
        if(mParkMarker != null) {
            mParkMarker.remove();
            mParkMarker = null;
            mParkStreet = null;
            if (sharedPreferences != null) {
                sharedPreferences.edit().clear().apply();
            }
        }
    }
    //also added the positions to sharedPreference
    private void setParkMarker(LatLng latLng) {
        addMarkerAndInfoWindow(true, latLng, 0);
        mParkLocation = latLng;
        String lat = String.valueOf(latLng.latitude);
        String lng = String.valueOf(latLng.longitude);

        if (sharedPreferences == null) {
            sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCES_FILE_NAME, 0);
//            preferenceEditor = sharedPreferences.edit();
        }

        sharedPreferences.edit()
                .putString(PARK_LAT_KEY, lat)
                .putString(PARK_LNG_KEY, lng)
                .apply();

//        if(sharedPreferences.contains(PARK_LNG_KEY)) {
//            System.out.println("-------------shared preference has lng!-------------");
//            System.out.println("-------------real lng-------------" + latLng.longitude);
//            System.out.println("-------------lng shared preference!-------------" + sharedPreferences.getString(PARK_LNG_KEY, null));
//        }

    }

    public void cancelAlarm(View view) {
        if (mParkMarker != null) {
            removeParkMarker();
            removeAlarm();
        }
    }

    private LatLng locToLat(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("Connection Fails!!");
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //userMoveMap = false;
//            showAddressAndMarker(mLastLocation, BLUE);
            updateStreet(locToLat(mLastLocation), false);
            mMap.clear();
            centerMap(mLastLocation);
            addMarkerAndInfoWindow(false, locToLat(mLastLocation), BLUE);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connection Suspended!!");
    }
}
