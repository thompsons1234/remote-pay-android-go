package com.clover.remote.protocol.message;


/**
 * Message that is returned to get the last
 * {TX_START,VOID_PAYMENT,REFUND_REQUEST,TIP_ADJUST}
 * message processed on the device.
 *
 * The message will include the request and the response of the message.
 *
 */
public class LastMessageResponseMessage extends Message {
  public final Message request;
  public final Message response;

  public LastMessageResponseMessage(Message request, Message response) {
    super(Method.LAST_MSG_RESPONSE);
    this.response = response;
    this.request = request;
  }
}
