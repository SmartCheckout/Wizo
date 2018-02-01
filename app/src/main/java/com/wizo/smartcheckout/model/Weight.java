package com.wizo.smartcheckout.model;

/**
 * Created by Swetha_Swaminathan on 11/12/2017.
 */

public class Weight {

    public enum Unit{KG, GM};

    private double value;
    private Unit unit;

    public Weight(double value,Unit unit)
    {
        this.value = value;
        this.unit = unit;
    }

    public double getvalue() {
        return value;
    }

    public void setvalue(double weight) {
        this.value = weight;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }




}
