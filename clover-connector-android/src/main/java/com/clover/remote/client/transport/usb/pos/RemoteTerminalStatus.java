package com.clover.remote.client.transport.usb.pos;


public enum RemoteTerminalStatus {
  /** Remote terminal not connected */
  TERMINAL_DISCONNECTED,
  /** Remote terminal is connected, but not ready to perform transactions at the moment */
  TERMINAL_CONNECTED_NOT_READY,
  /** Remote terminal is connected and ready to perform transactions */
  TERMINAL_CONNECTED_READY,
  /** Remote terminal is connected, but merchant mismatch, cannot perform transactions */
  TERMINAL_CONNECTED_MERCHANT_MISMATCH,
}
