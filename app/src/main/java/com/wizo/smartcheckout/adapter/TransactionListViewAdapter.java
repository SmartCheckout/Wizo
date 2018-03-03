package com.wizo.smartcheckout.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wizo.smartcheckout.R;
import com.wizo.smartcheckout.activity.MainActivity;
import com.wizo.smartcheckout.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.wizo.smartcheckout.constant.constants.RECEIPT_ACTIVITY;

/**
 * Created by Swetha_Swaminathan on 11/13/2017.
 */

public class TransactionListViewAdapter extends BaseAdapter {

    private static class ViewHolder {
        public final TextView mlStoreName;
        public final TextView mlStoreAddress;
        public final TextView mTransactionDate;
        public final TextView mBillCurrency;
        public final TextView mBillAmount;
        public final ImageView mTrnsQRCode;

        public ViewHolder(TextView mlStoreName, TextView mlStoreAddress,
                          TextView mTransactionDate, TextView mBillCurrency, TextView mBillAmount, ImageView mTrnsQRCode) {
            this.mlStoreName = mlStoreName;
            this.mlStoreAddress = mlStoreAddress;
            this.mTransactionDate = mTransactionDate;
            this.mBillCurrency = mBillCurrency;
            this.mBillAmount = mBillAmount;
            this.mTrnsQRCode = mTrnsQRCode;
        }
    }

    List<Transaction> mDataSource = null;
    private Context mContext;
    private LayoutInflater mInflater;
    private int mListLayoutId;

    public TransactionListViewAdapter(Context context, List<Transaction> transactionList,int listLayoutType){
        this.mContext = context;
        this.mDataSource = transactionList;
        this.mListLayoutId = listLayoutType;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        final MainActivity activity = ((MainActivity)mContext);
        final Transaction transaction = (Transaction) getItem(position);
        View lDetailedView, lBillView;
        TextView lStoreName, lStoreAddress, lTransactionDate, lBillCurrency, lBillAmount;
        ImageView lTrnsQRCode;

        if (null == convertView) {
            convertView = mInflater.inflate(mListLayoutId, parent, false);
            lStoreName = convertView.findViewById(R.id.storeName);
            lStoreAddress = convertView.findViewById(R.id.storeAddress);
            lTransactionDate = convertView.findViewById(R.id.transactionDate);
            lBillCurrency = convertView.findViewById(R.id.billCurrency);
            lBillAmount = convertView.findViewById(R.id.billAmount);
            lTrnsQRCode = convertView.findViewById(R.id.trnsQRCode);
            convertView.setTag(new ViewHolder(lStoreName, lStoreAddress, lTransactionDate, lBillCurrency, lBillAmount, lTrnsQRCode));
        }else{
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            lStoreName = viewHolder.mlStoreName;
            lStoreAddress = viewHolder.mlStoreAddress;
            lTransactionDate = viewHolder.mTransactionDate;
            lBillCurrency = viewHolder.mBillCurrency;
            lBillAmount = viewHolder.mBillAmount;
            lTrnsQRCode = viewHolder.mTrnsQRCode;
        }

        DetailedViewListener lDetailedViewListener = new DetailedViewListener(transaction, activity);

        if(mListLayoutId == R.layout.pending_transaction_item){
            //lTrnsQRCode.setImageBitmap(CommonUtils.generateBitmap(transaction.getTrnsId(),0xFF000000, 0x00FFFFFF));
        }

        lStoreName.setText(transaction.getStore().getTitle());
        lStoreAddress.setText(transaction.getStore().getAddress().getCity());
        SimpleDateFormat formatter = new SimpleDateFormat("d MMM yyyy");
        lTransactionDate.setText(formatter.format(transaction.getTrnsDate()));
        lBillCurrency.setText(activity.getResources().getString(R.string.rupee));
        lBillAmount.setText(String.valueOf(transaction.getBill().getTotal()));
        convertView.setOnClickListener(lDetailedViewListener);

        return convertView;
    }

    private class DetailedViewListener implements View.OnClickListener {

        private Transaction mTransaction;
        private MainActivity mActivity;

        public DetailedViewListener(Transaction transaction, MainActivity activity) {
            this.mActivity = activity;
            this.mTransaction = transaction;
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putString("TransactionId", mTransaction.getTrnsId());
            bundle.putString("CallingView", "History");

            if (mListLayoutId == R.layout.pending_transaction_item) {
                bundle.putBoolean("isPending", true);
            } else
                bundle.putBoolean("isPending", false);
            mActivity.launchFragment(RECEIPT_ACTIVITY, bundle);


        }
    }
}
