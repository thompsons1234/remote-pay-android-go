/*
 * Copyright (C) 2016 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.remote.client.transport;

import android.util.Log;

import java.nio.channels.NotYetConnectedException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class CloverTransport {

  public static final String DEVICE_CONNECTED = "com.clover.remotepay.DEVICE_CONNECTED";
  public static final String DEVICE_READY = "com.clover.remotepay.DEVICE_READY";
  public static final String DEVICE_DISCONNECTED = "com.clover.remotepay.DEVICE_DISCONNECTED";

  private final List<CloverTransportObserver> observers = new CopyOnWriteArrayList<>();

  /**
   * Should be called by subclasses (super.notifyDeviceConnected) when the device connects (but is not ready)
   * in order to forward to all observers
   */
  protected void notifyDeviceConnected() {
    for (CloverTransportObserver obs : observers) {
      try {
        obs.onDeviceConnected(this);
      } catch (Exception ex) {
        Log.e(getClass().getName(), "Error notifying observer", ex);
      }
    }
  }

  /**
   * Should be called by subclasses (super.notifyDeviceReady) when the device is ready to process messages
   * in order to forward to all observers
   */
  protected void notifyDeviceReady() {
    for (CloverTransportObserver obs : observers) {
      try {
        obs.onDeviceReady(this);
      } catch (Exception ex) {
        Log.e(getClass().getName(), "Error notifying observer", ex);
      }
    }
  }

  /**
   * Should be called by subclasses (super.notifyDeviceDisconnected) when the device disconnects
   * in order to forward to all observers
   */
  protected void notifyDeviceDisconnected() {
    for (CloverTransportObserver obs : observers) {
      try {
        obs.onDeviceDisconnected(this);
      } catch (Exception ex) {
        Log.e(getClass().getName(), "Error notifying observer", ex);
      }
    }
  }

  /**
   * Should be called by subclasses (super.onMessage) when a message is received
   * in order to forward to all observers
   * @param message message to forward
   */
  protected void onMessage(String message) {
    for (CloverTransportObserver obs : observers) {
      try {
        obs.onMessage(message);
      } catch (Exception ex) {
        Log.e(getClass().getName(), "Error processing message: " + message, ex);
      }
    }
  }

  public void subscribe(CloverTransportObserver observer) {
    observers.add(observer);
  }

  public void unsubscribe(CloverTransportObserver observer) {
    observers.remove(observer);
  }

  /**
   * Initializes the connection using the underlying transport
   */
  public abstract void initializeConnection();

  public void dispose() {
    observers.clear();
  }

  // Implement this to send raw message to the Mini

  /**
   * Sends the specified encoded message
   *
   * @param message encoded message to send
   * @return 0 if successful, -1 if failure
   * @throws NotYetConnectedException if the message is sent when the underlying transport is not connected
   */
  public abstract int sendMessage(String message) throws NotYetConnectedException;
}

