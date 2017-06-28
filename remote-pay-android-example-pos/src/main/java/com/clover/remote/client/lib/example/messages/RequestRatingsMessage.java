package com.clover.remote.client.lib.example.messages;

/**
 * Created by glennbedwell on 5/4/17.
 */
public class RequestRatingsMessage extends PayloadMessage {
  public RequestRatingsMessage() {
    super(null, MessageType.REQUEST_RATINGS);
  }
}
