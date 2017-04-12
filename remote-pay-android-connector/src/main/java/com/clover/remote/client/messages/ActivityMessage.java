package com.clover.remote.client.messages;

import com.clover.remote.message.Method;

import java.util.Map;

public class ActivityMessage {
  public final String actionId;
  public final String payload;

  public ActivityMessage(String actionId, String payload) {
    this.actionId = actionId;
    this.payload = payload;
  }
}
