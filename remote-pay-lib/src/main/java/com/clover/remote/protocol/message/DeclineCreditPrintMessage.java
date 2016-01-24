package com.clover.remote.protocol.message;

import com.clover.sdk.v3.payments.Credit;

public class DeclineCreditPrintMessage extends Message {
  public final Credit credit;
  public final String reason;

  public DeclineCreditPrintMessage(Credit credit, String reason) {
    super(Method.PRINT_CREDIT);
    this.credit = credit;
    this.reason = reason;
  }
}
