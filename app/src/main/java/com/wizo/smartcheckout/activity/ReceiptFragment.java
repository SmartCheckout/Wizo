package com.wizo.smartcheckout.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.EncodeHintType;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.wizo.smartcheckout.R;
import com.wizo.smartcheckout.adapter.BillListViewAdapter;
import com.wizo.smartcheckout.model.Bill;
import com.wizo.smartcheckout.model.CartItem;
import com.wizo.smartcheckout.model.Store;
import com.wizo.smartcheckout.model.Transaction;
import com.wizo.smartcheckout.util.CommonUtils;
import com.wizo.smartcheckout.util.SharedPreferrencesUtil;
import com.wizo.smartcheckout.util.StateData;
import com.wizo.smartcheckout.util.TransactionStatus;

import net.glxn.qrgen.android.QRCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.wizo.smartcheckout.constant.constants.CART_ACTIVITY;
import static com.wizo.smartcheckout.constant.constants.SP_TRANSACTION_ID;
import static com.wizo.smartcheckout.constant.constants.SP_TRANSACTION_STATUS;
import static com.wizo.smartcheckout.constant.constants.SP_TRANSACTION_UPDATED_TS;
import static com.wizo.smartcheckout.constant.constants.STORESELECTION_ACTIVITY;
import static com.wizo.smartcheckout.constant.constants.TRANSACTION_URL;


public class ReceiptFragment extends Fragment {

    private static String TAG = "ReceiptFragment";
    private View view ;
    private ListView mListView;
    ImageView myImage ;
    TextView amountView ;
    TextView subtotalView ;
    TextView taxView ;
    TextView totalView ;
    TextView savingsView;
    TextView storeNameView;

    private AsyncHttpClient ahttpClient = new AsyncHttpClient();
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(view != null) {
            return view;
        }

        view = inflater.inflate(R.layout.payment_success,container,false);
        mListView = (ListView) view.findViewById(R.id.cart_list);
         myImage = (ImageView) view.findViewById(R.id.trnsQRCode);
         amountView = ((TextView) view.findViewById(R.id.amount));
         subtotalView = ((TextView) view.findViewById(R.id.subtotalVal));
         taxView = ((TextView) view.findViewById(R.id.taxVal));
         totalView = ((TextView) view.findViewById(R.id.totalVal));
         savingsView = ((TextView) view.findViewById(R.id.savingsVal));
        storeNameView = ((TextView) view.findViewById(R.id.storeName));


        final String transactionId = getArguments().getString("TransactionId");



        Button shopAgain = (Button) view.findViewById(R.id.shopAgain);

        shopAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StateData.transactionReceipt = null;
                StateData.transactionId = null;
                ((MainActivity)getActivity()).launchFragment(STORESELECTION_ACTIVITY,null);

                SharedPreferrencesUtil.setStringPreference(getActivity(),SP_TRANSACTION_ID,null);
                SharedPreferrencesUtil.setStringPreference(getActivity(),SP_TRANSACTION_STATUS, null);

            }
        });


        Button viewReciept = (Button) view.findViewById(R.id.receipt);

        viewReciept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            // TODO: Email Implementation
            }
        });

        myImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Dialog builder = new Dialog(getActivity());
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                builder.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.WHITE));
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        //nothing;

                    }
                });

                ImageView imageView = new ImageView(getActivity());
                WindowManager.LayoutParams params = builder.getWindow().getAttributes();
                params.height = CommonUtils.getScreenWidth(getActivity()) - 200;
                params.width = CommonUtils.getScreenWidth(getActivity()) - 200;
                Bitmap myBitmap = QRCode.from(transactionId).withSize(params.height,params.width).withColor(0xFF000000,0x00FFFFFF).bitmap();
                imageView.setImageBitmap(myBitmap);
                //below code fullfil the requirement of xml layout file for dialoge popup


                builder.addContentView(imageView, params);
                builder.show();
                return false;
            }

        });



        if(StateData.transactionReceipt != null && StateData.transactionReceipt.getTrnsId().equalsIgnoreCase(transactionId))
        {
            Transaction transaction = StateData.transactionReceipt;

            if(transaction.getCart() != null && transaction.getStore() != null && transaction.getBill() != null)
                restoreView(inflater,transaction.getTrnsId(),transaction.getCart(),transaction.getStore(),transaction.getBill(),new Date(transaction.getTrnsDate()));

            return view;
        }


        // if there is no cached receipt, retrieve from the backend

        view.findViewById(R.id.billlayout).setVisibility(View.GONE);

        RequestParams rqstparams = new RequestParams();
        rqstparams.put("trnsId", transactionId);

        final ProgressDialog nDialog = new ProgressDialog(getActivity());
        nDialog.setMessage("Loading..");
        nDialog.setIndeterminate(true);
        nDialog.setCancelable(false);
        nDialog.show();

        ahttpClient.get(TRANSACTION_URL, rqstparams, new JsonHttpResponseHandler() {
            @SuppressLint("NewApi")
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Type listType = new TypeToken<ArrayList<CartItem>>() {}.getType();
                List<CartItem> cartList = null;
                Bill bill = null;
                Store store = null;
                try {
                    cartList = new Gson().fromJson(response.getJSONArray("cart").toString(), listType);
                    store = new Gson().fromJson(response.getJSONObject("store").toString(), Store.class);
                    bill = new Gson().fromJson(response.getJSONObject("bill").toString(), Bill.class);
                    Date transcationDate = new Date(response.getLong("trnsDate"));

                    nDialog.dismiss();
                    view.findViewById(R.id.billlayout).setVisibility(View.VISIBLE);
                    restoreView(inflater,transactionId,cartList,store,bill,transcationDate);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        return view;
    }


    public void restoreView(LayoutInflater inflater, String transactionId, List<CartItem> cartList, Store store, Bill bill, Date transcationDate )
    {
        Bitmap myBitmap = QRCode.from(transactionId).withSize(150,150).withColor(0xFF000000,0x00FFFFFF).bitmap();
        myImage.setImageBitmap(myBitmap);
        Log.d(TAG,"Transaction bitmap generated");

        final View headerView =inflater.inflate(R.layout.bill_item_header,null);

        mListView.addHeaderView(headerView);


        BillListViewAdapter billViewAdapter = new BillListViewAdapter(getActivity(),cartList);
        mListView.setAdapter(billViewAdapter);

        if(store != null)
        {
            storeNameView.setText(store.getTitle()+","+store.getAddress().getCity());
        }

        if(bill != null) {

            String newtext = amountView.getText().toString().concat(String.valueOf(bill.getTotal()));
            amountView.setText(newtext);

            newtext = subtotalView.getText().toString().concat(String.valueOf(bill.getSubTotal()));
            subtotalView.setText(newtext);

            newtext = taxView.getText().toString().concat(String.valueOf(bill.getTax()));
            taxView.setText(newtext);

            newtext = totalView.getText().toString().concat(String.valueOf(bill.getTotal()));
            totalView.setText(newtext);

            newtext = savingsView.getText().toString().concat(String.valueOf(bill.getSavings()));
            savingsView.setText(newtext);
        }
    }


}
