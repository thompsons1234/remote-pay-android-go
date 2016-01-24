package com.clover.remote.protocol.message;

/**
 * Message that is sent to get the last
 * {TX_START,VOID_PAYMENT,REFUND_REQUEST,TIP_ADJUST}
 * message processed on the device.
 *
 */
public class LastMessageRequestMessage extends Message {
  public LastMessageRequestMessage() {
    super(Method.LAST_MSG_RESPONSE);
  }
}
