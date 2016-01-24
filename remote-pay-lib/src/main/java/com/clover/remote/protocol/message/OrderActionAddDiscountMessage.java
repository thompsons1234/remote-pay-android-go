package com.clover.remote.protocol.message;

import com.clover.remote.order.action.AddDiscountAction;

public class OrderActionAddDiscountMessage extends Message {
  public final AddDiscountAction addDiscountAction;

  public OrderActionAddDiscountMessage(final AddDiscountAction addDiscountAction) {
    super(Method.ORDER_ACTION_ADD_DISCOUNT);
    this.addDiscountAction = addDiscountAction;
  }
}
