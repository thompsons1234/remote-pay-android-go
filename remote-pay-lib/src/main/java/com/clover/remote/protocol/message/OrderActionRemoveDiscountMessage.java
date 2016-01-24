package com.clover.remote.protocol.message;

import com.clover.remote.order.action.RemoveDiscountAction;

public class OrderActionRemoveDiscountMessage extends Message {
  public final RemoveDiscountAction removeDiscountAction;

  public OrderActionRemoveDiscountMessage(final RemoveDiscountAction removeDiscountAction) {
    super(Method.ORDER_ACTION_REMOVE_DISCOUNT);
    this.removeDiscountAction = removeDiscountAction;
  }
}
