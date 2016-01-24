package com.clover.remote.protocol.message;

import com.clover.remote.terminal.ErrorCode;
import com.clover.remote.terminal.TxState;
import com.clover.sdk.v3.payments.Refund;

public class RefundResponseMessage extends Message {
  public final String orderId;
  public final String paymentId;
  public final Refund refund;
  /**
   * Null if not an error.
   */
  public final ErrorCode reason;
  /**
   * An message internationalized to the merchant, or null.
   */
  public final String message;
  public final TxState code;

  public RefundResponseMessage(String orderId, String paymentId, Refund refund,
                               ErrorCode reason, String message, TxState code) {
    super(Method.REFUND_RESPONSE);
    this.orderId = orderId;
    this.paymentId = paymentId;
    this.refund = refund;
    this.reason = reason;
    this.message = message;
    this.code = code;
  }
}
