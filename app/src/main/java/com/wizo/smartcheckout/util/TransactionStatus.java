package com.wizo.smartcheckout.util;

/**
 * Created by yeshwanth on 8/21/2017.
 */

public enum TransactionStatus {
    CHECKOUT("Checkout"),
    PAYMENT_INITIATED("Payment Initiated"),
    PAYMENT_SUCCESSFUL("Payment Successful"),
    PAYMENT_FAILURE("Payment Failed"),
    APPROVED("Transaction Approved"),
    INITIATED("Transaction Initiated"),
    SUSPENDED("Transaction Suspended");

    private final String displayName;

    TransactionStatus(String displayName){
        this.displayName = displayName;
    }
    public String getDisplayName(){
        return this.displayName;
    }

}
