package com.clover.remote.client.lib.example;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.clover.common.util.CurrencyUtils;
import com.clover.common2.Signature2;
import com.clover.remote.client.CloverConnector;
import com.clover.remote.client.ICloverConnectorListener;
import com.clover.remote.client.device.WebSocketCloverDeviceConfiguration;
import com.clover.remote.client.lib.example.model.*;
import com.clover.remote.client.messages.*;
import com.clover.remote.order.DisplayDiscount;
import com.clover.remote.order.DisplayLineItem;
import com.clover.remote.order.DisplayOrder;
import com.clover.remote.protocol.CloverSdkDeserializer;
import com.clover.remote.protocol.CloverSdkSerializer;
import com.clover.remote.protocol.message.TipAddedMessage;
import com.clover.remote.terminal.InputOption;
import com.clover.remote.terminal.TxState;
import com.clover.sdk.v3.payments.Payment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


public class ExamplePOSActivity extends Activity implements CurrentOrderFragment.OnFragmentInteractionListener, AvailableItem.OnFragmentInteractionListener, OrdersFragment.OnFragmentInteractionListener, RegisterFragment.OnFragmentInteractionListener, SignatureFragment.OnFragmentInteractionListener, ProcessingFragment.OnFragmentInteractionListener {

    private static final String TAG = "ExamplePOSActivity";
    public static final String EXAMPLE_POS_SERVER_KEY = "clover_device_endpoint";
    public static final int WS_ENDPOINT_ACTIVITY = 123;
    public static final int SVR_ACTIVITY = 456;



    String _checksURL = null;

    CloverConnector cloverConnector;

    POSStore store = new POSStore();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_pos);

        if(loadBaseURL()) {

            initialize();

        }

        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.contentContainer);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        RegisterFragment register = RegisterFragment.newInstance(store, cloverConnector);

        fragmentTransaction.add(R.id.contentContainer, register, "REGISTER");
        fragmentTransaction.commit();



        // initialize store...
        store.addAvailableItem(new POSItem("1", "Hamburger", 759, true, true));
        store.addAvailableItem(new POSItem("2", "Cheeseburger", 699, true, true));
        store.addAvailableItem(new POSItem("3", "Double Hamburger", 819, true, true));
        store.addAvailableItem(new POSItem("4", "Double Cheeseburger", 899, true, true));
        store.addAvailableItem(new POSItem("5", "Bacon Cheeseburger", 999, true, true));
        store.addAvailableItem(new POSItem("6", "Small French Fries", 239, true, true));
        store.addAvailableItem(new POSItem("7", "Medium French Fries", 259, true, true));
        store.addAvailableItem(new POSItem("8", "Large French Fries", 279, true, true));
        store.addAvailableItem(new POSItem("9", "Small Fountain Drink", 169, true, true));
        store.addAvailableItem(new POSItem("10", "Medium Fountain Drink", 189, true, true));
        store.addAvailableItem(new POSItem("11", "Large Fountain Drink", 229, true, true));
        store.addAvailableItem(new POSItem("12", "Chocolate Milkshake", 449, true, true));
        store.addAvailableItem(new POSItem("13", "Vanilla Milkshake", 419, true, true));
        store.addAvailableItem(new POSItem("14", "Strawberry Milkshake", 439, true, true));
        store.addAvailableItem(new POSItem("15", "$25 Gift Card", 2500, false, false));
        store.addAvailableItem(new POSItem("16", "$50 Gift Card", 5000, false, false));

        store.addAvailableDiscount(new POSDiscount("10% Off", 0.1f));
        store.addAvailableDiscount(new POSDiscount("$5 Off", 500));
        store.addAvailableDiscount(new POSDiscount("None", 0));

        store.createOrder();
    }

    private boolean loadBaseURL() {
        String _serverBaseURL = PreferenceManager.getDefaultSharedPreferences(this).getString(EXAMPLE_POS_SERVER_KEY, null);

        if(_serverBaseURL == null || "".equals(_serverBaseURL.trim())) {
            Intent intent = new Intent(this, ExamplePOSSettingsActivity.class);
            startActivityForResult(intent, WS_ENDPOINT_ACTIVITY);
            return false;
        }

        _checksURL = _serverBaseURL;

        Log.d(TAG, _serverBaseURL);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == WS_ENDPOINT_ACTIVITY) {

            loadBaseURL();
            initialize();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_parent, menu);
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



    public void onClickWelcome(View view) {
        cloverConnector.showWelcomeScreen();
    }
    public void onClickThankYou(View view) {
        cloverConnector.showThankYouScreen();
    }
    public void onSale(View view) {
        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setAmount(2250L);

        cloverConnector.sale(saleRequest);
    }

    public void onAuth(View view) {
        AuthRequest authRequest = new AuthRequest(false);
        authRequest.setAmount(5000L);

        cloverConnector.sale(authRequest);
    }

    public void onPreAuth(View view) {
        AuthRequest authRequest = new AuthRequest(true);
        authRequest.setAmount(5000L);

        cloverConnector.sale(authRequest);
    }

    public void onClickStatus(View view) {

        //Toast.makeText(ExamplePOSActivity.this, "Status: " + cloverConnector.getStatus() + " [Error:" + cloverConnector.getLastException() + "]", Toast.LENGTH_SHORT).show();
    }

    public void onClickReconnect(View view) {
        //initialize();
    }

    public void onClickDisplayMessage(View view) {
        //EditText editText = (EditText)findViewById(R.id.DeviceM);
        //cloverConnector.showMessage(editText.getText().toString());
    }

    public void onPrintTextMessage(View view) {
//        EditText editText = (EditText)findViewById(R.id.PrintTextMessageContent);
//        List<String> messages = new ArrayList<String>();
//        messages.add(editText.getText().toString());
//        cloverConnector.printText(messages);
    }

    public void onClickCancel(View view) {
        cloverConnector.cancel();
    }


    public void initialize() {
        URI uri = null;
        try {
            if(cloverConnector != null) {
                cloverConnector.dispose();
            }
            uri = new URI(_checksURL);
            cloverConnector = new CloverConnector(new WebSocketCloverDeviceConfiguration(uri));
            cloverConnector.addCloverConnectorListener(new ICloverConnectorListener() {
                public void onDisconnected() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)findViewById(R.id.ConnectionStatusLabel)).setText("Disconnected");
                        }
                    });

                }

                public void onConnected() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(ExamplePOSActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
                            ((TextView)findViewById(R.id.ConnectionStatusLabel)).setText("Connecting");
                        }
                    });
                }

                public void onReady() {
                    runOnUiThread(new Runnable(){
                        public void run() {
                            Toast.makeText(ExamplePOSActivity.this, "Ready!", Toast.LENGTH_SHORT).show();
                            ((TextView)findViewById(R.id.ConnectionStatusLabel)).setText("Connected");
                        }
                    });
                }

                public void onError(final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ExamplePOSActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                public void onDebug(final String s) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ExamplePOSActivity.this, "Debug: " + s, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onDeviceActivityStart(final CloverDeviceEvent deviceEvent) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)findViewById(R.id.DeviceStatus)).setText(deviceEvent.getMessage());
                            //Toast.makeText(ExamplePOSActivity.this, deviceEvent.getMessage(), Toast.LENGTH_SHORT).show();
                            LinearLayout ll = (LinearLayout)findViewById(R.id.DeviceOptionsPanel);
                            for(final InputOption io : deviceEvent.getInputOptions()){
                                Button btn = new Button(ExamplePOSActivity.this);
                                btn.setText(io.description);
                                btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cloverConnector.invokeInputOption(io);
                                    }
                                });
                                ll.addView(btn);
                            }
                        }
                    });
                }

                @Override
                public void onDeviceActivityEnd(final CloverDeviceEvent deviceEvent) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)findViewById(R.id.DeviceStatus)).setText(deviceEvent.getMessage());
                            //Toast.makeText(ExamplePOSActivity.this, deviceEvent.getMessage(), Toast.LENGTH_SHORT).show();
                            LinearLayout ll = (LinearLayout)findViewById(R.id.DeviceOptionsPanel);
                            ll.removeAllViews();
                        }
                    });
                }

                @Override
                public void onDeviceError(CloverDeviceErrorEvent deviceErrorEvent) {

                }

                @Override
                public void onAuthResponse(final AuthResponse response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            store.addPaymentToOrder(new POSPayment(response.getPayment().getId(), response.getPayment().getOrder().getId(), "DFLTEMPLYEE", response.getPayment().getAmount()), store.getCurrentOrder());
                        }
                    });
                }

                @Override
                public void onAuthTipAdjustResponse(TipAdjustAuthResponse response) {

                }

                @Override
                public void onAuthCaptureResponse(CaptureAuthResponse response) {

                }

                @Override
                public void onSignatureVerifyRequest(SignatureVerifyRequest request) {

                    /*
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    SignatureFragment signature = SignatureFragment.newInstance(request, cloverConnector);

                    fragmentTransaction.replace(R.id.contentContainer, signature, "SIGNATURE");
                    fragmentTransaction.commit();
                    */


                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    hideFragments(fragmentManager, fragmentTransaction);

                    Fragment fragment = fragmentManager.findFragmentByTag("SIGNATURE");
                    if(fragment == null) {
                        fragment = SignatureFragment.newInstance(request, cloverConnector);
                        fragmentTransaction.add(R.id.contentContainer, fragment, "SIGNATURE");
                    } else {
                        ((SignatureFragment)fragment).setSignatureVerifyRequest(request);
                        fragmentTransaction.show(fragment);
                    }

                    fragmentTransaction.commit();
                }

                @Override
                public void onCloseoutResponse(CloseoutResponse response) {

                }

                @Override
                public void onSaleResponse(final SaleResponse response) {
                    store.addPaymentToOrder(new POSPayment(response.getPayment().getId(), response.getPayment().getOrder().getId(), "DFLTEMPLYEE", response.getPayment().getAmount()), store.getCurrentOrder());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            store.createOrder();
                            CurrentOrderFragment currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
                            currentOrderFragment.setOrder(store.getCurrentOrder());
                            cloverConnector.showWelcomeScreen();

                            showRegister(null);
                        }
                    });
                }

                @Override
                public void onManualRefundResponse(final ManualRefundResponse response) {
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            Toast.makeText(ExamplePOSActivity.this, "Refund: " + response.getCredit().getAmount(), Toast.LENGTH_LONG);
                        }
                    });
                    //store.refunds.add(new POSRefund(response.getCredit().getId(), response.getCredit().getOrderRef().getId(), "DFLTEMPLYEE", response.getCredit().getAmount()));
                }

                @Override
                public void onRefundPaymentResponse(RefundPaymentResponse response) {
                }

                @Override
                public void onTipAdded(TipAddedMessage message) {

                }

                @Override
                public void onVoidPaymentResponse(VoidPaymentResponse response) {

                }

                @Override
                public void onCaptureCardResponse(CaptureCardResponse response) {

                }

                @Override
                public void onTransactionState(TxState txState) {

                }
            });
            //cloverConnector.initialize(uri);

            //Toast.makeText(ExamplePOSActivity.this, "Last Exception: " + cloverConnector.getLastException(), Toast.LENGTH_LONG).show();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void showSettings(MenuItem item) {
        Intent intent = new Intent(this, ExamplePOSSettingsActivity.class);
        startActivityForResult(intent, WS_ENDPOINT_ACTIVITY);
    }

    public void showOrders(View view) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        hideFragments(fragmentManager, fragmentTransaction);

        Fragment fragment = fragmentManager.findFragmentByTag("ORDERS");
        if(fragment == null) {
            fragment = OrdersFragment.newInstance(store, cloverConnector);
            ((OrdersFragment)fragment).setCloverConnector(cloverConnector);
            fragmentTransaction.add(R.id.contentContainer, fragment, "ORDERS");
        } else {
            fragmentTransaction.show(fragment);
        }

        fragmentTransaction.commit();
    }

    public void showRegister(View view) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        hideFragments(fragmentManager, fragmentTransaction);

        Fragment fragment = fragmentManager.findFragmentByTag("REGISTER");
        if(fragment == null) {
            fragment = RegisterFragment.newInstance(store, cloverConnector);
            fragmentTransaction.add(R.id.contentContainer, fragment, "REGISTER");
        } else {
            fragmentTransaction.show(fragment);
        }

        fragmentTransaction.commit();
    }

    private void hideFragments(FragmentManager fragmentManager, FragmentTransaction fragmentTransaction) {
        Fragment fragment = fragmentManager.findFragmentByTag("ORDERS");
        if(fragment != null) {
            fragmentTransaction.hide(fragment);
        }
        fragment = fragmentManager.findFragmentByTag("REGISTER");
        if(fragment != null) {
            fragmentTransaction.hide(fragment);
        }
        fragment = fragmentManager.findFragmentByTag("SIGNATURE");
        if(fragment != null) {
            fragmentTransaction.hide(fragment);
        }

    }
}
