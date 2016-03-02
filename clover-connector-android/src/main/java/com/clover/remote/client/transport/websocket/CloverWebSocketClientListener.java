package com.clover.remote.client.transport.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * Created by blakewilliams on 2/26/16.
 */
public interface CloverWebSocketClientListener /*extends WebSocketClient*/{
  public void onOpen(WebSocketClient ws, ServerHandshake handshakedata);
  public void onNotResponding(WebSocketClient ws);
  public void onPingResponding(WebSocketClient ws);
  public void onClose(WebSocketClient ws, int code, String reason, boolean remote);
  public void onMessage(WebSocketClient ws, String message);

}
