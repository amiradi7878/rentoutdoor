package com.tanjungdev.rentoutdoor.activity;


import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

import com.google.android.gms.location.LocationRequest;
import com.tanjungdev.rentoutdoor.adapter.ViewPagerAdapter;
import com.tanjungdev.rentoutdoor.fragment.ByBrandFragment;
import com.tanjungdev.rentoutdoor.fragment.ByRentalFragment;
import com.tanjungdev.rentoutdoor.fragment.DetailViewFragment;
import com.tanjungdev.rentoutdoor.fragment.NavigationDrawerFragment;
import com.tanjungdev.rentoutdoor.fragment.ResultListFragment;
import com.tanjungdev.rentoutdoor.model.Item;
import com.tanjungdev.rentoutdoor.util.UtilMethods;
import com.tanjungdev.rentoutdoor.R ;
import com.tanjungdev.rentoutdoor.fragment.ByTypeFragment;
import com.yalantis.phoenix.PullToRefreshView;

import android.support.v7.widget.Toolbar;

import java.util.List;


public class HomeActivity extends AppCompatActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ByRentalFragment.CategorySelectionCallbacks,
        ByBrandFragment.CategorySelectionCallbacks,
        ByTypeFragment.CategorySelectionCallbacks,
        ResultListFragment.ResultListCallbacks,
        SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {

    private Toolbar toolbar;
    TabLayout tabLayout;
    private ViewPager viewPager;

    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    ViewPagerAdapter viewPagerAdapter;
    private int navigationDepth = 0;
    private SearchView mSearchView;
    private List<String> suggestions;
    private String mCurrentSearchText;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private FragmentManager fragmentManager;
    private CharSequence mTitle;
    private String subCategoryTitle = null;
    private String resultListTitle = null;
    private String detailViewTitle = null;
    private String searchQueryTitle = null;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private Location currentBestLocation = null;
    private LocationManager mLocationManager;
    private Location locationGPS;
    private int count = 0;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private UtilMethods.InternetConnectionListener internetConnectionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final PullToRefreshView mPullToRefreshView;

            //mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);

            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            tabLayout = (TabLayout) findViewById(R.id.tabLayout);
            viewPager = (ViewPager) findViewById(R.id.viewPager);
            viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
            viewPagerAdapter.addFragments(new ByRentalFragment(), "Rental");
            viewPagerAdapter.addFragments(new ByTypeFragment(), "Kategori");
            viewPagerAdapter.addFragments(new ByBrandFragment(), "Brand");
            viewPager.setAdapter(viewPagerAdapter);
            tabLayout.setupWithViewPager(viewPager);


            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
            mTitle = getTitle();
            mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }






    @Override
    public void onBackPressed() {

        UtilMethods.hideSoftKeyboard(this);//percobaan
        if(navigationDepth>=1){
            navigationDepth--;
            fragmentManager.popBackStack();
            Log.d("navdep", "BackButton " +navigationDepth );
        }else if (navigationDepth == 0) {

            if(mSearchView.getVisibility()==View.VISIBLE){
                mSearchView.setVisibility(View.GONE);
            }

            if (tabLayout.getVisibility() == View.GONE) {
                tabLayout.setVisibility(View.VISIBLE);
            }
            count=count+1;
            Log.d("count",String.valueOf(count));
        }

        if(navigationDepth==0){

            if(mSearchView.getVisibility()==View.VISIBLE){
                mSearchView.setVisibility(View.GONE);
            }

            if (tabLayout.getVisibility() == View.GONE) {
                tabLayout.setVisibility(View.VISIBLE);
            }
        }

        if(count==2){
            finish();
        }


    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            mSearchView.setQuery(query, false);
            mSearchView.clearFocus();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
      //  if (mSearchView.getVisibility() == View.VISIBLE) {
        //    hideSearchView();
       // }
    }


    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        if (navigationDepth == 0) {
            actionBar.setTitle("RAOD");
        }else if (navigationDepth == 1) {
            actionBar.setTitle(resultListTitle);
            Log.d("navdep", "restoreActionBar " + resultListTitle);
        } else if (navigationDepth == 2) {
            actionBar.setTitle(detailViewTitle);
            Log.d("navdep", "restoreActionBar " + resultListTitle);
        }else if (navigationDepth == 3) {
            actionBar.setTitle(detailViewTitle);
        }else if (navigationDepth == 4) {
            actionBar.setTitle(searchQueryTitle);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());



        mSearchView.setSearchableInfo(searchableInfo);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mSearchView.setQueryHint(Html.fromHtml("<small><small>" + "Cari disini..."+ "</small></small>"));

        /*
        int searchPlateId = mSearchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = mSearchView.findViewById(searchPlateId);
        searchPlate.setBackgroundColor(Color.TRANSPARENT);
        int submitAreaId = mSearchView.getContext().getResources().getIdentifier("android:id/submit_area", null, null);
        View submitArea = mSearchView.findViewById(submitAreaId);
        submitArea.setBackgroundColor(Color.TRANSPARENT);
        int searchImgIcon = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView searchImgView = (ImageView) mSearchView.findViewById(searchImgIcon);
        searchImgView.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        searchImgView.setVisibility(View.GONE);
        int closeButtonId = mSearchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButton = (ImageView) mSearchView.findViewById(closeButtonId);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.setQuery(null, false);
                UtilMethods.hideSoftKeyboard(HomeActivity.this);
            }
        });*/


        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean chckInternet(){
        if (UtilMethods.isConnectedToInternet(this)) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        count = 0;
        //harusnya ada showhidesearchview
        UtilMethods.hideSoftKeyboard(HomeActivity.this);
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                //if (!isSearchFilerShowing() && !mNavigationDrawerFragment.isDrawerOpen())

                UtilMethods.showSoftKeyboard(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showHideSearchView() {
        if (mSearchView.getVisibility() == View.VISIBLE) {
            mSearchView.setFocusable(false);
            mSearchView.setVisibility(View.GONE);
            UtilMethods.hideSoftKeyboard(this);
        } else {
            mSearchView.setVisibility(View.VISIBLE);
            mSearchView.setQuery("", false);
            mSearchView.setFocusable(true);
            mSearchView.requestFocus();
            UtilMethods.showSoftKeyboard(this);
        }
    }



    private void hideSearchView() {
        if (mSearchView.getVisibility() == View.VISIBLE) {
            mSearchView.setVisibility(View.GONE);
        }
       // hideSuggestionList();
    }
/*
    private void hideSuggestionList() {
        if (suggestionListView != null && suggestionListView.getVisibility() == View.VISIBLE) {
            suggestionListView.setAdapter(null);
            suggestionListView.setVisibility(View.GONE);
        }
    }
*/
    @Override
    public void onCategorySelected(String catID, String title ) {
        hideSearchView();
        count = 0;
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, ResultListFragment.newInstance(catID, title),"rl")
                .addToBackStack("catSelect")
                .commit();
        navigationDepth++;
        resultListTitle = title;

        getSupportActionBar().setTitle(title);
        Log.d("navdep", "NavSelect : " + navigationDepth);

        if(tabLayout.getVisibility()==View.VISIBLE) {
            tabLayout.setVisibility(View.GONE);
        }
        UtilMethods.hideSoftKeyboard(this);
    }

    @Override
    public void onResultItemSelected(Item itemDetails) {
        Log.d("ItemDetails", String.valueOf(itemDetails));
        Log.d("ItemDetails", String.valueOf(itemDetails.getCarId()));

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, DetailViewFragment.newInstance(itemDetails),"rl2")
                .addToBackStack("resultItemSelect")
                .commit();
        hideSearchView();
        navigationDepth++;
        getSupportActionBar().setTitle(itemDetails.getTitle());

        detailViewTitle = itemDetails.getTitle();

        Log.d("navdep", "NavSelect : " + navigationDepth);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(itemDetails.getTitle());
        UtilMethods.hideSoftKeyboard(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        hideSearchView();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, ResultListFragment.newInstance("", suggestions.get(position)))
                .commit();
        navigationDepth = 4;
        getSupportActionBar().setTitle(suggestions.get(position));
        searchQueryTitle = suggestions.get(position);
        UtilMethods.hideSoftKeyboard(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mSearchView.clearFocus();
        hideSearchView();
        UtilMethods.hideSoftKeyboard(this);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, ResultListFragment.newInstance(query))
                .addToBackStack("catSelect")
                .commit();
        navigationDepth++;

        if(tabLayout.getVisibility()==View.VISIBLE) {
            tabLayout.setVisibility(View.GONE);
        }
        getSupportActionBar().setTitle(query);
        resultListTitle = query;
        searchQueryTitle="true";
        Log.d("navdepQuery",String.valueOf(query));
        Log.d("navdep", "NavSelect : " + navigationDepth);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.length() == 0) {
            //hideSuggestionList();
            UtilMethods.hideSoftKeyboard(this);
            mSearchView.clearFocus();
            mCurrentSearchText = null;
            return false;
        }
        if (!TextUtils.isEmpty(newText)) {
            //getSearchSuggestions(newText);
            mCurrentSearchText = newText;
            return true;
        }
        return false;
    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

}