package com.wizo.smartcheckout.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.wizo.smartcheckout.activity.PastTransactionFragment;
import com.wizo.smartcheckout.activity.PendingTransactionFragment;

/**
 * Created by Yesh on 2/25/2018.
 */

public class TransactionHistoryPageAdapter extends FragmentStatePagerAdapter{

    int mNumOfTabs;

    public TransactionHistoryPageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new PendingTransactionFragment();
            case 1:
                return new PastTransactionFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return "Pending";
            case 1:
                return "Completed";
            default:
                return null;
        }
    }

}
