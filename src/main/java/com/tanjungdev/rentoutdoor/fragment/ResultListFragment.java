package com.tanjungdev.rentoutdoor.fragment;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


import com.tanjungdev.rentoutdoor.adapter.ResultListAdapterRental;
import com.google.android.gms.maps.model.LatLng;
import com.tanjungdev.rentoutdoor.adapter.GPSTrackStandAlone;
import com.tanjungdev.rentoutdoor.adapter.ResultListAdapterType;
import com.tanjungdev.rentoutdoor.util.Constants;
import com.tanjungdev.rentoutdoor.util.UtilMethods;
import com.tanjungdev.rentoutdoor.R;
import com.tanjungdev.rentoutdoor.activity.MapActivity;
import com.tanjungdev.rentoutdoor.adapter.ResultListAdapterBrand;
import com.tanjungdev.rentoutdoor.model.Item;
import com.tanjungdev.rentoutdoor.util.LocationChangeListener;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;


public class ResultListFragment extends Fragment implements UtilMethods.InternetConnectionListener,
        LocationChangeListener, SwipeRefreshLayout.OnRefreshListener {

    public static String catId;
    public static String titleId;
    public static String searchTerm="";
    public static LocationChangeListener locationChangeListener;
    private final int RESULT_ACTION = 1;
    private final int RESULT_LIMIT = 100;
    private ArrayList<Item> searchResultList;
    private ResultListCallbacks mCallbacks;
    private UtilMethods.InternetConnectionListener internetConnectionListener;
    private ArrayList<Item> resultList;
    private ListView resultListView;
    private LatLng itemLocation;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    GPSTrackStandAlone gTrack = new GPSTrackStandAlone(getActivity());
    MapActivity mAct = new MapActivity();
    private TextView progText;

    public ResultListFragment() {

    }

    public static ResultListFragment newInstance(String id, String title) {
        ResultListFragment fragment = new ResultListFragment();
        catId = id;
        titleId = title;
        searchTerm = "";
        locationChangeListener = fragment;
        return fragment;
    }

    public static ResultListFragment newInstance(String term) {
        ResultListFragment fragment = new ResultListFragment();
        catId = "";
        titleId = "";
        searchTerm = term;
        Log.d("term",searchTerm);
        locationChangeListener = fragment;
        return fragment;
    }

    public void getGPSLoc(){
            double latitude = mAct.getLatitudeSend();
            double longitude = mAct.getLongitudeSend();
            Log.d("LatitudeCurrGPSLoc",String.valueOf(latitude));
            Log.d("LongCur",String.valueOf(longitude));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (ResultListCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ResultListCallbacks.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_result_list, container, false);
        progText = (TextView)rootView.findViewById(R.id.progText);
        resultListView = (ListView) rootView.findViewById(R.id.resultListView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        setHasOptionsMenu(true);
        return rootView;
    }
    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 5000);
    }
    @Override
    public void onResume() {
        super.onResume();
        Constants.isHomeOpened = false;
        Constants.isResultListFragmentOpened = true;
        if (UtilMethods.isConnectedToInternet(getActivity())) {
            if (!TextUtils.isEmpty(catId))
                initResultList();
            else if (!TextUtils.isEmpty(searchTerm))
                getSearchResults(searchTerm);
        } else {

            internetConnectionListener = (UtilMethods.InternetConnectionListener) ResultListFragment.this;
            UtilMethods.showNoInternetDialog(getActivity(), internetConnectionListener, getResources().getString(R.string.no_internet),
                    getResources().getString(R.string.no_internet_text),
                    getResources().getString(R.string.retry_string),
                    getResources().getString(R.string.exit_string), RESULT_ACTION);
        }
    }
    private void initResultList() {

            Log.d("Pembuktian", catId);
            if (Integer.parseInt(catId) > 100 && Integer.parseInt(catId) < 200) {
                new getCarRent().execute();
            } else if (Integer.parseInt(catId) > 199 && Integer.parseInt(catId) < 300) {
                new getCarBrand().execute();
            } else if (Integer.parseInt(catId) > 299 && Integer.parseInt(catId) < 400) {
                new getCarType().execute();// Disini dan di HomeAct agak ketuker
            }
    }

    private void getSearchResults(String query) {

        new getSearchCarBrand().execute();
    }

    public class getSearchCarBrand extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp = null;
            try {

                hp = new URL("https://amolpedi.000webhostapp.com/"
                        + "getSearchByBrand.php?value=" + searchTerm);
                // hp = new URL(
                // "http://192.168.1.106/restourant/foodtype.php?value="
                // + id);
                //Log.d("URL", "" + hp);
                URLConnection hpCon = hp.openConnection();
                hpCon.connect();
                InputStream input = hpCon.getInputStream();
                Log.d("input", "" + input);

                BufferedReader r = new BufferedReader(new InputStreamReader(
                        input));

                String x = "";
                x = r.readLine();
                String total = "";

                while (x != null) {
                    total += x;
                    x = r.readLine();
                }
                //   Log.d("URL", "" + total);

                JSONArray j = new JSONArray(total);
                //   Log.d("URL1", "" + j.length());
                Item[] itemList = new Item[j.length()];
                resultList = new ArrayList<Item>();
                for (int i = 0; i < j.length(); i++) {
                    Item item = new Item();// buat variabel category
                    //JSONObject Obj;
                    JSONObject Obj = j.getJSONObject(i); //sama sperti yang lama, cman ini lebih mempersingkat karena getJSONObject cm d tulis sekali aja disini

                    item.setId(Obj.getString(Constants.JF_ID));
                    item.setTitle(Obj.getString(Constants.JF_TITLE));


                    item.setAddress(Obj.getString(Constants.JF_ADDRESS));

                    item.setTelephoneNumber(Obj.optString(Constants.JF_TELEPHONE, Constants.NO_DATA_FOUND));

                    item.setEmailAddress(Obj.optString(Constants.JF_EMAIL, Constants.NO_DATA_FOUND));
                    item.setWebUrl(Obj.optString(Constants.JF_WEB, Constants.NO_DATA_FOUND));
                    item.setFacebookUrl(Obj.optString(Constants.JF_FACEBOOK, Constants.NO_DATA_FOUND));

                    item.setLatitude(Obj.optDouble(Constants.JF_LATITUDE, Constants.NULL_LOCATION));
                    item.setLongitude(Obj.optDouble(Constants.JF_LONGITUDE, Constants.NULL_LOCATION));
                    try {
                        item.setRating(Float.parseFloat(Obj.optString(Constants.JF_RATING, Constants.NO_DATA_FOUND)));
                    } catch (NumberFormatException e) {
                        item.setRating(0.0f);
                    }

                    item.setTagLine(Obj.optString(Constants.JF_TAG_LINE, Constants.NO_DATA_FOUND));
                    item.setDescription(Obj.optString(Constants.JF_DESCRIPTION, Constants.NO_DATA_FOUND));
                    item.setVerification(Obj.optString(Constants.JF_VERIFICATION, Constants.NO_DATA_FOUND).equals("1") ? true : false);

                    item.setCarId(Obj.optString(Constants.JF_CARID, Constants.NO_DATA_FOUND));
                    item.setCarTitle(Obj.optString(Constants.JF_CARTITLE, Constants.NO_DATA_FOUND));
                    item.setCarRentalId(Obj.optString(Constants.JF_CARRENTALID, Constants.NO_DATA_FOUND));
                    item.setCarPrice(Obj.optString(Constants.JF_CARPRICE, Constants.NO_DATA_FOUND));
                    item.setCarYear(Obj.optString(Constants.JF_CARYEAR, Constants.NO_DATA_FOUND));

                    JSONArray imgArr = Obj.getJSONArray("thumbImage");
                    String[] imageThumb = new String[imgArr.length()];
                    // String[] imageLarge = new String[imgArr.length()];

                    for (int k = 0; k < imgArr.length(); k++) {
                        imageThumb[k] = imgArr.getString(k);
                        // imageLarge[k] = imgArr.getJSONObject(k).getString(JF_TITLE);
                    }


                    for(int l = 0; l <imgArr.length(); l++) {
                        item.setImageLargeUrls(imageThumb);
                    }


                    item.setImageThumbUrls(imageThumb);

                    for(int l = 0; l <imgArr.length(); l++) {
                       Log.d("itemSearch", String.valueOf(item.getTitle()));
                    }
                    // item.setImageLargeUrls(imageLarge);

                    //  JSONArray imgArr = Obj.getJSONArray("thumbImage");

                    /*String[] imgCount = new String[imgArr.length()];
                    for(int k = 0 ; k < imgCount.length; k++) {


                        imgCount[k] = imgArr.getString(k);
                        item.setImageThumbUrls(imgCount);
                    }*/


                    Location trgtLocation = new Location("trgtLocation");
                    trgtLocation.setLatitude(item.getLatitude());
                    trgtLocation.setLongitude(item.getLongitude());

                    Log.d("LatLong", "Latitude "+String.valueOf(trgtLocation.getLatitude())+"Longitude "+ String.valueOf(trgtLocation.getLongitude()));

                    Location crntLocation = gTrack.getLocation(getActivity());


                    Log.d("crntLocation", String.valueOf(gTrack.getLocation(getActivity())));

                    item.setDistance(crntLocation.distanceTo(trgtLocation) / 1000);
                    Log.d("distance", String.valueOf(item.getDistance()));


                    String trimAddress =item.getAddress();
                    String trimAdress2 =  trimAddress.substring(0, Math.min(trimAddress.length(), 40))+"...";
                    item.setAddress(trimAdress2);

                    resultList.add(item);
                    // itemList[i]=item;
                }

                Collections.sort(resultList, new Comparator<Item>() {
                    @Override
                    public int compare(Item lhs, Item rhs) {
                        return lhs.getDistance().compareTo(rhs.getDistance());//mungkin valuenya null
                    }
                });

                Log.d("resultList value", String.valueOf(resultList));

                /*Arrays.sort(itemList, new Comparator<Item>() {
                    @Override
                    public int compare(Item lhs, Item rhs) {
                        return lhs.getDistance().compareTo(rhs.getDistance());//mungkin valuenya null
                    }
                });*/


                //itemList.notify();



                for (int i = 0; i < j.length(); i++) {

                    Log.d("itemList", String.valueOf(itemList[i]));//itemList hasilnya null
                }



                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        resultListView.setAdapter(new ResultListAdapterBrand(getActivity(), mCallbacks, resultList));

                    }
                });

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NullPointerException e) {
                // TODO: handle exception
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progText.setVisibility(View.VISIBLE);
            progText.setText("Searching . . . "+ searchTerm);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progText.setVisibility(View.GONE);
        }
    }

    public class getCarRent extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            URL hp = null;
            try {

                //getGPSLoc();

                hp = new URL("https://amolpedi.000webhostapp.com/"
                        + "getCarRent.php?value=" + catId);
                // hp = new URL(
                // "http://192.168.1.106/restourant/foodtype.php?value="
                // + id);
                //Log.d("URL", "" + hp);
                URLConnection hpCon = hp.openConnection();
                hpCon.connect();
                InputStream input = hpCon.getInputStream();
                Log.d("getCar", "getCarRent");

                BufferedReader r = new BufferedReader(new InputStreamReader(
                        input));

                String x = "";
                x = r.readLine();
                String total = "";

                while (x != null) {
                    total += x;
                    x = r.readLine();
                }
             //   Log.d("URL", "" + total);

                JSONArray j = new JSONArray(total);
             //   Log.d("URL1", "" + j.length());
                Item[] itemList = new Item[j.length()];
                resultList = new ArrayList<Item>();
                for (int i = 0; i < j.length(); i++) {
                    Item item = new Item();// buat variabel category
                    //JSONObject Obj;
                    JSONObject Obj = j.getJSONObject(i); //sama sperti yang lama, cman ini lebih mempersingkat karena getJSONObject cm d tulis sekali aja disini

                    item.setId(Obj.getString(Constants.JF_ID));
                    item.setTitle(Obj.getString(Constants.JF_TITLE));


                    item.setAddress(Obj.getString(Constants.JF_ADDRESS));

                    item.setTelephoneNumber(Obj.optString(Constants.JF_TELEPHONE, Constants.NO_DATA_FOUND));

                    item.setEmailAddress(Obj.optString(Constants.JF_EMAIL, Constants.NO_DATA_FOUND));
                    item.setWebUrl(Obj.optString(Constants.JF_WEB, Constants.NO_DATA_FOUND));
                    item.setFacebookUrl(Obj.optString(Constants.JF_FACEBOOK, Constants.NO_DATA_FOUND));
                    item.setTwitterUrl(Obj.optString(Constants.JF_TWITTER, Constants.NO_DATA_FOUND));
                    item.setLatitude(Obj.optDouble(Constants.JF_LATITUDE, Constants.NULL_LOCATION));
                    item.setLongitude(Obj.optDouble(Constants.JF_LONGITUDE, Constants.NULL_LOCATION));
                    try {
                        item.setRating(Float.parseFloat(Obj.optString(Constants.JF_RATING, Constants.NO_DATA_FOUND)));
                    } catch (NumberFormatException e) {
                        item.setRating(0.0f);
                    }
                    item.setTagLine(Obj.optString(Constants.JF_TAG_LINE, Constants.NO_DATA_FOUND));
                    item.setDescription(Obj.optString(Constants.JF_DESCRIPTION, Constants.NO_DATA_FOUND));
                    item.setVerification(Obj.optString(Constants.JF_VERIFICATION, Constants.NO_DATA_FOUND).equals("1") ? true : false);

                    item.setCarId(Obj.optString(Constants.JF_CARID, Constants.NO_DATA_FOUND));
                    item.setCarTitle(Obj.optString(Constants.JF_CARTITLE, Constants.NO_DATA_FOUND));
                    item.setCarRentalId(Obj.optString(Constants.JF_CARRENTALID, Constants.NO_DATA_FOUND));
                    item.setCarPrice(Obj.optString(Constants.JF_CARPRICE, Constants.NO_DATA_FOUND));
                    item.setCarYear(Obj.optString(Constants.JF_CARYEAR, Constants.NO_DATA_FOUND));

                    JSONArray imgArr = Obj.getJSONArray("thumbImage");
                    String[] imageThumb = new String[imgArr.length()];

                    // String[] imageLarge = new String[imgArr.length()];
                    for (int k = 0; k < imgArr.length(); k++) {// ini untuk thumbnail di resultList
                        imageThumb[k] = imgArr.getString(k);
                        Log.d("setImageThumb", imageThumb[k]);
                        // imageLarge[k] = imgArr.getJSONObject(k).getString(JF_TITLE);
                    }


                    for(int l = 0; l <imgArr.length(); l++) {
                            item.setImageLargeUrls(imageThumb);
                            Log.d("setImageLargeUrls", String.valueOf(item.getImageLargeUrls()));
                    }

                    item.setImageThumbUrls(imageThumb);
                    // item.setImageLargeUrls(imageLarge);

                    //  JSONArray imgArr = Obj.getJSONArray("thumbImage");

                    /*String[] imgCount = new String[imgArr.length()];
                    for(int k = 0 ; k < imgCount.length; k++) {


                        imgCount[k] = imgArr.getString(k);
                        item.setImageThumbUrls(imgCount);
                    }*/

/*
                        Location trgtLocation = new Location("trgtLocation");
                        trgtLocation.setLatitude(item.getLatitude());
                        trgtLocation.setLongitude(item.getLongitude());

                        Log.d("LatLong", "Latitude "+String.valueOf(trgtLocation.getLatitude())+"Longitude "+ String.valueOf(trgtLocation.getLongitude()));

                        Location crntLocation = gTrack.getLocation(getActivity());


                        Log.d("crntLocation", String.valueOf(gTrack.getLocation(getActivity())));

                        item.setDistance(crntLocation.distanceTo(trgtLocation) / 1000);
                        Log.d("distance", String.valueOf(item.getDistance()));

*/




                            resultList.add(item);
                   // itemList[i]=item;
                }
/*
                Collections.sort(resultList, new Comparator<Item>() {
                    @Override
                    public int compare(Item lhs, Item rhs) {
                        return lhs.getDistance().compareTo(rhs.getDistance());//mungkin valuenya null
                    }
                });

                Log.d("resultList value", String.valueOf(resultList));

                /*Arrays.sort(itemList, new Comparator<Item>() {
                    @Override
                    public int compare(Item lhs, Item rhs) {
                        return lhs.getDistance().compareTo(rhs.getDistance());//mungkin valuenya null
                    }
                });


                //itemList.notify();

                for (int i = 0; i < j.length(); i++) {

                    Log.d("itemList", String.valueOf(itemList[i]));//itemList hasilnya null
                }
*/


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        resultListView.setAdapter(new ResultListAdapterRental(getActivity(), mCallbacks, resultList));

                    }
                });

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NullPointerException e) {
                // TODO: handle exception
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progText.setVisibility(View.VISIBLE);
            progText.setText("Loadig Data");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progText.setVisibility(View.GONE);
        }
        }

    public class getCarBrand extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp = null;
            try {
                Log.d("title Result List",titleId);
                hp = new URL("https://amolpedi.000webhostapp.com/"
                        + "getCarBrand.php?value=" + catId);
                // hp = new URL(
                // "http://192.168.1.106/restourant/foodtype.php?value="
                // + id);
             //   Log.d("URL", "" + hp);
                URLConnection hpCon = hp.openConnection();
                hpCon.connect();
                InputStream input = hpCon.getInputStream();
                Log.d("getCar", "getCarBrand");

                BufferedReader r = new BufferedReader(new InputStreamReader(
                        input));

                String x = "";
                x = r.readLine();
                String total = "";

                while (x != null) {
                    total += x;
                    x = r.readLine();
                }
               // Log.d("URL", "" + total);

                JSONArray j = new JSONArray(total);
              //  Log.d("URL1", "" + j.length());
                resultList = new ArrayList<Item>();
                for (int i = 0; i < j.length(); i++) {
                    Log.d("if",titleId);
                    Item item = new Item();// buat variabel category
                    //JSONObject Obj;
                    JSONObject Obj = j.getJSONObject(i); //sama sperti yang lama, cman ini lebih mempersingkat karena getJSONObject cm d tulis sekali aja disini

                    item.setId(Obj.getString(Constants.JF_ID));
                    item.setTitle(Obj.getString(Constants.JF_TITLE));


                    item.setAddress(Obj.getString(Constants.JF_ADDRESS));

                    item.setTelephoneNumber(Obj.optString(Constants.JF_TELEPHONE, Constants.NO_DATA_FOUND));

                    item.setEmailAddress(Obj.optString(Constants.JF_EMAIL, Constants.NO_DATA_FOUND));
                    item.setWebUrl(Obj.optString(Constants.JF_WEB, Constants.NO_DATA_FOUND));
                    item.setFacebookUrl(Obj.optString(Constants.JF_FACEBOOK, Constants.NO_DATA_FOUND));

                    item.setLatitude(Obj.optDouble(Constants.JF_LATITUDE, Constants.NULL_LOCATION));
                    item.setLongitude(Obj.optDouble(Constants.JF_LONGITUDE, Constants.NULL_LOCATION));
                    try {
                        item.setRating(Float.parseFloat(Obj.optString(Constants.JF_RATING, Constants.NO_DATA_FOUND)));
                    } catch (NumberFormatException e) {
                        item.setRating(0.0f);
                    }

                    try {
                        item.setRatingCount(Integer.parseInt(Obj.optString(Constants.JF_RATING_COUNT, Constants.NO_DATA_FOUND)));
                    } catch (NumberFormatException e) {
                        item.setRatingCount(0);
                    }

                    try {
                        item.setRatingScore(Integer.parseInt(Obj.optString(Constants.JF_RATINGSCORE, Constants.NO_DATA_FOUND)));
                    } catch (NumberFormatException e) {
                        item.setRatingCount(0);
                    }

                    item.setTagLine(Obj.optString(Constants.JF_TAG_LINE, Constants.NO_DATA_FOUND));
                    item.setDescription(Obj.optString(Constants.JF_DESCRIPTION, Constants.NO_DATA_FOUND));
                    item.setVerification(Obj.optString(Constants.JF_VERIFICATION, Constants.NO_DATA_FOUND).equals("1") ? true : false);
                    item.setTitle(Obj.getString(Constants.JF_TITLE));
                    item.setCarTitle(Obj.getString(Constants.JF_CARTITLE));
                    item.setCarId(Obj.optString(Constants.JF_CARID, Constants.NO_DATA_FOUND));
                    item.setCarBrandId(Obj.optString(Constants.JF_BRANDID, Constants.NO_DATA_FOUND));
                    item.setCarRentalId(Obj.optString(Constants.JF_CARRENTALID, Constants.NO_DATA_FOUND));
                    item.setCarPrice(Obj.optString(Constants.JF_CARPRICE, Constants.NO_DATA_FOUND));
                    item.setCarYear(Obj.optString(Constants.JF_CARYEAR, Constants.NO_DATA_FOUND));
                    item.setRentLogo(Obj.optString(Constants.JF_RENTLOGO, Constants.NO_DATA_FOUND));

                    JSONArray imgArr = Obj.getJSONArray("thumbImage");
                    String[] imageThumb = new String[imgArr.length()];
                    // String[] imageLarge = new String[imgArr.length()];

                    for (int k = 0; k < imgArr.length(); k++) {
                        imageThumb[k] = imgArr.getString(k);
                        // imageLarge[k] = imgArr.getJSONObject(k).getString(JF_TITLE);
                    }

                    for(int l = 0; l <imgArr.length(); l++) {
                        item.setImageLargeUrls(imageThumb);
                    }


                    item.setImageThumbUrls(imageThumb);


                    resultList.add(item);
                }
                /*Collections.sort(resultList, new Comparator<Item>() {
                    @Override
                    public int compare(Item lhs, Item rhs) {
                        return lhs.getDistance().compareTo(rhs.getDistance());//mungkin valuenya null
                    }
                });

                Log.d("resultList CarBrand ", String.valueOf(resultList));*/
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultListView.setAdapter(new ResultListAdapterBrand(getActivity(), mCallbacks, resultList));
                    }
                });

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NullPointerException e) {
                // TODO: handle exception
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progText.setVisibility(View.VISIBLE);
            progText.setText("Loadig Data");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progText.setVisibility(View.GONE);
        }

    }

    public class getCarType extends AsyncTask<Void, Void, Void>{//fokus disini
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp = null;
            try {
                hp = new URL("https://amolpedi.000webhostapp.com/"
                        + "getCarType.php?value=" + catId);
                URLConnection hpCon = hp.openConnection();
                hpCon.connect();
                InputStream input = hpCon.getInputStream();
                Log.d("getCar", "getCarType");
                BufferedReader r = new BufferedReader(new InputStreamReader(
                        input));

                String x = "";
                x = r.readLine();
                String total = "";

                while (x != null) {
                    total += x;
                    x = r.readLine();
                }


                JSONArray j = new JSONArray(total);
                Log.d("URL1", "" + j.length());
                Item[] itemList = new Item[j.length()];
                resultList = new ArrayList<Item>();
                for (int i = 0; i < j.length(); i++) {
                    Log.d("if",titleId);
                    Item item = new Item();// buat variabel category
                    //JSONObject Obj;
                    JSONObject Obj = j.getJSONObject(i); //sama sperti yang lama, cman ini lebih mempersingkat karena getJSONObject cm d tulis sekali aja disini

                    item.setId(Obj.getString(Constants.JF_ID));
                    item.setTitle(Obj.getString(Constants.JF_TITLE));


                    item.setAddress(Obj.getString(Constants.JF_ADDRESS));

                    item.setTelephoneNumber(Obj.optString(Constants.JF_TELEPHONE, Constants.NO_DATA_FOUND));

                    item.setEmailAddress(Obj.optString(Constants.JF_EMAIL, Constants.NO_DATA_FOUND));
                    item.setWebUrl(Obj.optString(Constants.JF_WEB, Constants.NO_DATA_FOUND));
                    item.setFacebookUrl(Obj.optString(Constants.JF_FACEBOOK, Constants.NO_DATA_FOUND));

                    item.setLatitude(Obj.optDouble(Constants.JF_LATITUDE, Constants.NULL_LOCATION));
                    item.setLongitude(Obj.optDouble(Constants.JF_LONGITUDE, Constants.NULL_LOCATION));
                    try {
                        item.setRating(Float.parseFloat(Obj.optString(Constants.JF_RATING, Constants.NO_DATA_FOUND)));
                    } catch (NumberFormatException e) {
                        item.setRating(0.0f);
                    }

                    try {
                        item.setRatingCount(Integer.parseInt(Obj.optString(Constants.JF_RATING_COUNT, Constants.NO_DATA_FOUND)));
                    } catch (NumberFormatException e) {
                        item.setRatingCount(0);
                    }

                    try {
                        item.setRatingScore(Integer.parseInt(Obj.optString(Constants.JF_RATINGSCORE, Constants.NO_DATA_FOUND)));
                    } catch (NumberFormatException e) {
                        item.setRatingCount(0);
                    }

                    item.setTagLine(Obj.optString(Constants.JF_TAG_LINE, Constants.NO_DATA_FOUND));
                    item.setDescription(Obj.optString(Constants.JF_DESCRIPTION, Constants.NO_DATA_FOUND));
                    item.setVerification(Obj.optString(Constants.JF_VERIFICATION, Constants.NO_DATA_FOUND).equals("1") ? true : false);

                    item.setCarId(Obj.optString(Constants.JF_CARID, Constants.NO_DATA_FOUND));

                    item.setTitle(Obj.optString(Constants.JF_TITLE, Constants.NO_DATA_FOUND));
                    item.setCarTitle(Obj.getString(Constants.JF_CARTITLE));//ditukar agar bisa menggunakan 1 adapter resultListAdapterBrand

                    item.setCarBrandId(Obj.optString(Constants.JF_BRANDID, Constants.NO_DATA_FOUND));
                    item.setCarRentalId(Obj.optString(Constants.JF_CARRENTALID, Constants.NO_DATA_FOUND));
                    item.setCarPrice(Obj.optString(Constants.JF_CARPRICE, Constants.NO_DATA_FOUND));
                    item.setCarYear(Obj.optString(Constants.JF_CARYEAR, Constants.NO_DATA_FOUND));
                    item.setRentLogo(Obj.optString(Constants.JF_RENTLOGO, Constants.NO_DATA_FOUND));

                    JSONArray imgArr = Obj.getJSONArray("thumbImage");
                    String[] imageThumb = new String[imgArr.length()];
                    // String[] imageLarge = new String[imgArr.length()];

                    for (int k = 0; k < imgArr.length(); k++) {
                        imageThumb[k] = imgArr.getString(k);
                        // imageLarge[k] = imgArr.getJSONObject(k).getString(JF_TITLE);
                    }

                    for(int l = 0; l <imgArr.length(); l++) {
                        item.setImageLargeUrls(imageThumb);
                    }

                    Log.d("test Address = ",item.getAddress());

                    item.setImageThumbUrls(imageThumb);


                    resultList.add(item);


                    itemList[i] = item;

                    Log.d("newResultListFragment", String.valueOf(itemList[i]));

                    Arrays.sort(itemList, new Comparator<Item>() {
                        @Override
                        public int compare(Item lhs, Item rhs) {
                            return 0;
                        }
                    });


                }


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        resultListView.setAdapter(new ResultListAdapterBrand(getActivity(), mCallbacks, resultList));
                    }
                });

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NullPointerException e) {
                // TODO: handle exception
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progText.setVisibility(View.VISIBLE);
            progText.setText("Loadig Data");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progText.setVisibility(View.GONE);
        }

    }


    @Override
    public void onConnectionEstablished(int code) {

        if (code == RESULT_ACTION) {
            if (!TextUtils.isEmpty(catId))
                initResultList();
            else if (!TextUtils.isEmpty(searchTerm))
            initResultList();
        }
    }

    @Override
    public void onUserCanceled(int code) {
        if (code == RESULT_ACTION) {
            getActivity().finish();
        }
    }

    @Override
    public void onLocationChange() {
        if (UtilMethods.isConnectedToInternet(getActivity())) {
            getSearchResults(searchTerm);
            Collections.shuffle(searchResultList);
            ((ResultListAdapterBrand) resultListView.getAdapter()).notifyDataSetChanged();
            ((ResultListAdapterRental) resultListView.getAdapter()).notifyDataSetChanged();
            ((ResultListAdapterType) resultListView.getAdapter()).notifyDataSetChanged();

        } else {
            internetConnectionListener = (UtilMethods.InternetConnectionListener) ResultListFragment.this;
            UtilMethods.showNoInternetDialog(getActivity(), internetConnectionListener, getResources().getString(R.string.no_internet),
                    getResources().getString(R.string.no_internet_text),
                    getResources().getString(R.string.retry_string),
                    getResources().getString(R.string.exit_string), RESULT_ACTION);
        }
    }

    public interface ResultListCallbacks {
        void onResultItemSelected(Item itemDetails);
    }

}
