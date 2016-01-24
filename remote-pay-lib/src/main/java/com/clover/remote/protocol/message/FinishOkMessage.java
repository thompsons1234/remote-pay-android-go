package com.clover.remote.protocol.message;

import com.clover.common2.Signature2;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Refund;

public class FinishOkMessage extends Message {
  public final Payment payment;
  public final Credit credit;
  public final Refund refund;
  public final Signature2 signature;

  public FinishOkMessage(Payment payment) {
    this(payment, null, null, null);
  }

  public FinishOkMessage(Refund refund) {
    this(null, null, refund, null);
  }

  public FinishOkMessage(Payment payment, Signature2 signature) {
    this(payment, null, null, signature);
  }

  public FinishOkMessage(Credit credit) {
    this(null, credit, null, null);
  }

  public FinishOkMessage(Payment payment, Credit credit, Refund refund, Signature2 signature) {
    super(Method.FINISH_OK);
    this.payment = payment;
    this.credit = credit;
    this.signature = signature;
    this.refund = refund;
  }

}
