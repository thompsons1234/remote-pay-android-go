package com.clover.remote.protocol.message;

import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.payments.Refund;
import com.clover.sdk.v3.payments.Payment;

public class RefundPaymentPrintMessage extends Message {
  public final Payment payment;
  public final Refund refund;
  public final Order order;

  public RefundPaymentPrintMessage(Payment payment, Refund refund, Order order) {
    super(Method.REFUND_PRINT_PAYMENT);
    this.payment = payment;
    this.refund = refund;
    this.order = order;
  }
}
