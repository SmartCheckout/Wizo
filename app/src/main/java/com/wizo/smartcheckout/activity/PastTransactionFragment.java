package com.wizo.smartcheckout.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

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
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.wizo.smartcheckout.constant.constants.TRANSACTION_LIST_SIZE;
import static com.wizo.smartcheckout.constant.constants.TRANSACTION_SEARCH_EP;

/**
 * Created by Yesh on 3/1/2018.
 */

public class PastTransactionFragment extends WizoFragment{
    private AsyncHttpClient ahttpClient = new AsyncHttpClient();
    private View mView;
    private ProgressBar mProgressBar;
    private ListView mApprovedTransactionListView;
    private String TAG = "PendingTransactionFragment";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Step 1 : Inflate the view
        if(mView!=null){
            return mView;
        }
        mView = inflater.inflate(R.layout.transaction_history_completed, container, false);
        Bundle inputBundle = getArguments();
        // Step 2 : Get the list view and progress bar
        mProgressBar = mView.findViewById(R.id.progress_bar);
        mApprovedTransactionListView = mView.findViewById(R.id.past_transaction_list);
        // Step 3 : Check state data for the transaction

        mProgressBar.setVisibility(View.VISIBLE);
        if (inputBundle != null && inputBundle.getBoolean("cache")) {
            Log.i(TAG, "Restoring cached transaction history");
            if (StateData.approvedTransactionList != null) {
                TransactionListViewAdapter transactionListViewAdapter = new TransactionListViewAdapter(getActivity(), StateData.approvedTransactionList, R.layout.past_transaction_item);
                mApprovedTransactionListView.setAdapter(transactionListViewAdapter);
            }
            return mView;
        }

        // Step 4 :
        getPendingTransactions();
        return mView;

    }

    protected void getPendingTransactions(){
        // Get the past transaction - Limit 5 per customer
        RequestParams rqstparams = new RequestParams();
        rqstparams.put("status", TransactionStatus.APPROVED);
        rqstparams.put("userId", StateData.userId);
        // TBD: change it to pagination when scrolling is implemented
        rqstparams.put("size",TRANSACTION_LIST_SIZE);
        rqstparams.put("page",0);

        ahttpClient.get(TRANSACTION_SEARCH_EP, rqstparams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Type listType = new TypeToken<ArrayList<Transaction>>() {}.getType();
                List<Transaction> transactionList = null;
                try {
                    transactionList = new Gson().fromJson(response.get("content").toString(), listType);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG,transactionList.size()+" ");
                StateData.approvedTransactionList = new ArrayList<>();
                StateData.approvedTransactionList.addAll(transactionList);
                TransactionListViewAdapter transactionListViewAdapter = new TransactionListViewAdapter(getActivity(),transactionList,R.layout.pending_transaction_item);
                mApprovedTransactionListView.setAdapter(transactionListViewAdapter);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d(TAG,errorResponse.toString());
                mProgressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onBackPressed() {

    }

}
