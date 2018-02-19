package com.wizo.smartcheckout.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.wizo.smartcheckout.R;
import com.wizo.smartcheckout.activity.MainActivity;
import com.wizo.smartcheckout.model.Transaction;
import com.wizo.smartcheckout.util.CommonUtils;
import com.wizo.smartcheckout.util.StateData;

import net.glxn.qrgen.android.QRCode;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.wizo.smartcheckout.constant.constants.RECEIPT_ACTIVITY;

/**
 * Created by Swetha_Swaminathan on 11/13/2017.
 */

public class TransactionListViewAdapter extends BaseAdapter {

    List<Transaction> mDataSource = null;
    private Context mContext;
    private LayoutInflater mInflater;
    private int listLayoutId;

    public TransactionListViewAdapter(Context context, List<Transaction> transactionList,int listLayoutType)
    {
        mContext = context;
        mDataSource = transactionList;
        this.listLayoutId = listLayoutType;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = mInflater.inflate(listLayoutId, parent, false);


        final Transaction transaction = (Transaction) getItem(position);

        final MainActivity activity = ((MainActivity)mContext);
        if(listLayoutId == R.layout.pending_transaction_item)
        {
            ImageView myImage = (ImageView) rowView.findViewById(R.id.trnsQRCode);
            Bitmap myBitmap = QRCode.from(transaction.getTrnsId()).withColor(0xFF000000,0x00FFFFFF).bitmap();
            myImage.setImageBitmap(myBitmap);

        }



        TextView storeName =  rowView.findViewById(R.id.storeName);
        storeName.setText(transaction.getStore().getTitle());

        TextView storeAddress = rowView.findViewById(R.id.storeAddress);
        storeAddress.setText(transaction.getStore().getAddress().getCity()+" , ");

        TextView transactionDate = rowView.findViewById(R.id.transactionDate);
        SimpleDateFormat formatter = new SimpleDateFormat("d MMM yyyy");
        transactionDate.setText(formatter.format(transaction.getTrnsDate()));

        TextView billAmount = rowView.findViewById(R.id.billAmount);
        billAmount.setText(activity.getResources().getString(R.string.rupee) + String.valueOf(transaction.getBill().getTotal()));

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putString("TransactionId", transaction.getTrnsId());
                bundle.putString("CallingView", "History");

                if(listLayoutId == R.layout.pending_transaction_item)
                {
                   bundle.putBoolean("isPending",true);
                }
                else
                    bundle.putBoolean("isPending",false);
                activity.launchFragment(RECEIPT_ACTIVITY,bundle);


            }
        });

        return rowView;
    }
}
