package com.clover.remote.protocol.message;

public class DiscoveryResponseMessage extends Message {
  public final String merchantId;
  public final String name;
  public final String serial;
  public final String model;
  public final boolean ready;

  public DiscoveryResponseMessage(String merchantId, String name, String serial, String model, boolean ready) {
    super(Method.DISCOVERY_RESPONSE);
    this.merchantId = merchantId;
    this.name = name;
    this.serial = serial;
    this.model = model;
    this.ready = ready;
  }
}
