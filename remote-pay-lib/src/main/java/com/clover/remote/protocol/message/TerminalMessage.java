package com.clover.remote.protocol.message;

public class TerminalMessage extends Message {
  public final String text;

  public TerminalMessage(String text) {
    super(Method.TERMINAL_MESSAGE);
    this.text = text;
  }
}
