package com.clover.remote.protocol.message;

import com.clover.remote.order.action.AddLineItemAction;

public class OrderActionAddLineItemMessage extends Message {
  public final AddLineItemAction addLineItemAction;

  public OrderActionAddLineItemMessage(final AddLineItemAction addLineItemAction) {
    super(Method.ORDER_ACTION_ADD_LINE_ITEM);
    this.addLineItemAction = addLineItemAction;
  }
}
