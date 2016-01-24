package com.clover.remote.protocol.message;

import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Payment;

public class PaymentVoidedMessage extends Message {
  public final Payment payment;
  public final VoidReason voidReason;

  public PaymentVoidedMessage(Payment payment, VoidReason voidReason) {
    super(Method.PAYMENT_VOIDED);
    this.payment = payment;
    this.voidReason = voidReason;
  }
}
