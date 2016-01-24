package com.clover.remote.protocol.message;

import com.clover.remote.terminal.KeyPress;

public class KeyPressMessage extends Message {
  public final KeyPress keyPress;

  public KeyPressMessage(KeyPress keyPress) {
    super(Method.KEY_PRESS);
    this.keyPress = keyPress;
  }
}
