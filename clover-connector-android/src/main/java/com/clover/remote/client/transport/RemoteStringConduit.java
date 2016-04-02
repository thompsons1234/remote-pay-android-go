package com.clover.remote.client.transport;

import java.io.IOException;

public interface RemoteStringConduit {
  void sendString(String var1) throws IOException, InterruptedException;

  String receiveString() throws IOException, InterruptedException;
}
