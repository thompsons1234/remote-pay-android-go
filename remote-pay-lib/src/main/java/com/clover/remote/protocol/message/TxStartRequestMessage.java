package com.clover.remote.protocol.message;

import com.clover.common2.payments.PayIntent;
import com.clover.sdk.v3.order.Order;

public class TxStartRequestMessage extends Message {
  public final PayIntent payIntent;
  public final Order order;

  public TxStartRequestMessage(PayIntent payIntent, Order order) {
    super(Method.TX_START);
    this.payIntent = payIntent;
    this.order = order;
  }
}
