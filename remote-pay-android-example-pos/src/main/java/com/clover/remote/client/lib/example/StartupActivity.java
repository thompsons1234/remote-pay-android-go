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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.remote.client.clovergo.CloverGoDeviceConfiguration;
import com.clover.remote.client.lib.example.qrCode.barcode.BarcodeCaptureActivity;
import com.clover.remote.client.lib.example.rest.ApiClient;
import com.clover.remote.client.lib.example.rest.ApiInterface;
import com.clover.remote.client.lib.example.utils.Validator;
import com.clover.sdk.util.Platform;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v3.scanner.BarcodeResult;
import com.clover.sdk.v3.scanner.BarcodeScanner;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import io.fabric.sdk.android.Fabric;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.firstdata.clovergo.domain.model.ReaderInfo.ReaderType.RP350;
import static com.firstdata.clovergo.domain.model.ReaderInfo.ReaderType.RP450;

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

  private static final CloverGoDeviceConfiguration.ENV GO_ENV = CloverGoDeviceConfiguration.ENV.LIVE;
  private static final String APP_ID = "com.firstdata.hack2020";

  private String mGoApiKey, mGoSecret, mGoAccessToken;
  private String mOAuthClientId, mOAuthClientSecret, mOAuthEnv, mOAuthUrl, mOAuthApiKey;


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
  private Toast mToast;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Fabric.with(this, new Crashlytics());
    setContentView(R.layout.activity_startup);

    loadBaseURL();

    if (null != getActionBar()) {
      getActionBar().hide();
    }

    readerRadioGroup = (RadioGroup) findViewById(R.id.readerRadioGroup);

    RadioGroup group = (RadioGroup) findViewById(R.id.radioGroup);
    group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        TextView textView = (TextView) findViewById(R.id.lanPayDisplayAddress);
        textView.setEnabled(checkedId == R.id.lanRadioButton);
        if (checkedId == R.id.goRadioButton) {
          readerRadioGroup.setVisibility(View.VISIBLE);
          findViewById(R.id.llGoModes).setVisibility(View.VISIBLE);
          findViewById(R.id.connectButton).setVisibility(View.GONE);
          findViewById(R.id.scanQRCode).setVisibility(View.GONE);
        } else {
          readerRadioGroup.setVisibility(View.GONE);
          findViewById(R.id.llGoModes).setVisibility(View.GONE);
          findViewById(R.id.connectButton).setVisibility(View.VISIBLE);
          findViewById(R.id.scanQRCode).setVisibility(View.VISIBLE);
        }
      }
    });

    Button connectButton = (Button) findViewById(R.id.connectButton);
    connectButton.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        cleanConnect(v);
        return true;
      }
    });

    // initialize...
    TextView textView = (TextView) findViewById(R.id.lanPayDisplayAddress);
    String url = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).getString(LAN_PAY_DISPLAY_URL, "wss://192.168.1.101:12345/remote_pay");

    textView.setText(url);
    textView.setEnabled(((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId() == R.id.lanRadioButton);

    String mode = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).getString(CONNECTION_MODE, USB);

    ((RadioButton) findViewById(R.id.lanRadioButton)).setChecked(LAN.equals(mode));
    ((RadioButton) findViewById(R.id.usbRadioButton)).setChecked(!LAN.equals(mode));
    ((RadioButton) findViewById(R.id.goRadioButton)).setChecked(GO.equalsIgnoreCase(mode));

    // Switch out the barcode scanner for the Clover Devices
    if (Platform.isClover()) {
      cloverBarcodeScanner = new BarcodeScanner(this);
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

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    if (intent != null && intent.getData() != null) {
      Uri uri = intent.getData();
      String merchantId = "";
      String employeeId = "";
      String clientId = "";
      String code = "";
      String value;

      for (String param : uri.getQueryParameterNames()) {
        value = uri.getQueryParameter(param);

        if (!TextUtils.isEmpty(value)) {
          if (param.equalsIgnoreCase("merchant_id")) {
            merchantId = value;
          } else if (param.equalsIgnoreCase("employee_id")) {
            employeeId = value;
          } else if (param.equalsIgnoreCase("client_id")) {
            clientId = value;
          } else if (param.equalsIgnoreCase("code")) {
            code = value;
          }
        }
      }
      getAccessToken(clientId, code);
    }
  }

  private boolean loadBaseURL() {
    String _serverBaseURL = PreferenceManager.getDefaultSharedPreferences(this).getString(ExamplePOSActivity.EXAMPLE_POS_SERVER_KEY, "wss://10.0.0.101:12345/remote_pay");

    TextView tv = (TextView) findViewById(R.id.lanPayDisplayAddress);
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
    RadioGroup group = (RadioGroup) findViewById(R.id.radioGroup);
    SharedPreferences prefs = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    URI uri = null;
    String config;

    if (group.getCheckedRadioButtonId() == R.id.goRadioButton) {
      setGoParams();
      config = GO;
      editor.putString(CONNECTION_MODE, GO);
      editor.commit();
    } else if (group.getCheckedRadioButtonId() == R.id.usbRadioButton) {
      config = USB;
      editor.putString(CONNECTION_MODE, USB);
      editor.apply();
    } else { // (group.getCheckedRadioButtonId() == R.id.lanRadioButton)
      String uriStr = ((TextView) findViewById(R.id.lanPayDisplayAddress)).getText().toString();
      config = WS_CONFIG;
      uri = parseValidateAndStoreURI(uriStr);
    }
    connect(uri, config, clearToken);
  }

  private void connect(URI uri, String config, boolean clearToken) {
    Intent intent = new Intent();
    intent.setClass(this, ExamplePOSActivity.class);

    if (config.equals("USB") || (config.equals(WS_CONFIG) && uri != null)) {
      intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_CONNECTOR_CONFIG, config);
      intent.putExtra(ExamplePOSActivity.EXTRA_CLEAR_TOKEN, clearToken);
      intent.putExtra(ExamplePOSActivity.EXTRA_WS_ENDPOINT, uri);
      startActivity(intent);

    } else if (config.equals("GO")) {
      if (Validator.isNetworkConnected(this)) {
        intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_CONNECTOR_CONFIG, config);
        intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_ACCESS_TOKEN, mGoAccessToken);
        intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_APP_ID, APP_ID);
        intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_API_KEY, mGoApiKey);
        intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_SECRET, mGoSecret);
        intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_ENV, GO_ENV);

        if (readerRadioGroup.getCheckedRadioButtonId() == R.id.rp450RadioButton) {
          intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_READER_TYPE, RP450);

        } else if (readerRadioGroup.getCheckedRadioButtonId() == R.id.rp350RadioButton) {
          intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_READER_TYPE, RP350);

        }

        startActivity(intent);
      } else {
        showToast("Check internet connection");

      }
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
    } catch (URISyntaxException e) {
      Log.e(TAG, "Invalid URL", e);
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Error");
      builder.setMessage("Invalid URL");
      builder.show();
      return null;
    }
  }

  public void connectGoWithAuthMode(View view) {
    setGoParams();

    if (((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId() == R.id.goRadioButton) {
      getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).edit().putString(CONNECTION_MODE, GO).commit();
    }

    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mOAuthUrl));
    startActivity(intent);
  }

  private void getAccessToken(String clientId, String code) {
    final ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("Merchant account loading....");
    progressDialog.show();

    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
    Call<ResponseBody> call = apiInterface.getAccessToken(mOAuthApiKey, clientId, mOAuthClientSecret, code, mOAuthEnv);

    call.enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        progressDialog.dismiss();
        if (response != null) {
          try {
            JSONObject jsonObject = new JSONObject(response.body().string());
            if (jsonObject.has("message")) {
              String err = jsonObject.getString("message");
              showToast(err);
            } else if (jsonObject.has("access_token")) {

              if (Validator.isNetworkConnected(StartupActivity.this)) {
                String accessToken = jsonObject.getString("access_token");
                String config = "GO";

                Intent intent = new Intent();
                intent.setClass(StartupActivity.this, ExamplePOSActivity.class);
                intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_CONNECTOR_CONFIG, config);
                intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_ACCESS_TOKEN, accessToken);
                intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_APP_ID, APP_ID);
                intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_API_KEY, mGoApiKey);
                intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_SECRET, mGoSecret);
                intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_ENV, GO_ENV);

                if (readerRadioGroup.getCheckedRadioButtonId() == R.id.rp450RadioButton)
                  intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_READER_TYPE, RP450);
                else if (readerRadioGroup.getCheckedRadioButtonId() == R.id.rp350RadioButton)
                  intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_READER_TYPE, RP350);

                startActivity(intent);
              } else {
                showToast("Check Internet Connection");

              }

            }
          } catch (JSONException | IOException e) {
            e.printStackTrace();
          }
        }
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
      }
    });
  }

  private void showToast(String message) {
    if (mToast == null) {
      mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }
    mToast.setText(message);
    mToast.show();
  }

  private void setGoParams() {
    mOAuthApiKey = "byJiyq2GZNmS6LgtAhr2xGS6gz4dpBYX";

    if (GO_ENV == CloverGoDeviceConfiguration.ENV.LIVE) {
      mGoApiKey = "mexbZJX5D3fa5kje1dZmrJVKOyAF9w8F";
      mGoSecret = "6hak16ff8e76r4565ab988f5d986a911e36f0f2347e3fv3eb719478c98e89io0";
      mGoAccessToken = "dd18d9a6-4bea-47e3-7d40-3ab8b0d61c29";

      mOAuthClientId = "K66BM82VZ4HAM";//PROD - CloverSDKDemoApp App ID
      mOAuthUrl = "https://clover.com/oauth/authorize?client_id=" + mOAuthClientId + "&response_type=code";

      mOAuthClientSecret = "6e2f4d4c-da09-a42c-fa72-bbd96a5c63aa"; //PROD - CloverSDKDemoApp App Secret
      mOAuthEnv = "www.clover.com";

    } else {
      mGoApiKey = "Lht4CAQq8XxgRikjxwE71JE20by5dzlY";
      mGoSecret = "7ebgf6ff8e98d1565ab988f5d770a911e36f0f2347e3ea4eb719478c55e74d9g";
      mGoAccessToken = "533238e2-dbd7-98d8-ff6b-3e953d028e30";

      mOAuthClientId = "1AST2ETARGG7C";
      mOAuthUrl = "https://stg1.dev.clover.com/oauth/authorize?client_id=" + mOAuthClientId + "&response_type=code";
//    String mOAuthUrl = "https://sandbox.dev.clover.com/oauth/authorize?client_id=" + mOAuthClientId + "&response_type=code";
//    String mOAuthUrl = "https://dev14.dev.clover.com/oauth/authorize?client_id=" + mOAuthClientId + "&response_type=code";

      mOAuthClientSecret = "fea4a38b-9346-d75c-2f09-1670381a1499";
      mOAuthEnv = "stg1.dev.clover.com";
    }
  }
}