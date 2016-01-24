package com.clover.remote.protocol.message;

import com.clover.sdk.v3.payments.Credit;

public class CreditPrintMessage extends Message {
  public final Credit credit;

  public CreditPrintMessage(Credit credit) {
    super(Method.PRINT_CREDIT);
    this.credit = credit;
  }
}
