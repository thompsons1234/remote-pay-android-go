package com.clover.remote.client.transport.usb.pos;

import android.app.Service;
import android.os.Binder;

public abstract class ServiceBinder<S extends Service> extends Binder {
  public abstract S getService();
}
