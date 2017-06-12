package com.tanjungdev.rentoutdoor.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tanjungdev.rentoutdoor.fragment.DetailViewFragment;
import com.tanjungdev.rentoutdoor.util.Constants;
import com.tanjungdev.rentoutdoor.util.UtilMethods;
import com.tanjungdev.rentoutdoor.R;
import com.tanjungdev.rentoutdoor.util.PathJSONParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class MapActivity extends FragmentActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Marker currentLocationMarker;
    private LatLng itemLocation;
    public double latitude;
    public double longitude;
    private boolean isUpdated = false;
    private String distance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        ((TextView) findViewById(R.id.itemHeadingTV)).setText(DetailViewFragment.itemDetails.getTitle());
        if (DetailViewFragment.itemDetails == null) {
            itemLocation = new LatLng(Constants.DUMMY_LOCATION_LATITUDE,
                    Constants.DUMMY_LOCATION_LONGITUDE);
        }else{
            itemLocation = new LatLng(DetailViewFragment.itemDetails.getLatitude(), DetailViewFragment.itemDetails.getLongitude());
        }

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }else{
            Log.d("GooglePlayService OK","yee");
        }

        createLocationRequest();
        createGoogleApiClient();

    }
/*
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }*/




    public boolean checkLocationPermission()
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }



    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
    }


    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        String permissions[] = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (EasyPermissions.hasPermissions(this, permissions)) {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.gpsReq),1 ,permissions );

        }

        //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        //checkLocationPermission();



        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

        }

    }


    @Override
    public void onConnectionSuspended(int i) {

    }
    public double getLatitudeSend() {//error disini
            //onCreate(new Bundle());

            latitude = mCurrentLocation.getLatitude();


        Log.d("LatitudeMapAct", String.valueOf(mCurrentLocation.getLatitude()));
        return latitude;
    }

    public double getLongitudeSend() {
           // onCreate(new Bundle());
            longitude = mCurrentLocation.getLongitude();

        Log.d("LongitudeMapAct",String.valueOf(mCurrentLocation.getLongitude()));
        return longitude;
    }



    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (!isUpdated) {
            isUpdated = true;
            updateMap();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected void stopLocationUpdates() {
        //if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    private void updateMap() {
        setUpMapIfNeeded();
        MarkerOptions options = new MarkerOptions();
        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        options.position(currentLatLng);
        Log.d("positionMapAct",String.valueOf(currentLatLng));
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_source));
        if (currentLocationMarker != null)
            currentLocationMarker.remove();
        currentLocationMarker = mMap.addMarker(options);

        if (UtilMethods.isUserSignedIn(MapActivity.this)) {
        } else
            currentLocationMarker.setTitle(getResources().getString(R.string.you_string));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLatLng)
                .tilt(90)
                .zoom(17)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        addTargetMarker();
        new GetPathDefault().execute();
        //        getPathFromGoogle();

    }

    private void addTargetMarker() {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(itemLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_destination))
                    .title(DetailViewFragment.itemDetails.getTitle()))
                    .setSnippet(DetailViewFragment.itemDetails.getAddress());
        }
    }

    private String getMapsApiDirectionsUrl() {

        String str_origin = "origin=" + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude();
        String str_dest = "destination=" + itemLocation.latitude + "," + itemLocation.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        Log.d("latitudeDirectional",String.valueOf(mCurrentLocation.getLongitude()));
        return url;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        setUpMapIfNeeded();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    private class GetPathDefault extends AsyncTask {

        URL url = null;
        BufferedReader reader = null;
        StringBuilder stringBuilder;

        @Override
        protected Object doInBackground(Object[] params) {

            try {
                url = new URL(getMapsApiDirectionsUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    throw e;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            new ParserTask().execute(stringBuilder.toString());
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                distance = jObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text");
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(10);
                polyLineOptions.color(getResources().getColor(R.color.map_polyline_color));

                if (UtilMethods.isUserSignedIn(MapActivity.this)) {
                    //set username
                }

                ((TextView) findViewById(R.id.distanceTV)).setText(distance);
                findViewById(R.id.awayTV).setVisibility(View.VISIBLE);
            }

            mMap.addPolyline(polyLineOptions);
        }
    }

}
