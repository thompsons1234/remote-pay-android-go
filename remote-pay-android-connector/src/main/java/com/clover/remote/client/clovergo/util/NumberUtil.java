package com.clover.remote.client.clovergo.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;

/**
 * Created by Arjun Chinya on 1/24/17.
 */

public class NumberUtil {

    public static String getDecimalString(double value) {
        NumberFormat nf = DecimalFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(true);
        return nf.format(value);
    }

    public static String getCurrencyString(double value){
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(true);
        nf.setCurrency(Currency.getInstance("USD"));
        return nf.format(value);
    }

    public static double parseCurrencyString(String value){
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        try {
            return nf.parse(value).doubleValue();
        }
        catch (Exception e){
            return 0;
        }
    }

    public static String getPercentageString(double value){
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumIntegerDigits(0);
        nf.setMaximumIntegerDigits(2);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(2);

        return nf.format(value);
    }


}
