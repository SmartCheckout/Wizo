package com.wizo.smartcheckout.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wizo.smartcheckout.R;
import com.wizo.smartcheckout.adapter.TransactionHistoryPageAdapter;

/**
 * Created by Yesh on 2/25/2018.
 */

public class TransactionHistoryTabbedFragment extends WizoFragment {
    @Override
    public void onBackPressed() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transaction_history,container,false);
        ViewPager viewPager = view.findViewById(R.id.pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        viewPager.setAdapter(new TransactionHistoryPageAdapter(getFragmentManager(),2));
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }
}
