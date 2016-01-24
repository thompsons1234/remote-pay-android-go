package com.clover.remote.protocol.message;

import com.clover.remote.order.action.OrderActionResponse;

public class OrderActionResponseMessage extends Message {
  public final OrderActionResponse orderActionResponse;

  public OrderActionResponseMessage(OrderActionResponse orderActionResponse) {
    super(Method.ORDER_ACTION_RESPONSE);
    this.orderActionResponse = orderActionResponse;
  }
}
