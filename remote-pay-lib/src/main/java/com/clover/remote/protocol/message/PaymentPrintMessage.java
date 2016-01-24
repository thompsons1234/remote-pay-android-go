package com.clover.remote.protocol.message;

import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.payments.Payment;

public class PaymentPrintMessage extends Message {
  public final Payment payment;
  public final Order order;

  public PaymentPrintMessage(Payment payment, Order order) {
    super(Method.PRINT_PAYMENT);
    this.payment = payment;
    this.order = order;
  }
}
