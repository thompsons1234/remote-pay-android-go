/*
 * Copyright (C) 2016 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.remote.client.lib.example;

import com.clover.remote.client.lib.example.qrCode.barcode.BarcodeCaptureActivity;
import com.clover.sdk.util.Platform;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v3.scanner.BarcodeResult;
import com.clover.sdk.v3.scanner.BarcodeScanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.net.URI;
import java.net.URISyntaxException;

public class StartupActivity extends Activity {

  public static final String TAG = StartupActivity.class.getSimpleName();
  public static final String EXAMPLE_APP_NAME = "EXAMPLE_APP";
  public static final String LAN_PAY_DISPLAY_URL = "LAN_PAY_DISPLAY_URL";
  public static final String CONNECTION_MODE = "CONNECTION_MODE";
  public static final String USB = "USB";
  public static final String GO = "GO";
  public static final String LAN = "LAN";
  private static final int BARCODE_READER_REQUEST_CODE = 1;
  public static final String WS_CONFIG = "WS";

  // Clover devices do not always support the custom Barcode scanner implemented here.
  // They DO have a different capability to scan barcodes.
  // We do a switch based on the platform to allow the example app to run on station.
  private BarcodeScanner cloverBarcodeScanner;
  private BroadcastReceiver cloverBarcodeReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      BarcodeResult barcodeResult = new BarcodeResult(intent);

      if (barcodeResult.isBarcodeAction()) {
        String barcode = barcodeResult.getBarcode();
        Log.d(TAG, "Barcode from clover handler is " + barcode);
        if (barcode != null) {
          connect(parseValidateAndStoreURI(barcode), WS_CONFIG, false);
        }
      }
    }
  };

  private RadioGroup readerRadioGroup;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Fabric.with(this, new Crashlytics());
    setContentView(R.layout.activity_startup);

    loadBaseURL();

    if (null != getActionBar()) {
      getActionBar().hide();
    }

    readerRadioGroup = (RadioGroup)findViewById(R.id.readerRadioGroup);

    RadioGroup group = (RadioGroup)findViewById(R.id.radioGroup);
    group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        TextView textView = (TextView) findViewById(R.id.lanPayDisplayAddress);
        textView.setEnabled(checkedId == R.id.lanRadioButton);
        if (checkedId == R.id.goRadioButton){
          readerRadioGroup.setVisibility(View.VISIBLE);
          findViewById(R.id.llGoModes).setVisibility(View.VISIBLE);
          findViewById(R.id.connectButton).setVisibility(View.GONE);
        }else{
          readerRadioGroup.setVisibility(View.GONE);
          findViewById(R.id.llGoModes).setVisibility(View.GONE);
          findViewById(R.id.connectButton).setVisibility(View.VISIBLE);
        }
      }
    });

    Button connectButton = (Button)findViewById(R.id.connectButton);
    connectButton.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        cleanConnect(v);
        return true;
      }
    });

    // initialize...
    TextView textView = (TextView) findViewById(R.id.lanPayDisplayAddress);
    String url = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).getString(LAN_PAY_DISPLAY_URL,  "wss://192.168.1.101:12345/remote_pay");

    textView.setText(url);
    textView.setEnabled(((RadioGroup)findViewById(R.id.radioGroup)).getCheckedRadioButtonId() == R.id.lanRadioButton);

    String mode = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).getString(CONNECTION_MODE, USB);

    ((RadioButton)findViewById(R.id.lanRadioButton)).setChecked(LAN.equals(mode));
    ((RadioButton)findViewById(R.id.usbRadioButton)).setChecked(!LAN.equals(mode));

    // Switch out the barcode scanner for the Clover Devices
    if (Platform.isClover()) {
      cloverBarcodeScanner = new BarcodeScanner(this);
    }
  }

  private boolean loadBaseURL() {

    String _serverBaseURL = PreferenceManager.getDefaultSharedPreferences(this).getString(ExamplePOSActivity.EXAMPLE_POS_SERVER_KEY, "wss://10.0.0.101:12345/remote_pay");

    TextView tv = (TextView)findViewById(R.id.lanPayDisplayAddress);
    tv.setText(_serverBaseURL);

    Log.d(TAG, _serverBaseURL);
    return true;
  }


  public void scanQRCode(View view) {
    if (cloverBarcodeScanner == null) {
      // not clover, try the generic way
      Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
      startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
    } else {
      // It is a Clover device, use the Clover version
      Bundle extras = new Bundle();
      extras.putBoolean(Intents.EXTRA_LED_ON, false);
      extras.putBoolean(Intents.EXTRA_SCAN_QR_CODE, true);
      cloverBarcodeScanner.executeStartScan(extras);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    // If this is a clover device register the listener
    if (cloverBarcodeScanner != null) {
      registerCloverBarcodeScanner();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    // If this is a clover device unregister the listener
    if (cloverBarcodeScanner != null) {
      unregisterCloverBarcodeScanner();
    }
  }
  private void registerCloverBarcodeScanner() {
    registerReceiver(cloverBarcodeReceiver, new IntentFilter(BarcodeResult.INTENT_ACTION));
  }
  private void unregisterCloverBarcodeScanner() {
    unregisterReceiver(cloverBarcodeReceiver);
  }

  public void cleanConnect(View view) {
    connect(view, true);
  }

  public void connect(View view) {
    connect(view, false);
  }

  public void connect(View view, boolean clearToken) {

    RadioGroup group = (RadioGroup)findViewById(R.id.radioGroup);
    SharedPreferences prefs = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    URI uri = null;
    String config;

      if (group.getCheckedRadioButtonId() == R.id.goRadioButton) {
          config = "GO";
          editor.putString(CONNECTION_MODE, GO);
          editor.commit();
      } else if(group.getCheckedRadioButtonId() == R.id.usbRadioButton) {
      config = USB;
      editor.putString(CONNECTION_MODE, USB);
      editor.apply();
    } else { // (group.getCheckedRadioButtonId() == R.id.lanRadioButton)
      String uriStr = ((TextView)findViewById(R.id.lanPayDisplayAddress)).getText().toString();
      config = WS_CONFIG;
      uri = parseValidateAndStoreURI(uriStr);
    }
    connect(uri, config, clearToken);
  }

  private void connect(URI uri, String config, boolean clearToken) {
    Intent intent = new Intent();
    intent.setClass(this, ExamplePOSActivity.class);

    if(config.equals("USB") || (config.equals(WS_CONFIG) && uri != null)) {
      intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_CONNECTOR_CONFIG, config);
      intent.putExtra(ExamplePOSActivity.EXTRA_CLEAR_TOKEN, clearToken);
      intent.putExtra(ExamplePOSActivity.EXTRA_WS_ENDPOINT, uri);
      startActivity(intent);
    }
  }

  private URI parseValidateAndStoreURI(String uriStr) {
    try {
      SharedPreferences prefs = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      URI uri = new URI(uriStr);
      String addressOnly = String.format("%s://%s:%d%s", uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath());
      editor.putString(LAN_PAY_DISPLAY_URL, addressOnly);
      editor.putString(CONNECTION_MODE, LAN);
      editor.apply();
      return uri;
    } catch(URISyntaxException e) {
      Log.e(TAG, "Invalid URL" ,e);
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Error");
      builder.setMessage("Invalid URL");
      builder.show();
      return null;
    }
  }

    if(config.equals("USB") || (config.equals("WS") && uri != null)) {
      intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_CONNECTOR_CONFIG, config);
      intent.putExtra(ExamplePOSActivity.EXTRA_WS_ENDPOINT, uri);
      startActivity(intent);
    } else if (config.equals("GO") ){

        if (Validator.isNetworkConnected(this)){

            String apiKey = "Lht4CAQq8XxgRikjxwE71JE20by5dzlY";
            String secret = "7ebgf6ff8e98d1565ab988f5d770a911e36f0f2347e3ea4eb719478c55e74d9g";
            String accessToken = "533238e2-dbd7-98d8-ff6b-3e953d028e30";

            intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_CONNECTOR_CONFIG, config);
            intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_ACCESS_TOKEN, accessToken);
            intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_API_KEY, apiKey);
            intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_SECRET, secret);

            if (readerRadioGroup.getCheckedRadioButtonId() == R.id.rp450RadioButton)
                intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_READER_TYPE, RP450);
            else if (readerRadioGroup.getCheckedRadioButtonId() == R.id.rp350RadioButton)
                intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_READER_TYPE, RP350);

            startActivity(intent);
        }else {
            Toast.makeText(this,"Check Internet Connection",Toast.LENGTH_LONG).show();
        }

    }

  }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // For non-clover devices this is how the generic barcode scanner
        // returns the scanned barcode
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    connect(parseValidateAndStoreURI(barcode.displayValue), WS_CONFIG, false);
                }
            } else Log.e(TAG, String.format(getString(R.string.barcode_error_format),
                    CommonStatusCodes.getStatusCodeString(resultCode)));
        } else super.onActivityResult(requestCode, resultCode, data);
    }


    public void connectGoWithAuthMode(View view){

        String oAuthClientId = "1AST2ETARGG7C";
        String mOauthURL = "https://dev14.dev.clover.com/oauth/authorize?client_id=" + oAuthClientId + "&response_type=code";

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mOauthURL));
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            Uri uri = intent.getData();
            if (uri != null) {
                String merchantId = "";
                String employeeId = "";
                String clientId = "";
                String code = "";

                String url = uri.toString();

                String requiredDetails = url.substring(url.indexOf("?"));
                String[] data =  requiredDetails.split("&");
                for (String s : data) {
                    int splitIndex = s.indexOf("=");
                    String key =  s.substring(0, splitIndex);
                    String value = s.substring(splitIndex+1);
                    if (key.contains("merchant")) {
                        merchantId = value;
                    } else if (key.contains("employee")) {
                        employeeId = value;
                    } else if (key.contains("client")) {
                        clientId = value;
                    } else if (key.contains("code")) {
                        code = value;
                    }
                }
                getAccessToken(clientId, code);
            }
        }

    }

    private void getAccessToken(String clientId, String code) {

        String apiKey = "byJiyq2GZNmS6LgtAhr2xGS6gz4dpBYX";
        String clientSecret  = "fea4a38b-9346-d75c-2f09-1670381a1499";
        String env = "dev14.dev.clover.com";

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Merchant account loading....");
        progressDialog.show();


        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiInterface.getAccessToken(apiKey,clientId,clientSecret,code,env);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response!=null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        if (jsonObject.has("message")) {
                            String err = jsonObject.getString("message");
                            Toast.makeText(StartupActivity.this, err,Toast.LENGTH_SHORT).show();
                        } else if (jsonObject.has("access_token")){

                            if (Validator.isNetworkConnected(StartupActivity.this)){
                                String accessToken = jsonObject.getString("access_token");
                                String apiKey = "Lht4CAQq8XxgRikjxwE71JE20by5dzlY";
                                String secret = "7ebgf6ff8e98d1565ab988f5d770a911e36f0f2347e3ea4eb719478c55e74d9g";
                                String config = "GO";


                                Intent intent = new Intent();
                                intent.setClass(StartupActivity.this, ExamplePOSActivity.class);
                                intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_CONNECTOR_CONFIG, config);
                                intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_ACCESS_TOKEN, accessToken);
                                intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_API_KEY, apiKey);
                                intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_SECRET, secret);

                                if (readerRadioGroup.getCheckedRadioButtonId() == R.id.rp450RadioButton)
                                    intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_READER_TYPE, RP450);
                                else if (readerRadioGroup.getCheckedRadioButtonId() == R.id.rp350RadioButton)
                                    intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_READER_TYPE, RP350);

                                startActivity(intent);
                            }else {
                                Toast.makeText(StartupActivity.this,"Check Internet Connection",Toast.LENGTH_LONG).show();
                            }

                        }
                    }catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });


    }
}