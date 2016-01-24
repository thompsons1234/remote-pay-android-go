package com.clover.remote.protocol.message;

public class DiscoveryRequestMessage extends Message {

  public final boolean supportsOrderModification;

  public DiscoveryRequestMessage(boolean supportsOrderModification) {
    super(Method.DISCOVERY_REQUEST);
    this.supportsOrderModification = supportsOrderModification;
  }
}
