package com.clover.remote.protocol.message;

import com.clover.remote.order.DisplayOrder;
import com.clover.remote.order.operation.DiscountsAddedOperation;
import com.clover.remote.order.operation.DiscountsDeletedOperation;
import com.clover.remote.order.operation.LineItemsAddedOperation;
import com.clover.remote.order.operation.LineItemsDeletedOperation;
import com.clover.remote.order.operation.OrderDeletedOperation;

public class OrderUpdateMessage extends Message {

  public final DisplayOrder order;
  public final LineItemsAddedOperation lineItemsAddedOperation;
  public final LineItemsDeletedOperation lineItemsDeletedOperation;
  public final DiscountsAddedOperation discountsAddedOperation;
  public final DiscountsDeletedOperation discountsDeletedOperation;
  public final OrderDeletedOperation orderDeletedOperation;

  private OrderUpdateMessage(
      DisplayOrder order,
      LineItemsAddedOperation lineItemsAddedOperation,
      LineItemsDeletedOperation lineItemsDeletedOperation,
      DiscountsAddedOperation discountsAddedOperation,
      DiscountsDeletedOperation discountsDeletedOperation,
      OrderDeletedOperation orderDeletedOperation) {
    super(Method.SHOW_ORDER_SCREEN);

    this.order = order;
    this.lineItemsAddedOperation = lineItemsAddedOperation;
    this.lineItemsDeletedOperation = lineItemsDeletedOperation;
    this.discountsAddedOperation = discountsAddedOperation;
    this.discountsDeletedOperation = discountsDeletedOperation;
    this.orderDeletedOperation = orderDeletedOperation;
  }

  public OrderUpdateMessage(DisplayOrder order) {
    this(order, null, null, null, null, null);
  }

  public OrderUpdateMessage(DisplayOrder order, LineItemsAddedOperation lineItemsAddedOperation) {
    this(order, lineItemsAddedOperation, null, null, null, null);
  }

  public OrderUpdateMessage(DisplayOrder order, LineItemsDeletedOperation lineItemsDeletedOperation) {
    this(order, null, lineItemsDeletedOperation, null, null, null);
  }

  public OrderUpdateMessage(DisplayOrder order, DiscountsAddedOperation discountsAddedOperation) {
    this(order, null, null, discountsAddedOperation, null, null);
  }

  public OrderUpdateMessage(DisplayOrder order, DiscountsDeletedOperation discountsDeletedOperation) {
    this(order, null, null, null, discountsDeletedOperation, null);
  }

  public OrderUpdateMessage(DisplayOrder order, OrderDeletedOperation orderDeletedOperation) {
    this(order, null, null, null, null, orderDeletedOperation);
  }
}
