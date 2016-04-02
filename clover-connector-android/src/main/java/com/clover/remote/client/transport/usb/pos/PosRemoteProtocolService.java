package com.clover.remote.client.transport.usb.pos;

import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * This service runs on the POS device.
 */
public abstract class PosRemoteProtocolService extends Service {

  private final String TAG = getClass().getSimpleName();

  private final Handler mMainHandler = new Handler(Looper.getMainLooper());

//  private final Set<RemoteTerminalEventListener> mListeners
//      = Collections.newSetFromMap(new WeakHashMap<RemoteTerminalEventListener, Boolean>());

  private RemoteTerminalStatus mRemoteTerminalStatus = RemoteTerminalStatus.TERMINAL_DISCONNECTED;

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

  }

  public RemoteTerminalStatus getRemoteTerminalStatus() {
    return mRemoteTerminalStatus;
  }

  private interface Invokable<P> {
    void invoke(P p);
  }

  private Context getContext() {
    return this;
  }

  /**
   * Returns true if this POS supports order modifications.
   */
  protected abstract boolean isOrderModificationSupported();

}
