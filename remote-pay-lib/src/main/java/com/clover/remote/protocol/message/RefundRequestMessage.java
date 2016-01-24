package com.clover.remote.protocol.message;


public class RefundRequestMessage extends Message {
  public final String orderId;
  public final String paymentId;
  public final long amount;

  public RefundRequestMessage(String orderId, String paymentID, long amount) {
    super(Method.REFUND_REQUEST);
    this.orderId = orderId;
    this.paymentId = paymentID;
    this.amount = amount;
  }
}
