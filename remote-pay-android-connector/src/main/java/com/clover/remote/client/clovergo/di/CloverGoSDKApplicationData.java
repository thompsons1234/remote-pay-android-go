package com.clover.remote.client.clovergo.di;

import android.content.Context;

import com.firstdata.clovergo.data.model.SDKApplicationData;

public class CloverGoSDKApplicationData extends SDKApplicationData {

  ApplicationComponent applicationComponent;

  public CloverGoSDKApplicationData(String s, String s1, Context context, String s2, String s3, String s4, String s5, String s6) {
    super(s, s1, context, s2, s3, s4, s5, s6);

    applicationComponent = DaggerApplicationComponent.builder().sDKDataComponent(dataComponent).build();
  }

  public ApplicationComponent getApplicationComponent(){
    return applicationComponent;
  }
}
