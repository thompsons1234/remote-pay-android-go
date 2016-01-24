package com.clover.remote.protocol.message;


public class TipAdjustMessage extends Message {
  public final String orderId;
  public final String paymentId;
  public final long tipAmount;

  public TipAdjustMessage(String orderId, String paymentID, long tipAmount) {
    super(Method.TIP_ADJUST);
    this.orderId = orderId;
    this.paymentId = paymentID;
    this.tipAmount = tipAmount;
  }
}
