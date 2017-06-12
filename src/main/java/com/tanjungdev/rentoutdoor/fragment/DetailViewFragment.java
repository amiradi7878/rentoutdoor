package com.tanjungdev.rentoutdoor.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tanjungdev.rentoutdoor.model.Item;
import com.tanjungdev.rentoutdoor.util.ExpandableTextView;
import com.tanjungdev.rentoutdoor.util.PhoneCallDialog;
import com.tanjungdev.rentoutdoor.util.UtilMethods;
import com.tanjungdev.rentoutdoor.util.Constants;
import com.tanjungdev.rentoutdoor.R;
import com.tanjungdev.rentoutdoor.activity.MapActivity;
import com.tanjungdev.rentoutdoor.adapter.ImagePagerAdapter;
import com.tanjungdev.rentoutdoor.util.CustomRatingBar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.tanjungdev.rentoutdoor.util.UtilMethods.mailTo;

public class DetailViewFragment extends Fragment implements UtilMethods.InternetConnectionListener {


    public static Item itemDetails;
    private static AlertDialog dialog = null;
    private final int BROWSER_ACTION = 1;
    private final int MAP_ACTION = 2;
    private final int RATE_NOW_ACTION = 3;
    SimpleDateFormat appViewFormat;
    SimpleDateFormat serverFormat;
    private ViewPager imagePager;
    private ImageView prevImgView;
    private ImageView nextImgView;
    private UtilMethods.InternetConnectionListener internetConnectionListener;
    private int googlePlayServiceStatus;
    private TextView countRatingTV;
    private TextView allRatingTV;
    private String phoneString = null;


    public DetailViewFragment() {

    }

    public static DetailViewFragment newInstance(Item item) {
        DetailViewFragment fragment = new DetailViewFragment();
        itemDetails = item;
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_view, container, false);
        if (itemDetails != null) {
            imagePager = ((ViewPager) rootView.findViewById(R.id.detailHeadingImageViewPager));
            prevImgView = (ImageView) rootView.findViewById(R.id.prevImgView);
            nextImgView = (ImageView) rootView.findViewById(R.id.nextImgView);

            //! viewpager to show images with horizontal scrolling.
            imagePager.setAdapter(new ImagePagerAdapter(getActivity(), itemDetails.getImageLargeUrls()));


            //! hide previous and next arrow if adapter size is less then 2
            if (imagePager.getAdapter().getCount() <= 1) {
                prevImgView.setVisibility(View.INVISIBLE);
                nextImgView.setVisibility(View.INVISIBLE);
            }


            ((ViewPager) rootView.findViewById(R.id.detailHeadingImageViewPager)).addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {

                    if (imagePager.getAdapter().getCount() > 1) {
                        if (position == 0) {
                            nextImgView.setVisibility(View.VISIBLE);
                            prevImgView.setVisibility(View.INVISIBLE);
                        } else if (position == imagePager.getAdapter().getCount() - 1) {
                            prevImgView.setVisibility(View.VISIBLE);
                            nextImgView.setVisibility(View.INVISIBLE);
                        } else {
                            prevImgView.setVisibility(View.VISIBLE);
                            nextImgView.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });


            if (!TextUtils.isEmpty(itemDetails.getCarTitle())) {
                ((TextView) rootView.findViewById(R.id.itemNameTV)).setText(itemDetails.getCarTitle());
            }

            if (!TextUtils.isEmpty(itemDetails.getTelephoneNumber()) &&
                    !itemDetails.getTelephoneNumber().equals(Constants.NO_DATA_FOUND)) {
                if (!TextUtils.isEmpty(phoneString)) {
                    phoneString += ",";
                    phoneString += itemDetails.getTelephoneNumber();
                } else {
                    phoneString = itemDetails.getTelephoneNumber();
                }
            }

            if (!TextUtils.isEmpty(phoneString))
                ((TextView) rootView.findViewById(R.id.itemPhoneTV)).setText(phoneString);

            if (!TextUtils.isEmpty(itemDetails.getAddress()))
                ((TextView) rootView.findViewById(R.id.itemLocationTV)).setText(itemDetails.getAddress());

            if (!TextUtils.isEmpty(itemDetails.getCarPrice()))
                ((ExpandableTextView) rootView.findViewById(R.id.itemTagTV)).setText(itemDetails.getCarPrice());

            ((CustomRatingBar) rootView.findViewById(R.id.ratingBar)).setScore(itemDetails.getRating());

            ((TextView) rootView.findViewById(R.id.btnWeb)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(itemDetails.getWebUrl())) {
                        if (UtilMethods.isConnectedToInternet(getActivity())) {
                            UtilMethods.browseUrl(getActivity(), itemDetails.getWebUrl());
                        } else {
                            internetConnectionListener = (UtilMethods.InternetConnectionListener) DetailViewFragment.this;
                            UtilMethods.showNoInternetDialog(getActivity(), internetConnectionListener, getResources().getString(R.string.no_internet),
                                    getResources().getString(R.string.no_internet_text),
                                    getResources().getString(R.string.retry_string),
                                    getResources().getString(R.string.cancel_string), BROWSER_ACTION);
                        }

                    } else
                        Toast.makeText(getActivity(), getResources().getString(R.string.no_website), Toast.LENGTH_SHORT).show();
                }
            });

            //Email
            if (!TextUtils.isEmpty(itemDetails.getEmailAddress()))
                ((TextView) rootView.findViewById(R.id.itemEmailTV)).setText(itemDetails.getEmailAddress());

            //FB
            if (!TextUtils.isEmpty(itemDetails.getFacebookUrl())) {
                ((TextView) rootView.findViewById(R.id.itemFbTV)).setText(itemDetails.getFacebookUrl());
            }
            ((TextView) rootView.findViewById(R.id.itemFbTV)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(itemDetails.getFacebookUrl())) {
                        if (UtilMethods.isConnectedToInternet(getActivity())) {
                            UtilMethods.browseUrl(getActivity(), itemDetails.getFacebookUrl());
                        } else {
                            internetConnectionListener = (UtilMethods.InternetConnectionListener) DetailViewFragment.this;
                            UtilMethods.showNoInternetDialog(getActivity(), internetConnectionListener, getResources().getString(R.string.no_internet),
                                    getResources().getString(R.string.no_internet_text),
                                    getResources().getString(R.string.retry_string),
                                    getResources().getString(R.string.cancel_string), BROWSER_ACTION);
                        }

                    } else
                        Toast.makeText(getActivity(), getResources().getString(R.string.no_website), Toast.LENGTH_SHORT).show();
                }
            });

            //Twitter
            if (!TextUtils.isEmpty(itemDetails.getTwitterUrl())) {
                ((TextView) rootView.findViewById(R.id.itemTwittTV)).setText(itemDetails.getTwitterUrl());
            }
            ((TextView) rootView.findViewById(R.id.itemTwittTV)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(itemDetails.getTwitterUrl())) {
                        if (UtilMethods.isConnectedToInternet(getActivity())) {
                            UtilMethods.browseUrl(getActivity(), itemDetails.getTwitterUrl());
                        } else {
                            internetConnectionListener = (UtilMethods.InternetConnectionListener) DetailViewFragment.this;
                            UtilMethods.showNoInternetDialog(getActivity(), internetConnectionListener, getResources().getString(R.string.no_internet),
                                    getResources().getString(R.string.no_internet_text),
                                    getResources().getString(R.string.retry_string),
                                    getResources().getString(R.string.cancel_string), BROWSER_ACTION);
                        }

                    } else
                        Toast.makeText(getActivity(), getResources().getString(R.string.no_website), Toast.LENGTH_SHORT).show();
                }
            });

            Log.d("itemEmail",String.valueOf(itemDetails.getEmailAddress()));
            Log.d("itemFB",String.valueOf(itemDetails.getFacebookUrl()));
            Log.d("itemTW",String.valueOf(itemDetails.getTwitterUrl()));
            Log.d("itemTagline",String.valueOf(itemDetails.getTagLine()));
            //Tagline
            if (!TextUtils.isEmpty(itemDetails.getTagLine()))
                ((TextView) rootView.findViewById(R.id.itemTagTV2)).setText(itemDetails.getTagLine());


            ((TextView) rootView.findViewById(R.id.btnMap)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMap();
                }
            });


            ((TextView) rootView.findViewById(R.id.itemPhoneTV)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(phoneString)) {
                        if (UtilMethods.isDeviceCallSupported(getActivity())) {
                            if (phoneString.contains(",")) {
                                PhoneCallDialog.showPhoneCallDialog(getActivity(),
                                        phoneString.split(","));
                            } else {
                                UtilMethods.phoneCall(getActivity(), phoneString);
                            }
                        }
                    }
                }
            });

            ((TextView) rootView.findViewById(R.id.itemLocationTV)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMap();
                }
            });

        }
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
           // menu.findItem(R.id.action_filter).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(false);
        }
    }

    private void showMap() {
        if (itemDetails.getLatitude() != Constants.NULL_LOCATION && itemDetails.getLongitude() != Constants.NULL_LOCATION) {
            /** set APP_MAP_MODE to true to enable internet checking
            * because map needs internet connection
            * to  show user and business location as well as their distance
            */
            UtilMethods.APP_MAP_MODE = true;
            if (UtilMethods.isConnectedToInternet(getActivity())) {
                if (isGooglePlayServicesAvailable()) {
                    if (UtilMethods.isGpsEnable(getActivity())) {
                        startActivity(new Intent(getActivity(), MapActivity.class));
                    } else {
                        UtilMethods.showNoGpsDialog(getActivity(), getResources().getString(R.string.no_gps),
                                getResources().getString(R.string.no_gps_message),
                                getResources().getString(R.string.no_gps_positive_text),
                                getResources().getString(R.string.no_gps_negative_text));
                    }
                } else {
                    showGooglePlayServiceUnavailableDialog();
                }
            } else {
                internetConnectionListener = (UtilMethods.InternetConnectionListener) DetailViewFragment.this;
                UtilMethods.showNoInternetDialog(getActivity(), internetConnectionListener, getResources().getString(R.string.no_internet),
                        getResources().getString(R.string.no_internet_text),
                        getResources().getString(R.string.retry_string),
                        getResources().getString(R.string.cancel_string), MAP_ACTION);
            }

        } else
            Toast.makeText(getActivity(), getResources().getString(R.string.location_not_found), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private String dateTimeFormatter(String serverDate) {
        appViewFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a");
        serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return appViewFormat.format(serverFormat.parse(serverDate));
        } catch (ParseException e) {
            return serverDate;
        }
    }

    @Override
    public void onConnectionEstablished(int code) {
        if (code == BROWSER_ACTION) {
            UtilMethods.browseUrl(getActivity(), itemDetails.getWebUrl());
        }
        if (code == MAP_ACTION) {
            UtilMethods.APP_MAP_MODE = false;
            startActivity(new Intent(getActivity(), MapActivity.class));
        } else if (code == RATE_NOW_ACTION) {

        }
    }

    @Override
    public void onUserCanceled(int code) {
        if (code == RATE_NOW_ACTION) {

        }
        if (code == MAP_ACTION) {
            UtilMethods.APP_MAP_MODE = false;
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        googlePlayServiceStatus = GooglePlayServicesUtil.
                isGooglePlayServicesAvailable(getActivity());
        if (ConnectionResult.SUCCESS == googlePlayServiceStatus) {
            return true;
        } else {
            return false;
        }
    }

    private void showGooglePlayServiceUnavailableDialog() {
        GooglePlayServicesUtil.getErrorDialog(googlePlayServiceStatus, getActivity(), 0).show();
    }
}
