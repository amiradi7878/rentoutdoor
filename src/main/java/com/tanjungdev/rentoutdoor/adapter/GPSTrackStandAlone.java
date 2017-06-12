package com.tanjungdev.rentoutdoor.adapter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

/**
 * Created by everjn on 5/13/16.
 */
public class GPSTrackStandAlone extends Service implements LocationListener {
    public Context mContext;
    public boolean canGetLocation = false;
    public Location mLocation;
    LocationRequest mLocationRequest;
    public double mLatitude;
    public double mLongitude;
    // public static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 10;
    // public static final long MIN_TIME_FOR_UPDATE = 60000;
    // public LocationManager mLocationManager;
    public static final long INTERVAL = 1000 * 10;
    public static final long FASTEST_INTERVAL = 1000 * 5;

    public GPSTrackStandAlone(Context mContext) {

        this.mContext = mContext;

    }


    public Location getLocation(Context mContext) {
        LocationRequest mLocationRequest;
        GoogleApiClient mGoogleApiClient;
        // Context mContext;
        boolean canGetLocation = false;
        Location mLocation = new Location("location");
        double mLatitude = 1;
        double mLongitude = 2;
        final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 10;
        final long MIN_TIME_FOR_UPDATE = 60000;
        LocationManager mLocationManager;
        final long INTERVAL = 1000 * 10;
        final long FASTEST_INTERVAL = 1000 * 5;

        this.mContext = mContext;
        if(Looper.myLooper()==null){
            Looper.prepare();
        }
        try {

            mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            /*getting status of the gps*/
            Boolean isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            /*getting status of network provider*/
            Boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsEnabled && !isNetworkEnabled) {

                /*no location provider enabled*/
            } else {

                this.canGetLocation = true;

                /*getting location from network provider*/
                if (isNetworkEnabled) {


                        mLocationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_FOR_UPDATE,
                                MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                        Log.d("Perms", "Permission for GPS Granted");

                        if (mLocationManager != null) {

                            mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            if (mLocation != null) {
                                mLatitude = mLocation.getLatitude();
                                mLongitude = mLocation.getLongitude();
                            }

                        }

                    /*if gps is enabled then get location using gps*/
                    if (isGpsEnabled) {
                        if (mLocation == null) {
                            mLocationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_FOR_UPDATE,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (mLocationManager != null) {
                                mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (mLocation != null) {
                                    mLatitude = mLocation.getLatitude();
                                    mLongitude = mLocation.getLongitude();
                                }
                                Log.d("getLocation","Latitude "+mLatitude+", Longitude "+mLongitude);

                            }
                        }

                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return mLocation;
    }


    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            Toast.makeText(GPSTrackStandAlone.this, "isGooglePlayServiceAvailable = False", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }



    public double getLatitude() {
        if (mLocation != null) {

            mLongitude = mLocation.getLongitude();
            Log.d("MGPS", String.valueOf(mLocation.getLatitude()));
        }
        return mLongitude;
    }

    public double getLongitude() {
        if (mLocation != null) {

            mLongitude = mLocation.getLongitude();
            Log.d("MGPS", String.valueOf(mLocation.getLatitude()));
        }

        return mLongitude;
    }


    public boolean canGetLocation() {

        return this.canGetLocation;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub

    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

}
