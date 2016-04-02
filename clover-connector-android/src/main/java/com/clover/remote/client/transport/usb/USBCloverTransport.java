package com.clover.remote.client.transport.usb;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.clover.common.util.CloverUsbManager;
import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.CloverTransportObserver;
import com.clover.remote.client.transport.usb.pos.PosRemoteProtocolService;
import com.clover.remote.client.transport.usb.pos.PosUsbRemoteProtocolService;
import com.clover.remote.client.transport.usb.pos.RemoteTerminalStatus;
import com.clover.remote.client.transport.usb.pos.RemoteUsbManager;
import com.clover.remote.client.transport.usb.pos.UsbAccessorySetupUsbManager;

import java.nio.channels.NotYetConnectedException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by blakewilliams on 3/29/16.
 */
public class USBCloverTransport extends CloverTransport {

  public static final String ACTION_USB_PERMISSION = "com.clover.USB_PERMISSION";
  public static final String TAG = USBCloverTransport.class.getSimpleName();

  Context context;
  private UsbManager mUsbManager;
  private PosUsbRemoteProtocolService usbService;
  private ServiceConnection svcConnection;

  private BroadcastReceiver connectionBroadcastReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

        try {
          RemoteTerminalStatus status = RemoteTerminalStatus.valueOf(action);
          switch (status) {
          case TERMINAL_DISCONNECTED: {
            onDeviceDisconnected();
            break;
          }
          case TERMINAL_CONNECTED_NOT_READY: {
            onDeviceConnected();
            break;
          }
          case TERMINAL_CONNECTED_READY: {
            onDeviceReady();
            break;
          }
          }
        } catch (Exception e) {
          Log.e(TAG, "Couldn't parse intent: " + intent.getAction());
        }
      }
  };
  private BroadcastReceiver messageBroadcastReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      if(action.equals(PosUsbRemoteProtocolService.ACTION_USB_RECEIVE_MESSAGE)) {
        String msg = intent.getStringExtra(PosUsbRemoteProtocolService.EXTRA_MESSAGE);
        Log.d(TAG, String.format("Got message in Transport: %s", msg));
        onMessage(msg);
      }
    }
  };

  public USBCloverTransport(Context ctx) {
    context = ctx;
    context.registerReceiver(connectionBroadcastReceiver, getConnectionIntentFilter());
    context.registerReceiver(messageBroadcastReceiver, getMessageIntentFilter());

    findAndOpenDevice();

    Intent sendIntent = new Intent(PosUsbRemoteProtocolService.ACTION_USB_REQUEST_MESSAGE);
    context.sendBroadcast(sendIntent);
  }

  protected IntentFilter getConnectionIntentFilter() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(RemoteTerminalStatus.TERMINAL_DISCONNECTED.name());
    filter.addAction(RemoteTerminalStatus.TERMINAL_CONNECTED_READY.name());
    filter.addAction(RemoteTerminalStatus.TERMINAL_CONNECTED_NOT_READY.name());
    return filter;
  }
  protected IntentFilter getMessageIntentFilter() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(PosUsbRemoteProtocolService.ACTION_USB_RECEIVE_MESSAGE);
    return filter;
  }

  private void findAndOpenDevice() {
    Intent posUsbServiceIntent = new Intent()
        .setClass(context, PosUsbRemoteProtocolService.class)
        .setAction(PosUsbRemoteProtocolService.ACTION_USB_CONNECT);
    context.startService(posUsbServiceIntent);

    if (true) {
      return;
    }
    PosUsbRemoteProtocolService usbService = new PosUsbRemoteProtocolService();
//    Intent intent = new Intent(context, PosUsbRemoteProtocolService.class).setAction(PosUsbRemoteProtocolService.ACTION_USB_SETUP);
//    usbService.onStartCommand(intent, 0, 1);
    try {
      final UsbAccessorySetupUsbManager setupUsbManager = new UsbAccessorySetupUsbManager(context);

      final UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
      UsbDevice device = setupUsbManager.findDevice(usbManager, UsbAccessorySetupUsbManager.VENDOR_PRODUCT_IDS);

      Runnable runnable = new Runnable() {
        @Override public void run() {
          try {
            setupUsbManager.startAccessoryMode();

//            device = setupUsbManager.findDevice(usbManager, RemoteUsbManager.VENDOR_PRODUCT_IDS);
//            if(device != null) {
//
//            }
          } catch (UsbCloverManager.UsbConnectException e) {
            e.printStackTrace();
          }
        }
      };

      if(device != null) {
        if(!usbManager.hasPermission(device)) {
          requestPermission(usbManager, device, runnable);
        } else {
          runnable.run();
        }
      }

//      UsbAccessorySetupUsbManager.isUsbDeviceAttached(context);
//      setupUsbManager.startAccessoryMode();
//      return true;

//      RemoteUsbManager usbManager = new RemoteUsbManager(context);
//      usbManager.open();
    } catch (Throwable e) {
      Log.e(getClass().getSimpleName(), "Error flipping device...", e);
    }

    //Intent connectIntent = new Intent(context, PosUsbRemoteProtocolService.class).setAction(PosUsbRemoteProtocolService.ACTION_USB_CONNECT);
    //usbService.onStartCommand(connectIntent, 0, 2);

    if(true) {
      return;
    }
//    String action = intent.getAction();
//    Bundle extras = intent.getExtras();

    final Intent serviceIntent = new Intent().setClass(context, PosUsbRemoteProtocolService.class);


    mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
    while(deviceIterator.hasNext()){
      UsbDevice device = deviceIterator.next();


      if (CloverUsbManager.isMatch(device, UsbAccessorySetupUsbManager.VENDOR_PRODUCT_IDS)) {
        //if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
        Runnable runnable = new Runnable() {
          @Override public void run() {
            serviceIntent.setAction(PosUsbRemoteProtocolService.ACTION_USB_SETUP);
            context.startService(serviceIntent);
          }
        };

        if(mUsbManager.hasPermission(device)) {
          runnable.run();
        } else {
          requestPermission(mUsbManager, device, runnable);
        }
        return;
        //}
      }

      if (CloverUsbManager.isMatch(device, RemoteUsbManager.VENDOR_PRODUCT_IDS)) {
        Runnable runnable = new Runnable() {
          @Override public void run() {
            serviceIntent.setAction(PosUsbRemoteProtocolService.ACTION_USB_CONNECT);
            context.startService(serviceIntent);
//            bindToService();
            //
          }
        };

        if(mUsbManager.hasPermission(device)) {
          runnable.run();
        } else {
          requestPermission(mUsbManager, device, runnable);
        }

          return;
      }
    }


  }


  private void requestPermission(UsbManager mUsbManager, UsbDevice device, final Runnable runnable) {
    final BroadcastReceiver usbBroadcastReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        synchronized (this) {
          context.unregisterReceiver(this);
          UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            if(device != null){
              runnable.run();
            }
          }
          else {
            Log.d(TAG, "permission denied for device " + device);
          }

        }
      }
    };
    PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    context.registerReceiver(usbBroadcastReceiver, filter);
    mUsbManager.requestPermission(device, permissionIntent);
  }


  @Override public void dispose() {
    context.unregisterReceiver(connectionBroadcastReceiver);
    context.unregisterReceiver(messageBroadcastReceiver);
    /*if(svcConnection != null) {
      context.unbindService(svcConnection);
    }*/
  }

  @Override public synchronized int sendMessage(String message) throws NotYetConnectedException {
    Intent sendIntent = new Intent(PosUsbRemoteProtocolService.ACTION_USB_SEND_MESSAGE);
    sendIntent.putExtra(PosUsbRemoteProtocolService.EXTRA_MESSAGE, message);
    context.sendBroadcast(sendIntent);

    /*if(usbService == null) {
      bindToService();
    }
    if(usbService != null) {
      usbService.sendMessage(message);
    }*/
    return 0;
  }

  /*private void bindToService() {
    if(usbService == null) {
      final Intent serviceIntent = new Intent().setClass(context, PosUsbRemoteProtocolService.class);
      svcConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
          usbService = (PosUsbRemoteProtocolService) ((PosUsbRemoteProtocolService.PosUsbClientServiceBinder)service).getService();
          if(usbService != null) {
            usbService.addListener(USBCloverTransport.this);
          }
        }

        @Override public void onServiceDisconnected(ComponentName name) {
          usbService.removeListener(USBCloverTransport.this);
          usbService = null;
        }
      };
      if(context.bindService(serviceIntent, svcConnection, Context.BIND_AUTO_CREATE)) {
        Toast.makeText(context, "bound!", Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(context, "not bound!", Toast.LENGTH_SHORT).show();
      }
    }
  }*/

  @Override
  public void onMessage(String message) {
    for(CloverTransportObserver cto : observers) {
      cto.onMessage(message);
    }
  }

  public void onDeviceReady() {
//    bindToService();
    notifyDeviceReady();
  }

  public void onDeviceConnected() {
    notifyDeviceConnected();
  }

  public void onDeviceDisconnected() {
    notifyDeviceDisconnected();
  }

  private boolean isMyServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }
}
