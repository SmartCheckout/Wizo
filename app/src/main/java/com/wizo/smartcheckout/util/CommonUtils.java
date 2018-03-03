package com.wizo.smartcheckout.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.Toast;
import android.graphics.Bitmap;

import com.wizo.smartcheckout.R;

import net.glxn.qrgen.android.QRCode;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by swetha_swaminathan on 10/18/2017.
 */

public class CommonUtils {

    public static long getDifferenceinMinutes(Date date1, Date date2) {
        System.out.println(date1.toString());

        System.out.println(date2.toString());

        if (date1 == null || date2 == null)
            return 0;

        long diff = date2.getTime() - date1.getTime();

        long diffMinutes = TimeUnit.MINUTES.convert(diff,TimeUnit.MILLISECONDS);

        return diffMinutes;
    }

    public static Date getCurrentDate() {
        return new Date();
    }

    /**
     * Call this method on any method to check for internet connection
     * @param context
     * @return
     */
    public static boolean checkInternetConnection(final Context context) {
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null || info.isConnected() != true) {

            Toast.makeText(context, context.getResources().getString(R.string.toast_not_connected_internet), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }




    public static boolean isTransactionComplete()
    {
        if(StateData.status == TransactionStatus.INITIATED || StateData.status == TransactionStatus.CHECKOUT || StateData.status == TransactionStatus.SUSPENDED || StateData.status == TransactionStatus.PAYMENT_FAILURE)
            return false;

        return true;
    }
    public static int getScreenWidth(Activity ctx)
    {
        Display display = ctx.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = ctx.getResources().getDisplayMetrics().density;
        return Math.round(outMetrics.widthPixels );

    }

    public static int getScreenHeight(Activity ctx)
    {
        Display display = ctx.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = ctx.getResources().getDisplayMetrics().density;
        return Math.round(outMetrics.heightPixels );

    }

    public static Bitmap generateBitmap(String content, int foregroundColor, int backgroundColor){
        return QRCode.from(content).withColor(foregroundColor, backgroundColor).bitmap();
    }
    public static Bitmap generateBitmap(String content, int foregroundColor, int backgroundColor, int width, int height){
        return QRCode.from(content).withColor(foregroundColor, backgroundColor).withSize(width, height).bitmap();
    }
}
