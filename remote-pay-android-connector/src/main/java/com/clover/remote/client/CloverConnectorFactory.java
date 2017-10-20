package com.clover.remote.client;

/**
 * Created by connor on 10/19/17.
 */
public class CloverConnectorFactory {

  public static CloverConnector createCloverConnector(CloverDeviceConfiguration config) {

    return new CloverConnector(config);
  }
}
