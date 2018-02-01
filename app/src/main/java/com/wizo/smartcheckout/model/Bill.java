package com.wizo.smartcheckout.model;


import com.wizo.smartcheckout.util.Currency;

import java.math.BigDecimal;

/**
 * Created by rahul on 7/3/2017.
 */

public class Bill {

    private Double subTotal; // Total amount by adding MRP of all itens in the cart
    private Double savings;     // Total savings by calculating the disounts and offers
    private float taxPercent;
    private Double tax;       // T
    private Currency currency;
    private Double total;
    //Final amount to be paid
    private Double totalWeight;

    public Bill(Double subTotal, Double savings, float taxPercent, Currency currency) {
        this.subTotal = subTotal;
        this.savings = savings;
        this.taxPercent = taxPercent;
        this.currency = currency;
        notifyChanges();
    }

    public float getSubTotal() {
        return round(this.subTotal, 2);
    }

    public void setSubTotal(Double subTotal) {
        this.subTotal = subTotal;
    }

    public float getSavings() {
        return round(this.savings, 2);
    }

    public void setSavings(Double savings) {
        this.savings = savings;
    }

    public float getTax(){
        return round(this.tax, 2);
    }

    public float getTotal(){
        return round(this.total, 2);
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Currency getCurrency(){
        return this.currency;
    }

    public Double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(Double totalWeight) {
        this.totalWeight = totalWeight;
    }

    /*
        * Recalculates the tax and total amount of the bill.
        * Should be called by the client every time the subTotal amount or tax is changed.
        * */
    public void notifyChanges(){
        this.tax = this.subTotal * this.taxPercent;
        this.total =  this.subTotal + this.tax;
    }


    private float round(Double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
