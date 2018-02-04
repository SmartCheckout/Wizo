package com.wizo.smartcheckout.activity;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.SyncParams;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.razorpay.PaymentResultListener;
import com.wizo.smartcheckout.R;
import com.wizo.smartcheckout.util.SharedPreferrencesUtil;
import com.wizo.smartcheckout.util.StateData;
import com.wizo.smartcheckout.util.TransactionStatus;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.wizo.smartcheckout.constant.constants.CART_ACTIVITY;
import static com.wizo.smartcheckout.constant.constants.LOCATION_ACCURACY_LIMIT;
import static com.wizo.smartcheckout.constant.constants.PAYMENTSUCCESS_ACTIVITY;
import static com.wizo.smartcheckout.constant.constants.RC_CHECK_SETTING;
import static com.wizo.smartcheckout.constant.constants.RC_LOCATION_PERMISSION;
import static com.wizo.smartcheckout.constant.constants.RC_SCAN_BARCODE_STORE;
import static com.wizo.smartcheckout.constant.constants.STORESELECTION_ACTIVITY;
import static com.wizo.smartcheckout.constant.constants.TRANSACTION_UPDATE_EP;


public class MainActivity extends AppCompatActivity
        implements PaymentResultListener, NavigationView.OnNavigationItemSelectedListener
         {
    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_PHOTOS = "photos";
    private static final String TAG_MOVIES = "movies";
    private static final String TAG_NOTIFICATIONS = "notifications";
    private static final String TAG_SETTINGS = "settings";
    public static String CURRENT_TAG = TAG_HOME;
    private Handler mHandler;

    private static final String TAG = "MainActivity";


    private boolean locationEnabled = false;
    private int locationRetryCount = 0;
    private int locationRetryLimit = 5;

    private AsyncHttpClient ahttpClient = new AsyncHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mHandler = new Handler();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header=navigationView.getHeaderView(0);
        TextView name = (TextView)header.findViewById(R.id.userName);
        TextView email = (TextView)header.findViewById(R.id.userEmail);
        name.setText(StateData.userName);
        email.setText(StateData.userEmail);
//
//        ImageView imageView = (ImageView) header.findViewById(R.id.userImage);
//
//        if(StateData.userImage != null)
//            Glide.with(getApplicationContext())
//                .load(StateData.userImage)
//                .into(imageView);

        if(StateData.store == null)
            launchFragment(STORESELECTION_ACTIVITY);

        final Intent receivingIntent = getIntent();
        final int nextActivity = receivingIntent.getIntExtra("next_activity",0);
        if(nextActivity != 0)
        {
            launchFragment(nextActivity);
        }

    }

    public void launchFragment(final int fragmentId) {
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Log.d("Intent received", fragmentId + "");
                Fragment fragment = getHomeFragment(fragmentId);
                Log.d("Loaded Fragment", "" + fragment);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.content_layout, fragment, String.valueOf(fragmentId));
                fragmentTransaction.commitAllowingStateLoss();
            }
        };
        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }
    }

             @Override
             public void onActivityResult(int requestCode, int resultCode, Intent data) {
                 super.onActivityResult(requestCode, resultCode, data);
                 switch (requestCode) {
                     case RC_CHECK_SETTING:
                         // Response from location enabled
                         Fragment frg = getSupportFragmentManager().findFragmentByTag(String.valueOf(STORESELECTION_ACTIVITY));
                         if (frg != null) {
                             frg.onActivityResult(requestCode, resultCode, data);
                         }

                 }
             }

             /**
     * Utility Method to enable location settings
     *
     * */
//    public void enableLocationSettings(){
//
//        LocationRequest request = new LocationRequest()
//                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//                //Set Interval and Set Fastest Interval
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(request);
//
//        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
//        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//            //Location Setting Result Handler
//            @Override
//            public void onResult(LocationSettingsResult result) {
//                final Status status = result.getStatus();
//                switch (status.getStatusCode()) {
//                    case LocationSettingsStatusCodes.SUCCESS:
//                        locationEnabled = true;
//                        //startLocationUpdates();
//                        break;
//                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                        try {
//                            status.startResolutionForResult(MainActivity.this,RC_CHECK_SETTING);
//                        } catch (IntentSender.SendIntentException e) {
//                            // Ignore the error.
//                            Log.d(TAG,e.getLocalizedMessage());
//                        }
//                        break;
//                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                        System.out.println("Resolution not possible");
//                        stopLocationUpdates();
//                        launchFragment(STORESELECTION_ACTIVITY);
//                        break;
//                }
//            }
//        });
//
//    }


    @Override
    public void onStart() {
        super.onStart();
        locationRetryCount = 0;
    }

    @Override
    public void onStop() {
        super.onStop();
        //stopLocationUpdates();
    }


    private Fragment getHomeFragment(int navItemIndex) {
        switch (navItemIndex) {


            case CART_ACTIVITY:
                // photos
                CartFragment cartFragment = new CartFragment();
                return cartFragment;

            case STORESELECTION_ACTIVITY:
                // movies fragment
                StoreSelectionFragment storeSelectionFragment = new StoreSelectionFragment();
                return storeSelectionFragment;

            case PAYMENTSUCCESS_ACTIVITY:
                // notifications fragment
                PaymentSuccessFragment paymentSuccessFragment = new PaymentSuccessFragment();
                return paymentSuccessFragment;

//            case 4:
//                // settings fragment
//                SettingsFragment settingsFragment = new SettingsFragment();
//                return settingsFragment;
//            default:
//                return new HomeFragment();
        }

        return null;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        // Display transaction id QR code
        JSONObject updateTransReq = new JSONObject();
        try{
            // Updating transaction status and payment reference
            JSONObject payment = new JSONObject();
            payment.put("paymentGateway","RAZOR_PAY");
            payment.put("paymentRef",razorpayPaymentID);
            payment.put("paymentStatus","SUCCESS");

            updateTransReq.put("trnsId", StateData.transactionId);
            updateTransReq.put("status", TransactionStatus.PAYMENT_SUCCESSFUL);
            updateTransReq.put("payment", new JSONArray().put(payment));

            StringEntity requestEntity = new StringEntity(updateTransReq.toString(), ContentType.APPLICATION_JSON);

            ahttpClient.post(this, TRANSACTION_UPDATE_EP, requestEntity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        //Setting transaction id into state data
                        Log.d(TAG, "Update Transaction Successful");
                        StateData.transactionId = response.getString("trnsId");
                        Log.d(TAG, "Updated transaction id : " + StateData.transactionId);
                        launchFragment(PAYMENTSUCCESS_ACTIVITY);

                    } catch (Exception e) {
                        // TODO: throw custom exception
                    }
                }
            });

            Log.d(TAG,"Update transaction status triggered. " + updateTransReq.toString());

        }catch(Exception e){
            //Todo
        }
    }

    @Override
    public void onPaymentError(int i, String s) {

    }
}
