package com.clover.remote.protocol.message;

/**
 * Created by michaelhampton on 10/6/15.
 */
public class OpenCashDrawerMessage extends Message {
  public final String reason;

  protected OpenCashDrawerMessage(String reason) {
    super(Method.OPEN_CASH_DRAWER);
    this.reason = reason;
  }
}
