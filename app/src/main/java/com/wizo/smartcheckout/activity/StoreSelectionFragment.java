package com.wizo.smartcheckout.activity;

import android.app.FragmentContainer;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.wizo.smartcheckout.R;
import com.wizo.smartcheckout.barcodereader.BarcodeCaptureActivity;
import com.wizo.smartcheckout.model.Store;
import com.wizo.smartcheckout.util.CommonUtils;
import com.wizo.smartcheckout.util.SharedPreferrencesUtil;
import com.wizo.smartcheckout.util.StateData;
import com.wizo.smartcheckout.util.TransactionStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cz.msebera.android.httpclient.Header;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.wizo.smartcheckout.constant.constants.BARCODE_SEARCH_EP;
import static com.wizo.smartcheckout.constant.constants.CART_ACTIVITY;
import static com.wizo.smartcheckout.constant.constants.LOCATION_ACCURACY_LIMIT;
import static com.wizo.smartcheckout.constant.constants.LOCATION_SEARCH_EP;
import static com.wizo.smartcheckout.constant.constants.RC_CHECK_SETTING;
import static com.wizo.smartcheckout.constant.constants.RC_LOCATION_PERMISSION;
import static com.wizo.smartcheckout.constant.constants.RC_SCAN_BARCODE_STORE;
import static com.wizo.smartcheckout.constant.constants.SP_TRANSACTION_ID;
import static com.wizo.smartcheckout.constant.constants.STORESELECTION_ACTIVITY;
import static com.wizo.smartcheckout.constant.constants.TIMEOUT_TRANSACTION_MINS;


/**
 * Created by yeshwanth on 4/5/2017.
 */

public class StoreSelectionFragment extends Fragment implements FragmentCompat.OnRequestPermissionsResultCallback
{
    private View view;
    private Store selectedStore;
    private static AsyncHttpClient ahttpClient = new AsyncHttpClient();

    /* Location related variabled */
    private FusedLocationProviderClient mFusedLocProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private ResolvableApiException mResolvableApiException;
    private int locationRetryCount =0;
    private int locationRetryLimit = 5;

    private final int IN_PROGRESS = 0;
    private final int SELECTION_ALL_OPTIONS = 1;
    private final int SELECTION_NO_LOC = 2;
    private final int NONE = 3;
    // view elements
    private CardView storeSelectionCard;
    private Button scanQR, enableLocation;
    private TextView storeSelectionMsg, progressMsg;
    private ProgressBar progressBar;

    private static final String TAG = "StoreSelectionFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.store_selection,container,false);

        storeSelectionMsg = (TextView) view.findViewById(R.id.supporting_text);
        progressMsg = (TextView)view.findViewById(R.id.storeLocatingProgressMsg);
        storeSelectionCard = (CardView) view.findViewById(R.id.storeSelectioCard);
        progressBar = (ProgressBar) view.findViewById(R.id.storeLocatingProgress);

        //  Action listeners for buttons
        scanQR = (Button) view.findViewById(R.id.scanStoreQR);
        scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchScanBarcode(RC_SCAN_BARCODE_STORE);
            }
        });

        enableLocation = (Button) view.findViewById(R.id.enableLocation);
        enableLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mResolvableApiException != null){
                    try {
                        mResolvableApiException.startResolutionForResult(getActivity(), RC_CHECK_SETTING);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //  Check location settings
        //  Triggers the entire store selection logic
        checkLocationSettings();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeAllViews();
            }
        }
    }

    // Need to add code for on Pause and on Resume
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public void manageVisibiliy(int code){
        if(code == IN_PROGRESS){
            progressBar.setVisibility(View.VISIBLE);
            progressMsg.setVisibility(View.VISIBLE);
            storeSelectionCard.setVisibility(View.GONE);
        }else if(code == SELECTION_ALL_OPTIONS){
            progressBar.setVisibility(View.GONE);
            progressMsg.setVisibility(View.GONE);
            storeSelectionCard.setVisibility(View.VISIBLE);
            enableLocation.setVisibility(View.VISIBLE);
            scanQR.setVisibility(View.VISIBLE);
        }else if(code == SELECTION_NO_LOC){
            progressBar.setVisibility(View.GONE);
            progressMsg.setVisibility(View.GONE);
            storeSelectionCard.setVisibility(View.VISIBLE);
            enableLocation.setVisibility(View.GONE);
            scanQR.setVisibility(View.VISIBLE);
        }else if(code == NONE){
            view.setVisibility(View.GONE);
        }

    }

    public void launchScanBarcode(int scanType){
        Intent barcodeScanIntent = new Intent(getActivity(),BarcodeCaptureActivity.class);
        barcodeScanIntent.putExtra("requestCode",scanType);
        startActivityForResult(barcodeScanIntent, scanType);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        switch (requestCode) {
            case RC_SCAN_BARCODE_STORE:

                if (resultCode == RESULT_OK ) {
                    String barcode = bundle.getString("Barcode");
                    if(barcode != null) {
                        Log.d(TAG, "=====> Control returned from Scan Barcode Activity. Barcode : " + barcode);
                        findStoreByBarcode(barcode);
                    }
                }
                else if(resultCode == RESULT_CANCELED )
                {
                    String reason = bundle.getString("Reason");
                    if(reason != null && reason.equalsIgnoreCase("Timeout"))
                        Toast.makeText(getActivity(),getResources().getString(R.string.toast_scan_timedout),Toast.LENGTH_LONG).show();
                }
                break;

            case RC_CHECK_SETTING: // Response from location enabled
                switch (resultCode) {
                    case RESULT_OK:
                        //locationEnabled = true;
                        System.out.println("");
                        startLocationUpdates();
                        break;
                    case RESULT_CANCELED:
                        //stopLocationUpdates();
                        //launchFragment(STORESELECTION_ACTIVITY);
                        break;
                }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RC_LOCATION_PERMISSION:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startLocationUpdates();
                }
                else  {
                    stopLocationUpdates();
                    //launchFragment(STORESELECTION_ACTIVITY);
                }
        }
    }

    public void stopLocationUpdates(){
        if(mFusedLocProviderClient!=null)
            mFusedLocProviderClient.removeLocationUpdates(mLocationCallback);
    }

    // ----------- BEGIN : BACKEND CALL METHODS ------------- //
    public  void findStoreByLocation(final Location location){

        RequestParams params = new RequestParams();
        params.put("lattitude", location.getLatitude());
        params.put("longitude", location.getLongitude());
        params.put("context", "STORE_IN_CURRENT_LOC");
        Log.d(TAG,"Invoking findStoreByLocation with location : "+ location.getLatitude() + " : " + location.getLongitude());
        ahttpClient.setMaxRetriesAndTimeout(2,1000);
        ahttpClient.get(LOCATION_SEARCH_EP, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                System.out.println("In onSuccess SelectStore");
                try {
                    // Unique store found
                    if (response.length() == 1) {
                        JSONObject store = response.getJSONObject(0);

                        Store selectedStore = new Gson().fromJson(store.toString(), Store.class);
                        StateData.store = selectedStore;
                        StateData.storeId = selectedStore.getId();
                        StateData.storeName = selectedStore.getTitle();
                        stopLocationUpdates();
                        launchCartActivity();
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errResponse) {
                Log.d(TAG,"Unable to find store details. " + statusCode+" "+errResponse,throwable);
            }
        });

    }

    public void findStoreByBarcode(String barcode){
        //Get Product Details

        RequestParams params = new RequestParams();
        params.put("barcode", barcode);

        ahttpClient.get(BARCODE_SEARCH_EP, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Store selectedStore = new Store();
                try{
                    selectedStore.setDisplayAddress(response.getString("displayAddress"));
                    selectedStore.setId(response.getString("id"));
                    selectedStore.setTitle(response.getString("title"));
                    StateData.store = selectedStore;
                    StateData.storeId = selectedStore.getId();
                    StateData.storeName = selectedStore.getTitle();
                    launchCartActivity();
                }catch(JSONException je ){
                    je.printStackTrace();

                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errResponse) {
                Log.d(TAG,"Unable to find store details. " + statusCode+" "+errResponse,throwable);
            }
        });

    }

    // ----------- END : BACKEND CALL METHODS ------------- //

    // ----------- BEGIN : LOCATION HELPER METHODS ---------- //

    protected LocationRequest createLocationRequest(long interval, long fastestInterval, int priority) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastestInterval);
        mLocationRequest.setPriority(priority);
        return mLocationRequest;
    }

    public void checkLocationSettings() {
        mLocationRequest = createLocationRequest(5000, 2000, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(getContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //  Location setting is enabled in the device
                //  Starting location updates flow
                manageVisibiliy(IN_PROGRESS);
                startLocationUpdates();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (e instanceof ResolvableApiException) {
                    //  Location setting is disabled but device capable of handling location
                    //  Redirecting to store selection activity with location and qr code option.
                    //  TODO pass available store selection options. In this case both location and QRScan
                    //launchFragment(STORESELECTION_ACTIVITY);
                    mResolvableApiException = (ResolvableApiException) e;
                    manageVisibiliy(SELECTION_ALL_OPTIONS);

                } else {

                    //  Location setting is disabled and device is not capable of handling location
                    //  Redirecting to store selection activity with only QR code option
                    // TODO pass available store selection options. In this case only QRScan
                   // launchFragment(STORESELECTION_ACTIVITY);
                    System.out.println("un Resolvable settings failure in store selection fragment");
                    manageVisibiliy(SELECTION_NO_LOC);
                }
            }
        });
    }

    public void startLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //   Location permission not available for the app
            //   Requesting location permission from user
            String[] requiredPermission = {ACCESS_FINE_LOCATION};
            requestPermissions(requiredPermission,RC_LOCATION_PERMISSION);
        }else {
            //   Location permission available for the app
            manageVisibiliy(IN_PROGRESS);
            mFusedLocProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
            mLocationCallback = new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    handleLocationUpdate(locationResult.getLastLocation());
                }
            };

            mFusedLocProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    protected void handleLocationUpdate(Location location){
        Log.d(TAG,"Location Update received. Accuracy : "+ location.getAccuracy());
        locationRetryCount++;
        if(locationRetryCount <= locationRetryLimit){
            if(location.getAccuracy() <100){
                Log.d(TAG,"Accuracy lt 100. Invoking store selection by location");
                findStoreByLocation(location);
            }else{
                Log.d(TAG,"Accuracy gt 100. Defering store search by location.");
            }
        }else{
            stopLocationUpdates();
        }
    }
    // ----------- END : LOCATION HELPER METHODS ---------- //

    // ----------- BEGIN : BUSINESS LOGIC METHOD ---------- //

    private String checkAndRetrieveOngoingTransaction(){
        String ongoingTransaction = null;
        if (SharedPreferrencesUtil.getStringPreference(getActivity(),"TransactionId") != null ) {
            Date lastTransactionDate = SharedPreferrencesUtil.getDatePreference(getActivity(), "TransactionUpdatedDate", null);

            // if the last transaction was left pending under "N" minutes
            long minute_diff = CommonUtils.getDifferenceinMinutes(lastTransactionDate, CommonUtils.getCurrentDate());
            Log.d("tag", "last pending transaction in " + minute_diff);
            String status = SharedPreferrencesUtil.getStringPreference(getActivity(), "TransactionStatus");

            Log.d("tag", "last pending transaction status is " + status);

            if (minute_diff < TIMEOUT_TRANSACTION_MINS && status != null
                    && (status.equalsIgnoreCase(TransactionStatus.SUSPENDED.name()))) {
                ongoingTransaction = SharedPreferrencesUtil.getStringPreference(getActivity(),"TransactionId");
            }
        }

        return ongoingTransaction;
    }


    private void launchCartActivity(){
       final String transactionId = checkAndRetrieveOngoingTransaction();
        if(transactionId!= null ){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity(),R.style.DialogTheme);

                // set dialog message
                alertDialogBuilder
                        .setMessage(R.string.saved_transaction_dialog)
                        .setCancelable(false)
                        .setPositiveButton(R.string.continue_transaction,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                StateData.transactionId = transactionId;
                                ((MainActivity)getActivity()).launchFragment(CART_ACTIVITY,null);

                            }
                        })
                        .setNegativeButton(R.string.start_over,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                StateData.transactionId = null;
                                SharedPreferrencesUtil.setStringPreference(getContext(),SP_TRANSACTION_ID, null);
                                ((MainActivity)getActivity()).launchFragment(CART_ACTIVITY,null);

                            }
                        });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alertDialog.show();

            }
        else{
             StateData.transactionId = null;
             ((MainActivity)getActivity()).launchFragment(CART_ACTIVITY);
            }

            manageVisibiliy(NONE);
    }

    // ----------------- END : BUSINESS LOGIC METHODS ------------ //

}
