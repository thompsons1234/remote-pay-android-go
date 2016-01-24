package com.clover.remote.order;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import com.clover.remote.order.action.AddDiscountAction;
import com.clover.remote.order.action.AddLineItemAction;
import com.clover.remote.order.action.OrderActionResponse;
import com.clover.remote.order.action.RemoveDiscountAction;
import com.clover.remote.order.action.RemoveLineItemAction;

/**
 * This class is used by third-party modify order handler apps to request an order modification from the POS.
 */
public class ModifyOrders {
  public static final String METHOD_ADD_DISCOUNT = "addDiscount";
  public static final String METHOD_REMOVE_DISCOUNT = "removeDiscount";
  public static final String METHOD_ADD_LINE_ITEM = "addLineItem";
  public static final String METHOD_REMOVE_LINE_ITEM = "removeLineItem";

  public static final String EXTRA_ADD_DISCOUNT_ACTION = "addDiscountAction";
  public static final String EXTRA_REMOVE_DISCOUNT_ACTION = "removeDiscountAction";
  public static final String EXTRA_ADD_LINE_ITEM_ACTION = "addLineItemAction";
  public static final String EXTRA_REMOVE_LINE_ITEM_ACTION = "removeLineItemAction";
  public static final String EXTRA_ORDER_ACTION_RESPONSE = "orderActionResponse";

  private final Context context;
  private final Uri uri;

  public ModifyOrders(Context context) {
    this.context = context;
    this.uri = Uri.parse("content://com.clover.remote.terminal.modify_order");
  }

  public OrderActionResponse modify(AddDiscountAction action) {
    Bundle extras = new Bundle();
    extras.setClassLoader(getClass().getClassLoader());
    extras.putParcelable(EXTRA_ADD_DISCOUNT_ACTION, action);

    OrderActionResponse response = new OrderActionResponse();
    response.setAccepted(false);

    try {
      Bundle result = context.getContentResolver().call(uri, METHOD_ADD_DISCOUNT, null, extras);
      if (result != null) {
        result.setClassLoader(getClass().getClassLoader());
        OrderActionResponse orderActionResponse = result.getParcelable(EXTRA_ORDER_ACTION_RESPONSE);
        if (orderActionResponse != null) {
          response = orderActionResponse;
        }
      }
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }

    return response;
  }

  public OrderActionResponse modify(RemoveDiscountAction action) {
    Bundle extras = new Bundle();
    extras.setClassLoader(getClass().getClassLoader());
    extras.putParcelable(EXTRA_REMOVE_DISCOUNT_ACTION, action);

    OrderActionResponse response = new OrderActionResponse();
    response.setAccepted(false);

    try {
      Bundle result = context.getContentResolver().call(uri, METHOD_REMOVE_DISCOUNT, null, extras);
      if (result != null) {
        result.setClassLoader(getClass().getClassLoader());
        OrderActionResponse orderActionResponse = result.getParcelable(EXTRA_ORDER_ACTION_RESPONSE);
        if (orderActionResponse != null) {
          response = orderActionResponse;
        }
      }
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }

    return response;
  }

  public OrderActionResponse modify(AddLineItemAction action) {
    Bundle extras = new Bundle();
    extras.setClassLoader(getClass().getClassLoader());
    extras.putParcelable(EXTRA_ADD_LINE_ITEM_ACTION, action);

    OrderActionResponse response = new OrderActionResponse();
    response.setAccepted(false);

    try {
      Bundle result = context.getContentResolver().call(uri, METHOD_ADD_LINE_ITEM, null, extras);
      if (result != null) {
        result.setClassLoader(getClass().getClassLoader());
        OrderActionResponse orderActionResponse = result.getParcelable(EXTRA_ORDER_ACTION_RESPONSE);
        if (orderActionResponse != null) {
          response = orderActionResponse;
        }
      }
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }

    return response;
  }

  public OrderActionResponse modify(RemoveLineItemAction action) {
    Bundle extras = new Bundle();
    extras.setClassLoader(getClass().getClassLoader());
    extras.putParcelable(EXTRA_REMOVE_LINE_ITEM_ACTION, action);

    OrderActionResponse response = new OrderActionResponse();
    response.setAccepted(false);

    try {
      Bundle result = context.getContentResolver().call(uri, METHOD_REMOVE_LINE_ITEM, null, extras);
      if (result != null) {
        result.setClassLoader(getClass().getClassLoader());
        OrderActionResponse orderActionResponse = result.getParcelable(EXTRA_ORDER_ACTION_RESPONSE);
        if (orderActionResponse != null) {
          response = orderActionResponse;
        }
      }
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }

    return response;
  }
}
