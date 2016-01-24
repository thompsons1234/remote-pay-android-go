package com.clover.remote.protocol.message;


public class ShowPaymentReceiptOptionsMessage extends Message {
  public final String orderId;
  public final String paymentId;

  public ShowPaymentReceiptOptionsMessage(String orderId, String paymentID) {
    super(Method.SHOW_PAYMENT_RECEIPT_OPTIONS);
    this.orderId = orderId;
    this.paymentId = paymentID;
  }
}
