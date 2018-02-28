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
import android.content.pm.PackageManager;
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

import static com.clover.remote.client.lib.example.AppConstants.CONFIG_TYPE_GO;
import static com.clover.remote.client.lib.example.AppConstants.CONFIG_TYPE_LAN;
import static com.clover.remote.client.lib.example.AppConstants.CONFIG_TYPE_USB;
import static com.clover.remote.client.lib.example.AppConstants.CONFIG_TYPE_WS;

public class StartupActivity extends Activity {

  public static final String TAG = StartupActivity.class.getSimpleName();
  public static final String EXAMPLE_APP_NAME = "EXAMPLE_APP";
  public static final String LAN_PAY_DISPLAY_URL = "LAN_PAY_DISPLAY_URL";
  public static final String CONNECTION_MODE = "CONNECTION_MODE";
  private static final int BARCODE_READER_REQUEST_CODE = 1;

  public static final int OAUTH_REQUEST_CODE = 100;
  public static final int OAUTH_REQUEST_TOKEN = 101;
  public static final String EXTRA_CLOVER_GO_CODE = "EXTRA_CLOVER_GO_CODE";
  public static final String EXTRA_CLOVER_GO_CLIENT = "EXTRA_CLOVER_GO_CLIENT";
  public static final String EXTRA_CLOVER_GO_ACCESS_TOKEN = "EXTRA_CLOVER_GO_ACCESS_TOKEN";

  /**
   * IMPORTANT: Set these values correctly
   * GO_ENV - e.g. demo, sandbox, prod
   * APP_ID - your app's ID
   * APP_VERSION - your app's version
   */
  private static final CloverGoDeviceConfiguration.ENV GO_ENV = CloverGoDeviceConfiguration.ENV.SANDBOX;
  private static final String APP_ID = "<put your APP ID here>";
  private static final String APP_VERSION = "<put your APP VERSION here>";

  private String mGoApiKey, mGoSecret, mGoAccessToken;
  private String mOAuthClientId, mOAuthClientSecret, mOAuthEnv, mOAuthUrl, mOAuthApiKey, mOAuthTokenUrl;

  private BarcodeScanner cloverBarcodeScanner;
  private BroadcastReceiver cloverBarcodeReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      BarcodeResult barcodeResult = new BarcodeResult(intent);

      if (barcodeResult.isBarcodeAction()) {
        String barcode = barcodeResult.getBarcode();
        Log.d(TAG, "Barcode from clover handler is " + barcode);
        if (barcode != null) {
          connect(parseValidateAndStoreURI(barcode), CONFIG_TYPE_WS, false);
        }
      }
    }
  };

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

    TextView version = ((TextView) findViewById(R.id.version));
    try {
      version.setText("Version : " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
    } catch (PackageManager.NameNotFoundException e) {
    }

    RadioGroup group = (RadioGroup) findViewById(R.id.radioGroup);
    group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        TextView textView = (TextView) findViewById(R.id.lanPayDisplayAddress);
        textView.setEnabled(checkedId == R.id.lanRadioButton);
        if (checkedId == R.id.goRadioButton) {
          findViewById(R.id.llGoModes).setVisibility(View.VISIBLE);
          findViewById(R.id.connectButton).setVisibility(View.GONE);
          findViewById(R.id.scanQRCode).setVisibility(View.GONE);
        } else {
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

    TextView textView = (TextView) findViewById(R.id.lanPayDisplayAddress);
    String url = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).getString(LAN_PAY_DISPLAY_URL, "wss://192.168.1.101:12345/remote_pay");

    textView.setText(url);
    textView.setEnabled(((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId() == R.id.lanRadioButton);

    String mode = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).getString(CONNECTION_MODE, CONFIG_TYPE_USB);

    ((RadioButton) findViewById(R.id.lanRadioButton)).setChecked(CONFIG_TYPE_LAN.equals(mode));
    ((RadioButton) findViewById(R.id.usbRadioButton)).setChecked(!CONFIG_TYPE_LAN.equals(mode));
    ((RadioButton) findViewById(R.id.goRadioButton)).setChecked(CONFIG_TYPE_GO.equalsIgnoreCase(mode));

    if (Platform.isClover()) {
      cloverBarcodeScanner = new BarcodeScanner(this);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (cloverBarcodeScanner != null) {
      registerCloverBarcodeScanner();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (cloverBarcodeScanner != null) {
      unregisterCloverBarcodeScanner();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == BARCODE_READER_REQUEST_CODE) {
      if (resultCode == CommonStatusCodes.SUCCESS) {
        if (data != null) {
          Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
          connect(parseValidateAndStoreURI(barcode.displayValue), CONFIG_TYPE_WS, false);
        }
      } else Log.e(TAG, String.format(getString(R.string.barcode_error_format),
          CommonStatusCodes.getStatusCodeString(resultCode)));
    } else if (data != null && requestCode == OAUTH_REQUEST_CODE) {
      getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).edit().putString(CONNECTION_MODE, CONFIG_TYPE_GO).commit();

      String clientId = data.getStringExtra(EXTRA_CLOVER_GO_CLIENT);
      String code = data.getStringExtra(EXTRA_CLOVER_GO_CODE);
      getAccessToken(clientId, code);

    } else if (data != null && requestCode == OAUTH_REQUEST_TOKEN) {
      setGoParams();
      getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).edit().putString(CONNECTION_MODE, CONFIG_TYPE_GO).commit();
      String token = data.getStringExtra(EXTRA_CLOVER_GO_ACCESS_TOKEN);

      Intent intent = new Intent();
      intent.setClass(StartupActivity.this, ExamplePOSActivity.class);
      populateIntentGoExtras(intent, token);

      startActivity(intent);
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
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
      Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
      startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
    } else {
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
      config = CONFIG_TYPE_GO;
      editor.putString(CONNECTION_MODE, CONFIG_TYPE_GO);
      editor.apply();
    } else if (group.getCheckedRadioButtonId() == R.id.usbRadioButton) {
      config = CONFIG_TYPE_USB;
      editor.putString(CONNECTION_MODE, CONFIG_TYPE_USB);
      editor.apply();
    } else {
      String uriStr = ((TextView) findViewById(R.id.lanPayDisplayAddress)).getText().toString();
      config = CONFIG_TYPE_WS;
      uri = parseValidateAndStoreURI(uriStr);
    }
    connect(uri, config, clearToken);
  }

  private void connect(URI uri, String config, boolean clearToken) {
    Intent intent = new Intent();
    intent.setClass(this, ExamplePOSActivity.class);

    if (config.equals(CONFIG_TYPE_USB) || (config.equals(CONFIG_TYPE_WS) && uri != null)) {
      intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_CONNECTOR_CONFIG, config);
      intent.putExtra(ExamplePOSActivity.EXTRA_CLEAR_TOKEN, clearToken);
      intent.putExtra(ExamplePOSActivity.EXTRA_WS_ENDPOINT, uri);
      startActivity(intent);

    } else if (config.equals(CONFIG_TYPE_GO)) {
      if (Validator.isNetworkConnected(this)) {
        populateIntentGoExtras(intent, mGoAccessToken);
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
      editor.putString(CONNECTION_MODE, CONFIG_TYPE_LAN);
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

  public void connectGoWithNewAuthMode(View view) {
    setGoParams();

    Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
    intent.putExtra(WebViewActivity.EXTRA_CLOVER_GO_TOKEN_URL, mOAuthTokenUrl);
    startActivityForResult(intent, OAUTH_REQUEST_TOKEN);
  }

  public void connectGoWithAuthMode(View view) {
    setGoParams();

    if (((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId() == R.id.goRadioButton) {
      getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).edit().putString(CONNECTION_MODE, CONFIG_TYPE_GO).commit();
    }

    Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
    intent.putExtra(WebViewActivity.EXTRA_CLOVER_GO_CODE_URL, mOAuthUrl);
    startActivityForResult(intent, OAUTH_REQUEST_CODE);
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

                Intent intent = new Intent();
                intent.setClass(StartupActivity.this, ExamplePOSActivity.class);
                populateIntentGoExtras(intent, accessToken);
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

  private void populateIntentGoExtras(Intent intent, String token) {
    intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_CONNECTOR_CONFIG, CONFIG_TYPE_GO);
    intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_ACCESS_TOKEN, token);
    intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_APP_ID, APP_ID);
    intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_APP_VERSION, APP_VERSION);
    intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_API_KEY, mGoApiKey);
    intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_SECRET, mGoSecret);
    intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_GO_CONNECTOR_ENV, GO_ENV);
  }

  private void setGoParams() {

    /**
     * These should be provided to you.
     */
    mGoApiKey = "<put your key here>";
    mGoSecret = "<put your secret here>";

    /**
     * App ID found in developer portal app settings.
     * App secret found in developer portal app settings.
     */
    mOAuthClientId = "<put your Client ID here>";
    mOAuthClientSecret = "<put your secret here>";

    /**
     * Update the URLs accordingly based on the environment you want to point to
     * e.g. sandbox.dev.clover.com or www.clover.com
     */
    mOAuthEnv = "www.clover.com";
    mOAuthUrl = "https://clover.com/oauth/authorize?client_id=" + mOAuthClientId + "&response_type=code";
    mOAuthTokenUrl = "https://clover.com/oauth/authorize?client_id=" + mOAuthClientId + "&response_type=token";


    /**
     * This is used for demo purposes. You can generate an access token and hardcode it here so
     * that you can use the same access token repeatedly.
     */
    mGoAccessToken = "<put your token here>";

    /**
     * This is not required and is intended for demo purposes only. Each client needs to have their
     * own implementation of getting the access token using the code generated by Clover.
     */
    mOAuthApiKey = "<put your key here>";
  }
}
