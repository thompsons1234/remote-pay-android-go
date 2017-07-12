package com.clover.remote.client.transport;


public interface PairingDeviceConfiguration {
  void onPairingCode(String pairingCode);
  void onPairingSuccess(String authToken);
}
