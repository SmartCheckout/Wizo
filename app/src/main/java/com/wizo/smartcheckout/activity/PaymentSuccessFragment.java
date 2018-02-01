package com.wizo.smartcheckout.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.wizo.smartcheckout.R;
import com.wizo.smartcheckout.util.CommonUtils;
import com.wizo.smartcheckout.util.SharedPreferrencesUtil;
import com.wizo.smartcheckout.util.StateData;
import com.wizo.smartcheckout.util.TransactionStatus;

import net.glxn.qrgen.android.QRCode;

import static com.wizo.smartcheckout.constant.constants.CART_ACTIVITY;
import static com.wizo.smartcheckout.constant.constants.SP_TRANSACTION_ID;
import static com.wizo.smartcheckout.constant.constants.SP_TRANSACTION_STATUS;
import static com.wizo.smartcheckout.constant.constants.SP_TRANSACTION_UPDATED_TS;
import static com.wizo.smartcheckout.constant.constants.STORESELECTION_ACTIVITY;


public class PaymentSuccessFragment extends Fragment {

    private static String TAG = "PaymentSuccessFragment";
    private View view ;
    private AsyncHttpClient ahttpClient = new AsyncHttpClient();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(view != null) {
            return view;
        }

        view = inflater.inflate(R.layout.payment_success,container,false);
        Bitmap myBitmap = QRCode.from(StateData.transactionId).withColor(0xFF000000,0x00FFFFFF).bitmap();

        ImageView myImage = (ImageView) view.findViewById(R.id.trnsQRCode);
        TextView amountView = ((TextView) view.findViewById(R.id.amount));
        TextView subtotalView = ((TextView) view.findViewById(R.id.subtotalVal));
        TextView taxView = ((TextView) view.findViewById(R.id.taxVal));
        TextView totalView = ((TextView) view.findViewById(R.id.totalVal));
        TextView savingsView = ((TextView) view.findViewById(R.id.savingsVal));

        if(amountView != null && amountView.getText() != null)
        {
            String newtext= amountView.getText().toString().concat(StateData.billAmount.toString());
            amountView.setText(newtext);

            newtext= subtotalView.getText().toString().concat(String.valueOf(StateData.transactionReceipt.getBill().getSubTotal()));
            subtotalView.setText(newtext);

            newtext= taxView.getText().toString().concat(String.valueOf(StateData.transactionReceipt.getBill().getTax()));
            taxView.setText(newtext);

            newtext= totalView.getText().toString().concat(String.valueOf(StateData.transactionReceipt.getBill().getTotal()));
            totalView.setText(newtext);

            newtext= savingsView.getText().toString().concat(String.valueOf(StateData.transactionReceipt.getBill().getSavings()));
            savingsView.setText(newtext);

        }

        myImage.setImageBitmap(myBitmap);
        Log.d(TAG,"Transaction bitmap generated");

        Button shopAgain = (Button) view.findViewById(R.id.shopAgain);

        shopAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StateData.transactionReceipt = null;
                StateData.transactionId = null;
                ((MainActivity)getActivity()).launchFragment(STORESELECTION_ACTIVITY);

                SharedPreferrencesUtil.setStringPreference(getActivity(),SP_TRANSACTION_ID,null);
                SharedPreferrencesUtil.setStringPreference(getActivity(),SP_TRANSACTION_STATUS, null);

            }
        });


//        Button viewReciept = (Button) view.findViewById(R.id.viewReciept);
//
//        viewReciept.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
////                Intent billViewIntent = new Intent(PaymentSuccessFragment.this, BillViewActivity.class);
////                billViewIntent.putExtra("TransactionId",StateData.transactionId);
////                startActivity(billViewIntent);
//            }
//        });

        return view;
    }

}
