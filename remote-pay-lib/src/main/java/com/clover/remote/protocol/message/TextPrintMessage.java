package com.clover.remote.protocol.message;

import java.util.Arrays;
import java.util.List;

public class TextPrintMessage extends Message {
  public final List<String> textLines;

  public TextPrintMessage(List<String> textLines) {
    super(Method.PRINT_TEXT);
    this.textLines = textLines;
  }

  public TextPrintMessage(String... textLines) {
    this(Arrays.asList(textLines));
  }

}
