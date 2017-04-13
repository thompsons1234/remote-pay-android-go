package com.clover.remote.client;

import com.clover.remote.client.clovergo.CloverGoConnectorImpl;
import com.clover.remote.client.clovergo.CloverGoDeviceConfiguration;
import com.clover.remote.client.device.CloverDeviceConfiguration;

public class ConnectorFactory {
  public static IPaymentConnector createPaymentConnector(CloverDeviceConfiguration configuration) {
    return createCloverConnector(configuration);
  }

  public static ICloverConnector createCloverConnector(CloverDeviceConfiguration configuration) {
    if (configuration instanceof CloverGoDeviceConfiguration) {
      return new CloverGoConnectorImpl((CloverGoDeviceConfiguration)configuration);
    }
    return new CloverConnector(configuration);
  }
}
