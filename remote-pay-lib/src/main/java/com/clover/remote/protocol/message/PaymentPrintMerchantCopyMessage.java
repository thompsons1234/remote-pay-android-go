package com.clover.remote.protocol.message;

import com.clover.sdk.v3.payments.Payment;

public class PaymentPrintMerchantCopyMessage extends Message {
  public final Payment payment;

  public PaymentPrintMerchantCopyMessage(Payment payment) {
    super(Method.PRINT_PAYMENT_MERCHANT_COPY);
    this.payment = payment;
  }
}
