package com.clover.remote.protocol.message;

import com.clover.common2.Signature2;
import com.clover.sdk.v3.payments.Payment;

public class VerifySignatureMessage extends Message {

  public final Payment payment;
  public final Signature2 signature;

  public VerifySignatureMessage(Payment payment, Signature2 signature) {
    super(Method.VERIFY_SIGNATURE);
    this.payment = payment;
    this.signature = signature;
  }
}
