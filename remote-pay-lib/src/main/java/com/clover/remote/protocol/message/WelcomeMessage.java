package com.clover.remote.protocol.message;

public class WelcomeMessage extends Message {

  public WelcomeMessage() {
    super(Method.SHOW_WELCOME_SCREEN);
  }
}
