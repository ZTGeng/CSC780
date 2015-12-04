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
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
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
    LatLng mToParkLocation;
    SharedPreferences sharedPreferences;

    Toolbar toolbar;

    StreetViewer streetViewer;
    StreetDAOInterface streetDAO;

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

        buildGoogleApiClient(); // Once client connected, will center map and show street name
        mGoogleApiClient.connect();

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

    /**
     * This is where we can add markers or lines, add listeners or move the camera
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
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
                mToParkLocation = latLng;
                updatemStreet(latLng);
                mMap.clear();
                if (mStreet == null || mStreet.getLatLngs().isEmpty()) {
                    addDefaultMarker(latLng, RED);
                } else {
                    streetViewer.addStreet(mStreet, true);
                    addArrowMarker();
                }
                if(mParkMarker != null) {
                    addParkMarker(mParkLocation);
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
                    mToParkLocation = locToLat(mLastLocation);
                    updatemStreet(locToLat(mLastLocation));
                    mMap.clear();
//                    addDefaultMarker(locToLat(mLastLocation), BLUE);
                    if (mStreet == null || mStreet.getLatLngs().isEmpty()) {
                        addDefaultMarker(locToLat(mLastLocation), BLUE);
                    } else {
                        streetViewer.addStreet(mStreet, true);
                        addArrowMarker();
                    }
                    centerMap(mLastLocation); // If we don't want to zoom to 16, comment this line and return false.
                }
                if(mParkMarker != null) {
                    addParkMarker(mParkLocation);
                }
                return true;
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Todo: if infoWindow is shown, flip the arrow; if not , show InfoWindow
                //marker.showInfoWindow(); // If we don't want to center to marker, uncomment this line and return true;
                return false;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.equals(mParkMarker)) {
                    cancelAlarm(null);
                } else {
                    showAlert(marker);
                }
            }
        });
    }

    private void updatemStreet(LatLng latLng) {
        String[] address = getStreetName(latLng);
        if (address.length != 0) {
            mStreet = getStreetByAddress(address[0]);
            if (mStreet.getLatLngs().isEmpty()) {
                mStreet = null;
            } else if (distance(mStreet.getLatLngs().get(0), latLng) > 0.01) {
                mStreet = null;
            }
        }
    }

    private double distance(LatLng a, LatLng b) {
        double y = b.latitude - a.latitude;
        double x = b.longitude - a.longitude;
        return Math.sqrt(y * y + x * x);
    }

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

    // Add marker of one of three kinds

    private void addDefaultMarker(LatLng latLng, float color) {
        mMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
        mMarker.showInfoWindow();
    }

    private void addArrowMarker() {
        if (mStreet == null || mStreet.getLatLngs().isEmpty()) return;
        LatLng start = mStreet.getLatLngs().get(0);
        LatLng end = mStreet.getLatLngs().get(mStreet.getLatLngs().size() - 1);
        LatLng middle = new LatLng((start.latitude + end.latitude) / 2, (start.longitude + end.longitude) / 2);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(middle);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow));

        double y = end.latitude - start.latitude;
        double x = end.longitude - start.longitude;
        float rotation;
        if (mStreet.getSide().equals("L")) {
            y = -y;
            x = -x;
        }
        rotation = (float) Math.toDegrees(Math.atan2(y, x)) * -1 + 90;
        markerOptions.rotation(rotation);

        double alpha = 0.5 / Math.sqrt(y * y + x * x);
        markerOptions.infoWindowAnchor((float) (0.5 - x * alpha), (float) (0.5 - y * alpha));

        mMarker = mMap.addMarker(markerOptions);
        mMarker.showInfoWindow();
    }

    private void addParkMarker(LatLng latLng) {
        mParkMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car24))
                .anchor(.5f, .5f));
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

    private void showAlert(final Marker marker) {

        /**
         * An AlertDialog builder is used to build up the details of the modal
         * dialog such as title, a message and an icon. It is possible to add
         * one or more buttons to the modal dialog.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.set_alarm_title)
                .setMessage(R.string.set_alarm_description)
                .setIcon(android.R.drawable.ic_lock_idle_alarm);
        AlertDialog dlg = builder.create();
//        final Marker parkMarker = marker;
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeAlarm();
                setAlarm();

                removeParkMarker();
//                mParkStreet = mStreet;
//                setParkMarker(parkMarker.getPosition());
                if (mToParkLocation == null) {
                    mToParkLocation = marker.getPosition();
                }
                setParkMarker(mToParkLocation);
                marker.hideInfoWindow();
                mParkMarker.showInfoWindow();
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
            mParkLocation = null;
            if (sharedPreferences != null) {
                sharedPreferences.edit().clear().apply();
            }
        }
    }
    //also added the positions to sharedPreference
    private void setParkMarker(LatLng latLng) {
        addParkMarker(latLng);
        mParkStreet = mStreet;
        mParkLocation = latLng;
        String lat = String.valueOf(latLng.latitude);
        String lng = String.valueOf(latLng.longitude);

        if (sharedPreferences == null) {
            sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        }

        sharedPreferences.edit()
                .putString(PARK_LAT_KEY, lat)
                .putString(PARK_LNG_KEY, lng)
                .apply();

    }

    public void cancelAlarm(View view) {
        if (mParkMarker != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.cancel_alarm_title)
                    .setMessage(R.string.cancel_alarm_description)
                    .setIcon(android.R.drawable.ic_lock_idle_alarm);
            AlertDialog dlg = builder.create();

            dlg.setButton(DialogInterface.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeParkMarker();
                    removeAlarm();

                }
            });

            dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            /**
             * Show the modal dialog. Once the user has clicked on a button, the
             * dialog is automatically removed.
             */
            dlg.show();

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.cancel_alarm_title)
                    .setMessage(R.string.cancel_when_no_alarm)
                    .setIcon(android.R.drawable.ic_lock_idle_alarm);
            AlertDialog dlg = builder.create();

            dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
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
            mToParkLocation = locToLat(mLastLocation);
            updatemStreet(locToLat(mLastLocation));
            mMap.clear();
            centerMap(mLastLocation);
//            addDefaultMarker(locToLat(mLastLocation), BLUE);
            if (mStreet == null || mStreet.getLatLngs().isEmpty()) {
                addDefaultMarker(locToLat(mLastLocation), BLUE);
            } else {
                streetViewer.addStreet(mStreet, true);
                addArrowMarker();
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connection Suspended!!");
    }
}
