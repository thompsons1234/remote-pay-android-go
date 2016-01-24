package com.clover.remote.protocol.message;

import com.clover.remote.terminal.InputOption;
import com.clover.remote.terminal.UiState;

public class UiStateMessage extends Message {
  public final UiState uiState;
  public final String uiText;
  public final UiState.UiDirection uiDirection;
  public final InputOption[] inputOptions;

  public UiStateMessage(UiState uiState, String uiText, UiState.UiDirection uiDirection, InputOption[] inputOptions) {
    super(Method.UI_STATE);
    this.uiState = uiState;
    this.uiText = uiText;
    this.uiDirection = uiDirection;
    this.inputOptions = inputOptions;
  }
}
