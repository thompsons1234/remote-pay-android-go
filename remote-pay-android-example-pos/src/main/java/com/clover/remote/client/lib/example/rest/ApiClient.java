package com.clover.remote.client.lib.example.rest;

import retrofit2.Retrofit;

/**
 * Created by Avdhesh Akhani on 3/15/17.
 */

public class ApiClient {
  public static final String BASE_URL = "https://api-int.payeezy.com/"; //dev14?
  private static Retrofit retrofit = null;

  public static Retrofit getClient() {
    if (retrofit == null) {
      retrofit = new Retrofit.Builder()
              .baseUrl(BASE_URL)
              .build();
    }
    return retrofit;
  }
}