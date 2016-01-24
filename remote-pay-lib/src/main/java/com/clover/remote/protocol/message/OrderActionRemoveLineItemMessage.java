package com.clover.remote.protocol.message;

import com.clover.remote.order.action.RemoveLineItemAction;

public class OrderActionRemoveLineItemMessage extends Message {
  public final RemoveLineItemAction removeLineItemAction;

  public OrderActionRemoveLineItemMessage(final RemoveLineItemAction removeLineItemAction) {
    super(Method.ORDER_ACTION_REMOVE_LINE_ITEM);
    this.removeLineItemAction = removeLineItemAction;
  }
}
