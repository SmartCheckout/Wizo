package com.wizo.smartcheckout.util;


import android.location.Location;

import com.wizo.smartcheckout.model.Store;
import com.wizo.smartcheckout.model.Transaction;

import java.util.List;

/**
 * Created by yeshwanth on 8/17/2017.
 */

public class StateData {
    public static String userId = null;
    public static String userName = "Default User";
    public static String userEmail = "example@gmail.com";
    public static String userImage = null;

    public static String storeId = null;
    public static String storeName = null;
    public static String transactionId = null;
    public static TransactionStatus status = null;
    public static Transaction transactionReceipt = null;
    public static Store store;
    public static Float billAmount = 0.0f;
    public static Location location = null;

    public static List<Transaction> pendingTransactionList;
    public static List<Transaction> approvedTransactionList;
}
