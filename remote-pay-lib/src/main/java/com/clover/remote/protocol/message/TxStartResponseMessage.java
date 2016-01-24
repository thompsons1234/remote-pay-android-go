package com.clover.remote.protocol.message;

import com.clover.remote.terminal.RemoteControl;
import com.clover.sdk.v3.order.Order;

public class TxStartResponseMessage extends Message {
  public final Order order;
  /**
   * @deprecated  Use {@link #result}.
   */
  @Deprecated
  public final boolean success;
  public final RemoteControl.TxStartResponseResult result;

  public TxStartResponseMessage(Order order, boolean success, RemoteControl.TxStartResponseResult result) {
    super(Method.TX_START_RESPONSE);
    this.order = order;
    this.success = success;
    this.result = result != null ? result : success ? RemoteControl.TxStartResponseResult.SUCCESS : RemoteControl.TxStartResponseResult.FAIL;
  }
}
