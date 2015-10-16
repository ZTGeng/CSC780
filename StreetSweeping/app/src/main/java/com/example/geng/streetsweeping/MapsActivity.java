package com.example.geng.streetsweeping;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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

public class MapsActivity extends FragmentActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final float BLUE = BitmapDescriptorFactory.HUE_AZURE;
    private static final float RED  = BitmapDescriptorFactory.HUE_RED;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    GoogleApiClient mGoogleApiClient;
    Marker mMarker;
    boolean userMoveMap;

    StreetViewer streetViewer;
    StreetDAO streetDAO;

    TextView streetNameTextView;
    TextView sweepDateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("-------------onCreate-------------");
        setContentView(R.layout.main_layout);
        userMoveMap = true;

        setUpMapIfNeeded();

        streetViewer = new StreetViewer(mMap);
        streetDAO = new StreetDAO(new DBHelper(this));
        streetDAO.createDB();
        streetNameTextView = (TextView) findViewById(R.id.streetname);
        sweepDateTextView = (TextView) findViewById(R.id.sweepdate);

        buildGoogleApiClient(); // Once client connected, will center map and show street name
        mGoogleApiClient.connect();
        setUpStreets();

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
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(37.75, -122.45), 12, 0, 0)));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                showAddressAndMarker(latLng, RED);
            }
        });
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                System.out.println("Camera is Changing!!!!!!!");
                if (userMoveMap) {
                    if (mMarker != null) {
                        System.out.println("Trying to hide infoWindow!!");
                        mMarker.hideInfoWindow();
                    }
                } else {
                    userMoveMap = true;
                }
                if (cameraPosition.zoom > 15) {
                    setUpStreets();
                }
                // Test!!
                String str = cameraPosition.target.latitude + " " + cameraPosition.target.longitude +
                        " Z: " + cameraPosition.zoom + " b: " + cameraPosition.bearing;
                sweepDateTextView.setText(str);
            }
        });
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                userMoveMap = false;
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    showAddressAndMarker(mLastLocation, BLUE);
                }
                centerMap(mLastLocation); // If we don't want to zoom to 16, comment this line and return false.
                return true;
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                userMoveMap = false;
                //marker.showInfoWindow(); // If we don't want to center to marker, uncomment this line and return true;
                return false;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                // Set Alert!
            }
        });
    }

    /**
     * Center the map to the location, zoom to 16, without tilt or bearing.
     * @param location the new center of the map.
     */
    private void centerMap(Location location) {
        CameraPosition cameraPosition = new CameraPosition(locToLat(location), 16, 0, 0);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setUpStreets() {
        List<Street> streets = streetDAO.getStreetsOnScreen(mMap.getProjection().getVisibleRegion().latLngBounds);
        streetViewer.addStreets(streets);
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
        address = new String[] { getString(R.string.invalid_address) };
        return address;
    }

    /**
     * Add a Marker at a location on the map and popup the InfoWindow.
     * @param latLng A location on the map.
     * @param color Color of the Marker.
     * @param streetName Address of the location to be shown on infoWindow.
     */
    private void addMarkerAndInfoWindow(LatLng latLng, float color, String streetName) {
        mMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .title(streetName).snippet(getString(R.string.invalid_date)));// + System.getProperty("line.separator") + "Set Alert!"));

        mMarker.showInfoWindow();
    }

    /**
     * Call {@link #getStreetName(LatLng)} to get address of a location,
     * then set {@link #streetNameTextView} with the whole address.
     * Then clear the map and redraw the street lines, and active one of them.
     * Then call {@link #addMarkerAndInfoWindow(LatLng, float, String)}
     * to add a Marker and an InfoWindow at the location.
     * @param latLng The location.
     * @param color Color of the Marker. Azure(blue) if current location; red if user click.
     */
    private void showAddressAndMarker(LatLng latLng, float color) {
        String[] address = getStreetName(latLng);
        streetNameTextView.setText(TextUtils.join(", ", address));
        mMap.clear();
        setUpStreets();
        // active street near latLng
        addMarkerAndInfoWindow(latLng, color, address[0]);
    }

    private void showAddressAndMarker(Location location, float color) {
        showAddressAndMarker(locToLat(location), color);
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
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            userMoveMap = false;
            centerMap(mLastLocation);
            showAddressAndMarker(mLastLocation, BLUE);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connection Suspended!!");
    }
}
