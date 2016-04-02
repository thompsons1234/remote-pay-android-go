package com.clover.remote.client.transport.usb;

/**
 * Created by blakewilliams on 3/29/16.
 */
public interface USBCloverTransportServiceListener {
  void onMessage(String message);
  void onDeviceConnected();
  void onDeviceDisconnected();
  void onDeviceReady();
  void breakMe();
}
