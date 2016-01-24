package com.clover.remote.protocol.message;

public class TipAdjustResponseMessage extends Message {
  public final String orderId;
  public final String paymentId;
  public final long amount;
  public final boolean success;

  public TipAdjustResponseMessage(String orderId, String paymentId, long amount, boolean success) {
    super(Method.TIP_ADJUST_RESPONSE);
    this.orderId = orderId;
    this.paymentId = paymentId;
    this.amount = amount;
    this.success = success;
  }
}
