package com.wizo.smartcheckout.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.razorpay.PaymentResultListener;
import com.wizo.smartcheckout.R;
import com.wizo.smartcheckout.util.SharedPreferrencesUtil;
import com.wizo.smartcheckout.util.StateData;
import com.wizo.smartcheckout.util.TransactionStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

import static com.wizo.smartcheckout.constant.constants.CART_ACTIVITY;
import static com.wizo.smartcheckout.constant.constants.RC_CHECK_SETTING;
import static com.wizo.smartcheckout.constant.constants.RC_SCAN_BARCODE_STORE;
import static com.wizo.smartcheckout.constant.constants.RECEIPT_ACTIVITY;
import static com.wizo.smartcheckout.constant.constants.SP_TRANSACTION_ID;
import static com.wizo.smartcheckout.constant.constants.SP_TRANSACTION_STATUS;
import static com.wizo.smartcheckout.constant.constants.STORESELECTION_ACTIVITY;
import static com.wizo.smartcheckout.constant.constants.TRANSACTION_SUMMARY_ACTIVITY;
import static com.wizo.smartcheckout.constant.constants.TRANSACTION_UPDATE_EP;


public class MainActivity extends AppCompatActivity
        implements PaymentResultListener, NavigationView.OnNavigationItemSelectedListener
         {
    // tags used to attach the fragments
    private static final String TAG_HOME = "home";

    private Handler mHandler;

    private static final String TAG = "MainActivity";

    private WizoFragment currentFragment;


    private AsyncHttpClient ahttpClient = new AsyncHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mHandler = new Handler();
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header=navigationView.getHeaderView(0);
        TextView name = header.findViewById(R.id.userName);
        TextView email =header.findViewById(R.id.userEmail);
        name.setText(StateData.userName);
        email.setText(StateData.userEmail);


        if(StateData.store == null)
            launchFragment(STORESELECTION_ACTIVITY);

        final Intent receivingIntent = getIntent();
        final int nextActivity = receivingIntent.getIntExtra("next_activity",0);
        if(nextActivity != 0)
        {
            launchFragment(nextActivity,null);
        }

    }


    public void launchFragment(final int fragmentId) {
		launchFragment(fragmentId, null);
	}

    public void launchFragment(final int fragmentId, final Bundle bundle)
    {

        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Log.d("Intent received", fragmentId + "");
                WizoFragment fragment = getHomeFragment(fragmentId);
                currentFragment = fragment;
                Log.d("Loaded Fragment", "" + fragment);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                if(bundle != null)
                    fragment.setArguments(bundle);


                fragmentTransaction.replace(R.id.content_layout, fragment, String.valueOf(fragmentId));

                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };
        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }
    }

             @Override
             public void onActivityResult(int requestCode, int resultCode, Intent data) {
                 super.onActivityResult(requestCode, resultCode, data);
                 switch (requestCode) {
                     case RC_CHECK_SETTING:
                         // Response from location enabled
                         Fragment frg = getSupportFragmentManager().findFragmentByTag(String.valueOf(STORESELECTION_ACTIVITY));
                         if (frg != null) {
                             frg.onActivityResult(requestCode, resultCode, data);
                         }
                         break;
                     case RC_SCAN_BARCODE_STORE:
                         Fragment frg1 = getSupportFragmentManager().findFragmentByTag(String.valueOf(CART_ACTIVITY));
                         if (frg1 != null) {
                             frg1.onActivityResult(requestCode, resultCode, data);
                         }
                 }
             }
             

    @Override

    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private WizoFragment getHomeFragment(int navItemIndex) {
        switch (navItemIndex) {


            case CART_ACTIVITY:
                CartFragment cartFragment = new CartFragment();
                return cartFragment;

            case STORESELECTION_ACTIVITY:
                StoreSelectionFragment storeSelectionFragment = new StoreSelectionFragment();
                return storeSelectionFragment;

            case RECEIPT_ACTIVITY:
                ReceiptFragment paymentSuccessFragment = new ReceiptFragment();
                return paymentSuccessFragment;

            case TRANSACTION_SUMMARY_ACTIVITY:
                TransactionHistoryTabbedFragment transactionSummaryFragment = new TransactionHistoryTabbedFragment();
                return transactionSummaryFragment;

        }

        return null;
    }

    @Override
    public void onBackPressed() {

        currentFragment.onBackPressed();

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_shopnow) {
            FrameLayout frame =  findViewById(R.id.content_layout);
            frame.removeAllViews();
            launchFragment(STORESELECTION_ACTIVITY);


        } else if (id == R.id.nav_search) {
            FrameLayout frame =  findViewById(R.id.content_layout);
            frame.removeAllViews();
            LayoutInflater.from(getApplicationContext()).inflate(R.layout.coming_soon, frame, true);


        } else if (id == R.id.nav_transactions) {

            FrameLayout frame =  findViewById(R.id.content_layout);
            frame.removeAllViews();
            Bundle inputBundle = new Bundle();
            inputBundle.putBoolean("cache",false);
            launchFragment(TRANSACTION_SUMMARY_ACTIVITY,inputBundle);
        } else if (id == R.id.nav_offers) {
            FrameLayout frame =  findViewById(R.id.content_layout);
            frame.removeAllViews();
            LayoutInflater.from(getApplicationContext()).inflate(R.layout.coming_soon, frame, true);
        } else if (id == R.id.nav_share) {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,getResources().getString(R.string.share_message));
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

        } else if (id == R.id.nav_send) {

            Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
            sendIntent.setType("plain/text");
            sendIntent.setData(Uri.parse("mailto:"));
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Customer Feedback - "+new Date());
            sendIntent.putExtra(Intent.EXTRA_EMAIL, Uri.parse(getResources().getString(R.string.support_email)));
            if (sendIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(sendIntent);
            }
        } else if (id == R.id.nav_signout) {
            StateData.store = null;
            StateData.storeId = null;
            StateData.storeName = null;
            AuthUI.getInstance()
                    .signOut(MainActivity.this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        }
                    });

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        // Display transaction id QR code
        JSONObject updateTransReq = new JSONObject();
        try{
            // Updating transaction status and payment reference
            JSONObject payment = new JSONObject();
            payment.put("paymentGateway","RAZOR_PAY");
            payment.put("paymentRef",razorpayPaymentID);
            payment.put("paymentStatus","SUCCESS");

            updateTransReq.put("trnsId", StateData.transactionId);
            updateTransReq.put("status", TransactionStatus.PAYMENT_SUCCESSFUL);
            updateTransReq.put("payment", new JSONArray().put(payment));

            StateData.status = TransactionStatus.PAYMENT_SUCCESSFUL;
            StateData.transactionReceipt.setStatus(TransactionStatus.PAYMENT_SUCCESSFUL.toString());
            StringEntity requestEntity = new StringEntity(updateTransReq.toString(), ContentType.APPLICATION_JSON);

            SharedPreferrencesUtil.setStringPreference(this, SP_TRANSACTION_ID, null);
            SharedPreferrencesUtil.setStringPreference(this, SP_TRANSACTION_STATUS, null);

            ahttpClient.post(this, TRANSACTION_UPDATE_EP, requestEntity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        //Setting transaction id into state data
                        Log.d(TAG, "Update Transaction Successful");
                        StateData.transactionId = response.getString("trnsId");
                        Log.d(TAG, "Updated transaction id : " + StateData.transactionId);
                        Bundle bundle = new Bundle();
                        bundle.putString("TransactionId", StateData.transactionId);
                        bundle.putString("CallingView", "Cart");
                        launchFragment(RECEIPT_ACTIVITY,bundle);

                    } catch (Exception e) {
                        // TODO: throw custom exception
                    }
                }
            });

            Log.d(TAG,"Update transaction status triggered. " + updateTransReq.toString());

        }catch(Exception e){
            //Todo
        }
    }

    @Override
    public void onPaymentError(int i, String s) {

    }
}
