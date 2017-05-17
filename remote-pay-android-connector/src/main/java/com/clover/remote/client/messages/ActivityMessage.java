package com.clover.remote.client.messages;

public class ActivityMessage {
  public final String action;
  public final String payload;

  public ActivityMessage(String action, String payload) {
    this.action = action;
    this.payload = payload;
  }
}
