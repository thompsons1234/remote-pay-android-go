package com.clover.remote.protocol.message;

import com.clover.remote.terminal.TxState;

public class TxStateMessage extends Message {
  public final TxState txState;

  public TxStateMessage(TxState txState) {
    super(Method.TX_STATE);
    this.txState = txState;
  }
}
