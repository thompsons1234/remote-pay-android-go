package com.clover.remote.client.clovergo;

import com.clover.remote.client.device.CloverDeviceConfiguration;
import com.clover.remote.client.transport.CloverTransport;

import android.content.Context;

public class CloverGoDeviceConfiguration implements CloverDeviceConfiguration {
  private final Context context;
  private final String apiKey;
  private final String secret;
  private final String accessToken;
  private final String appId;

  public CloverGoDeviceConfiguration(Context context, String apiKey, String secret, String accessToken, String appId) {
    this.context = context;
    this.apiKey = apiKey;
    this.secret = secret;
    this.accessToken = accessToken;
    this.appId = appId;
  }

  @Override
  public String getCloverDeviceTypeName() {
    return "Clover Go";
  }

  @Override
  public String getMessagePackageName() {
    return "com.clover.remote.client.clovergo";
  }

  @Override
  public String getName() {
    return "Clover Go Device Configuration";
  }

  @Override
  public CloverTransport getCloverTransport() {
    // TODO: do we need this still?
    return null;
  }

  @Override
  public String getApplicationId() {
    return appId;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getSecret() {
    return secret;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public Context getContext() {
    return context;
  }
}
