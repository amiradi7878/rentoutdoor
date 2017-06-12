package com.tanjungdev.rentoutdoor.fragment;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tanjungdev.rentoutdoor.adapter.ByTypeAdapter;
import com.tanjungdev.rentoutdoor.model.Category;
import com.tanjungdev.rentoutdoor.util.UtilMethods;
import com.tanjungdev.rentoutdoor.util.Constants;
import com.tanjungdev.rentoutdoor.R;

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

/**
 * A simple {@link Fragment} subclass.
 */
public class ByTypeFragment extends Fragment implements UtilMethods.InternetConnectionListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final int CATEGORY_ACTION = 1;
    private CategorySelectionCallbacks mCallbacks;
    private ArrayList<Category> categoryList;
    private ListView categoryListView;
    private String Error = null;
    private UtilMethods.InternetConnectionListener internetConnectionListener;
    private TextView load;

    public ByTypeFragment() {

    }

    public static ByTypeFragment newInstance(int sectionNumber) {
        ByTypeFragment fragment = new ByTypeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (CategorySelectionCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement CategorySelectionCallbacks.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_by_type, container, false);
        categoryListView = (ListView) rootView.findViewById(R.id.categoryListView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (UtilMethods.isConnectedToInternet(getActivity())) {
            initCategoryList();
        } else {
            internetConnectionListener = (UtilMethods.InternetConnectionListener) ByTypeFragment.this;
            UtilMethods.showNoInternetDialog(getActivity(), internetConnectionListener,
                    getResources().getString(R.string.no_internet),
                    getResources().getString(R.string.no_internet_text),
                    getResources().getString(R.string.retry_string),
                    getResources().getString(R.string.exit_string), CATEGORY_ACTION);
        }

    }

    public class getCategList extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            URL hp = null;
            try {
                hp = new URL(
                        "https://amolpedi.000webhostapp.com/" + "bytype.php");

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
                    JSONObject Obj = j.getJSONObject(i);
                    category.setId(Obj.getString(Constants.JF_ID));
                    category.setTitle(Obj.getString(Constants.JF_TITLE));

                    if (!TextUtils.isEmpty(Obj.getString(Constants.JF_BACKGROUND_IMAGE))) {
                        category.setImageUrl(Obj.getString(Constants.JF_BACKGROUND_IMAGE));
                    }
                    Log.d("URL1",""+Obj.getString(Constants.JF_TITLE));
                    categoryList.add(category);
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        categoryListView.setAdapter(new ByTypeAdapter(getActivity(), mCallbacks, categoryList));
                    }
                });



            }catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Error = e.getMessage();
                Toast.makeText(getActivity(),"Check your internet connection please", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Error = e.getMessage();
                Toast.makeText(getActivity(),"Check your internet connection please", Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
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

    private void initCategoryList() {
        new getCategList().execute();
    }

    /*@Override
    public void onConnectionEstablished(int code) {
        if (code == CATEGORY_ACTION) {
            initCategoryList();
        }
    }*/

    @Override
    public void onConnectionEstablished(int code) {
        if (code == CATEGORY_ACTION) {
                initCategoryList();
        }
    }

    @Override
    public void onUserCanceled(int code) {
        if (code == CATEGORY_ACTION) {
            getActivity().finish();
        }
    }

    public static interface CategorySelectionCallbacks {
        void onCategorySelected(String catID, String title);
    }

}
