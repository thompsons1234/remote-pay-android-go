package com.clover.remote.protocol.message;

import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Payment;

/**
 * Message used to indicate that a payment should be voided.
 *
 */
public class VoidPaymentMessage extends Message {
  public final Payment payment;
  public final VoidReason voidReason;

  public VoidPaymentMessage(Payment payment, VoidReason voidReason) {
    super(Method.VOID_PAYMENT);
    this.payment = payment;
    this.voidReason = voidReason;
  }
}
