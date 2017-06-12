package com.tanjungdev.rentoutdoor.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.tanjungdev.rentoutdoor.adapter.ByRentalAdapter;
import com.tanjungdev.rentoutdoor.model.Category;
import com.tanjungdev.rentoutdoor.util.UtilMethods;
import com.google.android.gms.location.LocationListener;
import com.tanjungdev.rentoutdoor.R;
import com.tanjungdev.rentoutdoor.activity.HomeActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class ByRentalFragment extends Fragment implements UtilMethods.InternetConnectionListener, LocationListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final int CATEGORY_ACTION = 1;
    private CategorySelectionCallbacks mCallbacks;
    private ArrayList<Category> categoryList;
    private ListView categoryListView;
    private String Error = null;
    private UtilMethods.InternetConnectionListener internetConnectionListener;
    private HomeActivity hAct;
    private Location locationGPS;
    public double distance;
    private LocationManager mLocationManager;

    private LocationListener mLocationListener;

    public Criteria criteria;
    public String bestProvider;

    public ByRentalFragment() {

    }

    public static ByRentalFragment newInstance(int sectionNumber) {
        ByRentalFragment fragment = new ByRentalFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //((HomeActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        try {
            mCallbacks = (CategorySelectionCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement CategorySelectionCallbacks.");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        categoryListView = (ListView) rootView.findViewById(R.id.categoryListView);
        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();
        if (UtilMethods.isConnectedToInternet(getActivity())) {
            initCategoryList();
        } else {
            internetConnectionListener = (UtilMethods.InternetConnectionListener) ByRentalFragment.this;
            UtilMethods.showNoInternetDialog(getActivity(), internetConnectionListener,
                    getResources().getString(R.string.no_internet),
                    getResources().getString(R.string.no_internet_text),
                    getResources().getString(R.string.retry_string),
                    getResources().getString(R.string.exit_string), CATEGORY_ACTION);
        }

    }

    public Location gpsPerm(){

        // String bestProvider = String.valueOf(mLocationManager.getBestProvider(criteria, true)).toString();
        String permissions[] = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        criteria = new Criteria();
        mLocationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        bestProvider = String.valueOf(mLocationManager.getBestProvider(criteria, true)).toString();
        List<String> providers = mLocationManager.getProviders(true);

        for (String provider : providers) {
            if (EasyPermissions.hasPermissions(getActivity(), permissions)) {
                Location l = mLocationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;

                }
                if (locationGPS == null || l.getAccuracy() < locationGPS.getAccuracy()) {
                    // Found best last known location: %s", l);
                    locationGPS = l;
                    //Log.d("gpsPerm","LocationGPS : " + String.valueOf(locationGPS) );
                }
                // getLatitudeSend();
                // getLongitudeSend();

                onLocationChanged(locationGPS);
                return locationGPS;

            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.gpsReq), 1, permissions);
            }
        }


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {

        }
       // if(locationGPS==null){
      //      locationGPS.setLatitude(-7.7602082);
     //       locationGPS.setLongitude(110.406685);
      //  }
        return locationGPS;
    }

    @Override
    public void onLocationChanged(Location locationGPS) {

        if (locationGPS!=null) {
            String longitude = "Longitude: " + locationGPS.getLongitude();
            Log.d("HomeGPS", longitude);
            String latitude = "Latitude: " + locationGPS.getLatitude();
            Log.d("HomeGPS", latitude);
        }else{
            Log.d("locationNull","locationNull");
        }
    }

    public class getCategList extends AsyncTask<Void, Void, Void>{
        private Handler mHandler = new Handler(Looper.getMainLooper());

        @Override
        protected Void doInBackground(Void... params) {
            locationGPS = gpsPerm();
            if (locationGPS!=null) {
                Log.d("byRentalGPSLat",String.valueOf(locationGPS.getLatitude()));

                Log.d("byRentalGPSLong",String.valueOf(locationGPS.getLongitude()));
            }


            URL hp = null;
            try {
                hp = new URL(
                        "https://amolpedi.000webhostapp.com/" + "byrental.php");

                Log.d("URL", "" + hp);
                URLConnection hpCon = hp.openConnection();
                hpCon.connect();
                InputStream input = hpCon.getInputStream();

                BufferedReader r = new BufferedReader(new InputStreamReader(input));

                String x = "";
                x = r.readLine();
                String total = "";

                while (x != null) {
                    total += x;
                    x = r.readLine();
                }
                Log.d("UR1L", "" + total);

                JSONArray j = new JSONArray(total);

                Log.d("URL1", "" + j.length());



                categoryList = new ArrayList<Category>();

                for (int i = 0; i < j.length(); i++) {
                    Category category = new Category();
                    final double rentLat;
                    final double rentLong;


                    JSONObject Obj;
                    Obj = j.getJSONObject(i);

                    category.setId(Obj.getString("rental_id"));
                    category.setTitle(Obj.getString("rental_title"));
                    rentLat = (Double.parseDouble(Obj.getString("rental_Lat")));
                    rentLong = (Double.parseDouble(Obj.getString("rental_Long")));

                    //category.setIconUrl(Obj.getString(JF_ICON));

                    if (!TextUtils.isEmpty(Obj.getString("thumbImage1"))) {
                        category.setImageUrl(Obj.getString("thumbImage1"));
                    }

                        mHandler.post(new Runnable() {
                            public void run() {
                                Log.d("LatLongRental", String.valueOf(rentLat));

                                Location trgtLocation = new Location("trgtLocation");
                                trgtLocation.setLatitude(rentLat);
                                trgtLocation.setLongitude(rentLong);

                                if(locationGPS!=null) {
                                    Log.d("crntLocation", String.valueOf(locationGPS));

                                    distance = locationGPS.distanceTo(trgtLocation) / 1000;
                                }

                            }
                        });

                    category.setRentalDistance(distance);
                    Log.d("distance", String.valueOf(category.getRentalDistance()));



                    categoryList.add(category);
                }

                Collections.sort(categoryList, new Comparator<Category>() {
                    @Override
                    public int compare(Category c1, Category c2) {
                        return Double.compare(c1.getRentalDistance(), c2.getRentalDistance());
                    }
                });



                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        categoryListView.setAdapter(new ByRentalAdapter(getActivity(), mCallbacks, categoryList));
                    }
                });



            }catch (MalformedURLException e) {

                e.printStackTrace();
                Error = e.getMessage();
                Toast.makeText(getActivity(),"Check your internet connection please", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Error = e.getMessage();
                Toast.makeText(getActivity(),"Check your internet connection please", Toast.LENGTH_LONG).show();
            } catch (JSONException e) {

                Error = e.getMessage();
                e.printStackTrace();
                Toast.makeText(getActivity(),"Check your internet connection please", Toast.LENGTH_LONG).show();
            } catch (NullPointerException e) {
                // TODO: handle exception
                Error = e.getMessage();
                Toast.makeText(getActivity(),"Check your internet connection please", Toast.LENGTH_LONG).show();
            }
            return null;
        }
    }



    //! function for populate category list
    private void initCategoryList() {
            new getCategList().execute();
    }


    @Override
    public void onConnectionEstablished(int code) {
        if (code == CATEGORY_ACTION) {
            if (UtilMethods.isConnectedToInternet(getActivity())) {
                initCategoryList();

            } else {
                Log.d("NoCon", "NoConn");

                internetConnectionListener = (UtilMethods.InternetConnectionListener) getActivity();
                UtilMethods.showNoGpsDialog(getActivity(), getResources().getString(R.string.no_gps),
                        getResources().getString(R.string.no_gps_message),
                        getResources().getString(R.string.no_gps_positive_text),
                        getResources().getString(R.string.no_gps_negative_text));
            }
        }
    }

    @Override
    public void onUserCanceled(int code) {
        if (code == CATEGORY_ACTION) {
            getActivity().finish();
        }
    }



    //! callback interface listen by HomeActivity to detect user click on category
    public static interface CategorySelectionCallbacks {
        void onCategorySelected(String catID, String title);
    }

}
