package com.clover.remote.client.messages;


public class RetrieveDeviceStatusRequest {

  boolean sendLastMessage;

  public RetrieveDeviceStatusRequest(boolean sendLastMessage) {
    this.sendLastMessage = sendLastMessage;
  }

  public boolean isSendLastMessage() {
    return sendLastMessage;
  }

  public void setSendLastMessage(boolean sendLastMessage) {
    this.sendLastMessage = sendLastMessage;
  }
}
