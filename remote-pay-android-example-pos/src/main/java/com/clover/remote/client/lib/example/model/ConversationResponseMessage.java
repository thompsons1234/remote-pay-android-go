package com.clover.remote.client.lib.example.model;

/**
 * Created by glennbedwell on 5/4/17.
 */
public class ConversationResponseMessage extends PayloadMessage{
  public final String message;
  public ConversationResponseMessage(String message) {
    super("ConversationResponseMessage", MessageType.CONVERSATION_RESPONSE);
    this.message = message;
  }
}