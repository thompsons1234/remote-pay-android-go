package com.clover.remote.client.transport.usb;

/**
 * Created by blakewilliams on 3/29/16.
 */
public interface USBCloverTransportService {
  void sendMessage(String message);
  void addListener(USBCloverTransportServiceListener listener);
  void removeListener(USBCloverTransportServiceListener listener);
}
