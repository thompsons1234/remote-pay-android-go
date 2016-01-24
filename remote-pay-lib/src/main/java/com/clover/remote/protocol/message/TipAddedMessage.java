package com.clover.remote.protocol.message;

public class TipAddedMessage extends Message {
  public final long tipAmount;

  public TipAddedMessage(long tipAmount) {
    super(Method.TIP_ADDED);
    this.tipAmount = tipAmount;
  }
}
