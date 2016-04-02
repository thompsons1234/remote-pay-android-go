package com.clover.remote.client.transport.usb.pos;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.clover.common.analytics.ALog;
import com.clover.remote.message.DiscoveryRequestMessage;
import com.clover.settings.CloverSettings;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * This service runs on the POS device.
 */
public abstract class PosRemoteProtocolService extends Service {

  private final String TAG = getClass().getSimpleName();

  private final Handler mMainHandler = new Handler(Looper.getMainLooper());

//  private final Set<RemoteTerminalEventListener> mListeners
//      = Collections.newSetFromMap(new WeakHashMap<RemoteTerminalEventListener, Boolean>());

  private RemoteTerminalStatus mRemoteTerminalStatus = RemoteTerminalStatus.TERMINAL_DISCONNECTED;

//  public void addListener(RemoteTerminalEventListener listener) {
//    mListeners.add(listener);
//
//    // Invoke callback immediately
//    listener.onTerminalStatusChanged(mRemoteTerminalStatus);
//
//    // Request latest status to send to listener
//    sendMessage(new DiscoveryRequestMessage(isOrderModificationSupported()));
//  }
//
//  public void removeListener(RemoteTerminalEventListener listener) {
//    mListeners.remove(listener);
//  }

  private static boolean sConduitConnected = false;

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public void onDestroy() {
    sConduitConnected = false;
    super.onDestroy();
  }

  public static boolean isConduitConnected() {
    return sConduitConnected;
  }

  public void onConduitConnected() {
    sConduitConnected = true;
  }

  /*@Override
  public abstract onMessageReceived(final RemoteMessage message) {
    if (mRemoteTerminalStatus != RemoteTerminalStatus.TERMINAL_CONNECTED_MERCHANT_MISMATCH) {
      if (Method.DISCOVERY_RESPONSE.isMatch(message)) {
        String merchantId = CloverSettings.Merchant.getString(getContext().getContentResolver(),
            CloverSettings.Merchant.MERCHANT_ID);
        DiscoveryResponseMessage discoveryResponseMsg
            = (DiscoveryResponseMessage) Message.fromJsonString(message.payload);

        RemoteTerminalStatus remoteTerminalStatus;
        if (merchantId.equals(discoveryResponseMsg.merchantId)) {
          remoteTerminalStatus = discoveryResponseMsg.ready
              ? RemoteTerminalStatus.TERMINAL_CONNECTED_READY
              : RemoteTerminalStatus.TERMINAL_CONNECTED_NOT_READY;
        } else {
          remoteTerminalStatus = RemoteTerminalStatus.TERMINAL_CONNECTED_MERCHANT_MISMATCH;
        }

        handleTerminalStatusChanged(remoteTerminalStatus);
        return;
      }
    }

    switch (mRemoteTerminalStatus) {
      case TERMINAL_CONNECTED_READY:
        invokeListeners(new Invokable<RemoteTerminalEventListener>() {
          @Override
          public void invoke(RemoteTerminalEventListener listener) {
            listener.onMessageReceived(message);
          }
        });
        break;
      case TERMINAL_CONNECTED_MERCHANT_MISMATCH:
        ALog.d(this, "Message ignored");
        break;
    }
  }*/

//  @Override
  public void onConduitDisconnected() {
    sConduitConnected = false;
//    handleTerminalStatusChanged(RemoteTerminalStatus.TERMINAL_DISCONNECTED);
  }

//  @Override
  public void onMessageTransferError(final Exception e) {
    /*invokeListeners(new Invokable<RemoteTerminalEventListener>() {
      @Override
      public void invoke(RemoteTerminalEventListener listener) {
        listener.onMessageTransferError(e);
      }
    });*/
  }

  private void handleTerminalStatusChanged(final RemoteTerminalStatus status) {
    if (mRemoteTerminalStatus == status) {
      return;
    }

    mRemoteTerminalStatus = status;

    /*invokeListeners(new Invokable<RemoteTerminalEventListener>() {
      @Override
      public void invoke(RemoteTerminalEventListener listener) {
        listener.onTerminalStatusChanged(status);
      }
    });*/
  }

  public RemoteTerminalStatus getRemoteTerminalStatus() {
    return mRemoteTerminalStatus;
  }

  private interface Invokable<P> {
    void invoke(P p);
  }

  /*private void invokeListeners(final Invokable<RemoteTerminalEventListener> i) {
    for (final RemoteTerminalEventListener listener : mListeners) {
      mMainHandler.post(new Runnable() {
        @Override
        public void run() {
          i.invoke(listener);
        }
      });
    }
  }*/

  private Context getContext() {
    return this;
  }

  /**
   * Returns true if this POS supports order modifications.
   */
  protected abstract boolean isOrderModificationSupported();

}
