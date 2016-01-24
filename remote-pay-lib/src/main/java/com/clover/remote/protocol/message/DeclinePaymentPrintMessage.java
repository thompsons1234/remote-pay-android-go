package com.clover.remote.protocol.message;

import com.clover.sdk.v3.payments.Payment;

public class DeclinePaymentPrintMessage extends Message {
  public final Payment payment;
  public final String reason;

  public DeclinePaymentPrintMessage(Payment payment, String reason) {
    super(Method.PRINT_PAYMENT_DECLINE);
    this.payment = payment;
    this.reason = reason;
  }
}
