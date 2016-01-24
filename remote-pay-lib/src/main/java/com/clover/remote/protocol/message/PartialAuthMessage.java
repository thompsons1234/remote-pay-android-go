package com.clover.remote.protocol.message;

public class PartialAuthMessage extends Message {
  public final long partialAuthAmount;

  public PartialAuthMessage(long partialAuthAmount) {
    super(Method.PARTIAL_AUTH);
    this.partialAuthAmount = partialAuthAmount;
  }
}
