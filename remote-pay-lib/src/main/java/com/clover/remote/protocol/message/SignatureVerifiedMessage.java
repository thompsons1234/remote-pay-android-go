package com.clover.remote.protocol.message;

import com.clover.sdk.v3.payments.Payment;

public class SignatureVerifiedMessage extends Message {

  public final Payment payment;
  public final boolean verified;

  public SignatureVerifiedMessage(Payment payment, boolean verified) {
    super(Method.SIGNATURE_VERIFIED);
    this.payment = payment;
    this.verified = verified;
  }
}
