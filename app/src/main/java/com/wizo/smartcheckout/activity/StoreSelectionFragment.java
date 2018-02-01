package com.wizo.smartcheckout.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.request.GenericRequest;
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

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.wizo.smartcheckout.constant.constants.BARCODE_SEARCH_EP;
import static com.wizo.smartcheckout.constant.constants.CART_ACTIVITY;
import static com.wizo.smartcheckout.constant.constants.LOCATION_SEARCH_EP;
import static com.wizo.smartcheckout.constant.constants.RC_SCAN_BARCODE_STORE;
import static com.wizo.smartcheckout.constant.constants.SP_TRANSACTION_ID;
import static com.wizo.smartcheckout.constant.constants.TIMEOUT_TRANSACTION_MINS;


/**
 * Created by yeshwanth on 4/5/2017.
 */

public class StoreSelectionFragment extends Fragment
{
    private ProgressBar progressBar;
    private View view;
    private Store selectedStore;
    private static AsyncHttpClient ahttpClient = new AsyncHttpClient();

    private static final String TAG = "StoreSelectionFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(view != null) {
            return view;
        }

        view = inflater.inflate(R.layout.no_loc_store_selection,container,false);
        Button scanQRStore = (Button) view.findViewById(R.id.scanQrStore);
        //Need to add code to find locaiton from the QR code from the service
        scanQRStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchScanBarcode(RC_SCAN_BARCODE_STORE);
            }
        });

        if(StateData.location != null)
        {
            view.setVisibility(View.GONE);
            findStoreByLocation(StateData.location);
        }
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

    //Methods to launch applications activities. scanType should be a predefined constant for store or product(i.e.RC_SCAN_BARCODE_STORE etc.)
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

        }
    }


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

    private void launchCartActivity()
    {
        if (SharedPreferrencesUtil.getStringPreference(getActivity(),"TransactionId") != null )
        {
            Date lastTransactionDate = SharedPreferrencesUtil.getDatePreference(getActivity(),"TransactionUpdatedDate",null);

            // if the last transaction was left pending under "N" minutes
            long minute_diff = CommonUtils.getDifferenceinMinutes(lastTransactionDate,CommonUtils.getCurrentDate());
            Log.d("tag","last pending transaction in "+ minute_diff);

            String status =  SharedPreferrencesUtil.getStringPreference(getActivity(),"TransactionStatus");

            if( minute_diff < TIMEOUT_TRANSACTION_MINS && status != null && (status.equalsIgnoreCase(TransactionStatus.SUSPENDED.name())))
            {
                StateData.transactionId =  SharedPreferrencesUtil.getStringPreference(getActivity(),"TransactionId");

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity(),R.style.DialogTheme);

                // set dialog message
                alertDialogBuilder
                        .setMessage(R.string.saved_transaction_dialog)
                        .setCancelable(false)
                        .setPositiveButton(R.string.continue_transaction,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                ((MainActivity)getActivity()).launchFragment(CART_ACTIVITY);

                            }
                        })
                        .setNegativeButton(R.string.start_over,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                StateData.transactionId = null;
                                SharedPreferrencesUtil.setStringPreference(getContext(),SP_TRANSACTION_ID, null);
                                ((MainActivity)getActivity()).launchFragment(CART_ACTIVITY);

                            }
                        });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alertDialog.show();

            }
            else {
                StateData.transactionId = null;
                ((MainActivity)getActivity()).launchFragment(CART_ACTIVITY);
            }

        }
        else
        {
            ((MainActivity)getActivity()).launchFragment(CART_ACTIVITY);
        }

    }

}
