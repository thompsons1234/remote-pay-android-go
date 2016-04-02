package com.clover.remote.client.device;

import android.content.Context;
import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.usb.USBCloverTransport;

/**
 * Created by blakewilliams on 3/30/16.
 */
public class USBCloverDeviceConfiguration implements CloverDeviceConfiguration {
  Context context;

  public USBCloverDeviceConfiguration(Context ctx) {
    context = ctx;
  }

  @Override public String getCloverDeviceTypeName() {
    return DefaultCloverDevice.class.getCanonicalName();
  }

  @Override public String getMessagePackageName() {
    return "com.clover.remote.protocol.usb";
  }

  @Override public String getName() {
    return "Clover USB Connector";
  }

  @Override public CloverTransport getCloverTransport() {
    return new USBCloverTransport(context);
  }
}
