package com.wizo.smartcheckout.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.wizo.smartcheckout.R;
import com.wizo.smartcheckout.adapter.TransactionListViewAdapter;
import com.wizo.smartcheckout.model.Transaction;
import com.wizo.smartcheckout.util.StateData;
import com.wizo.smartcheckout.util.TransactionStatus;


import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.wizo.smartcheckout.constant.constants.TRANSACTION_LIST_SIZE;
import static com.wizo.smartcheckout.constant.constants.TRANSACTION_SEARCH_EP;


/**
 * Created by Swetha_Swaminathan on 11/13/2017.
 */

public class TransactionSummaryFragment extends WizoFragment {

    private AsyncHttpClient ahttpClient = new AsyncHttpClient();
    private ListView mListPastTransactionView;
    private ListView mListCurrentTransactionView;
    private View view;
    private  String TAG ="TransactionSummaryFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.transaction_view,container,false);

        Bundle inputBundle = getArguments();
        mListPastTransactionView = (ListView) view.findViewById(R.id.past_transaction_list);
        mListCurrentTransactionView = (ListView) view.findViewById(R.id.pending_transaction_list);

        if(inputBundle != null && inputBundle.getBoolean("cache"))
        {
            Log.i(TAG,"Restoring cached transaction history");

            if(StateData.pendingTransactionList != null)
            {
                TransactionListViewAdapter transactionListViewAdapter = new TransactionListViewAdapter(getActivity(),StateData.pendingTransactionList,R.layout.past_transaction_item);
                mListPastTransactionView.setAdapter(transactionListViewAdapter);
            }

            if (StateData.pastTransactionList != null) {
                TransactionListViewAdapter transactionListViewAdapter = new TransactionListViewAdapter(getActivity(),StateData.pastTransactionList,R.layout.pending_transaction_item);
                mListCurrentTransactionView.setAdapter(transactionListViewAdapter);

            }

            return view;

        }

        Log.i(TAG,"fetching latest transaction history");


        view.findViewById(R.id.transaction_history_view).setVisibility(View.GONE);


        final ProgressDialog nDialog = new ProgressDialog(getActivity());
        nDialog.setMessage("Loading..");
        nDialog.setIndeterminate(true);
        nDialog.setCancelable(false);
        nDialog.show();

        // Get the past transaction - Limit 5 per customer
        RequestParams rqstparams = new RequestParams();
        rqstparams.put("status", TransactionStatus.APPROVED);
        rqstparams.put("userId", StateData.userId);
        // TBD: change it to pagination when scrolling is implemented
        rqstparams.put("size",TRANSACTION_LIST_SIZE);
        rqstparams.put("page",0);

        ahttpClient.get(TRANSACTION_SEARCH_EP, rqstparams, new JsonHttpResponseHandler() {
            @SuppressLint("NewApi")

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Type listType = new TypeToken<ArrayList<Transaction>>() {}.getType();
                List<Transaction> transactionList = null;
                try {
                    transactionList = new Gson().fromJson(response.get("content").toString(), listType);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("Shwe",transactionList.size()+" ");
                StateData.pendingTransactionList = new ArrayList<>();
                StateData.pendingTransactionList.addAll(transactionList);
                TransactionListViewAdapter transactionListViewAdapter = new TransactionListViewAdapter(getActivity(),transactionList,R.layout.past_transaction_item);
                mListPastTransactionView.setAdapter(transactionListViewAdapter);
                nDialog.dismiss();
                view.findViewById(R.id.transaction_history_view).setVisibility(View.VISIBLE);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("Shwe",errorResponse.toString());
                nDialog.dismiss();
            }
        });

        // Get the current transaction - Limit 5 per customer

        RequestParams rqstparams1 = new RequestParams();

        rqstparams1.put("status",TransactionStatus.PAYMENT_SUCCESSFUL);
        rqstparams1.put("userId", StateData.userId);
        rqstparams.put("size",2);
        rqstparams.put("page",0);

        ahttpClient.get(TRANSACTION_SEARCH_EP, rqstparams1, new JsonHttpResponseHandler() {
            @SuppressLint("NewApi")

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Type listType = new TypeToken<ArrayList<Transaction>>() {}.getType();
                List<Transaction> transactionList = null;
                try {
                    transactionList = new Gson().fromJson(response.get("content").toString(), listType);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                StateData.pastTransactionList = new ArrayList<>();
                StateData.pastTransactionList.addAll(transactionList);
                TransactionListViewAdapter transactionListViewAdapter = new TransactionListViewAdapter(getActivity(),transactionList,R.layout.pending_transaction_item);
                mListCurrentTransactionView.setAdapter(transactionListViewAdapter);
                nDialog.dismiss();
                view.findViewById(R.id.transaction_history_view).setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("Shwe",errorResponse.toString());
                nDialog.dismiss();
            }
        });

        return view;
    }

    @Override
    public void onBackPressed()
    {
        // Do Nothing
    }


}
