package com.clover.remote.client.lib.example.rest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Avdhesh Akhani on 3/15/17.
 */

public interface ApiInterface {


    @GET("clovergoOAuth")
    Call<ResponseBody> getAccessToken(@Query("apikey") String apiKey, @Query("client_id") String clientId, @Query("client_secret") String clientSecret, @Query("code") String code, @Query("environment") String env);


}
