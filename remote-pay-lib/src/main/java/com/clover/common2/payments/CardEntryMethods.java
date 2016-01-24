package com.clover.common2.payments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import com.clover.sdk.v1.Intents;

public class CardEntryMethods {
  private static final String TAG = CardEntryMethods.class.getSimpleName();

  /** Battery percent threshold required to enable NFC reader */
  public static final int BATTERY_PCT_NFC_READER_THRESHOLD = 20;

  public static final byte MAG_STRIPE = (byte) Intents.CARD_ENTRY_METHOD_MAG_STRIPE;
  public static final byte ICC_CONTACT = (byte)Intents.CARD_ENTRY_METHOD_ICC_CONTACT;
  public static final byte NFC_CONTACTLESS = (byte)Intents.CARD_ENTRY_METHOD_NFC_CONTACTLESS;
  public static final byte MANUAL = (byte)Intents.CARD_ENTRY_METHOD_MANUAL;

  public static final byte ALL = (ICC_CONTACT | MAG_STRIPE | NFC_CONTACTLESS | MANUAL);

  private static byte activeCardEntryMethods = 0x0;

  public static void setMagStripe(boolean enabled) {
    if (enabled) {
      activeCardEntryMethods |= MAG_STRIPE;
    } else {
      activeCardEntryMethods &= ~MAG_STRIPE;
    }
  }

  public static boolean isMagStripe() {
    return (activeCardEntryMethods & MAG_STRIPE) != 0;
  }

  public static void setIccContact(boolean enable) {
    if (enable) {
      activeCardEntryMethods |= ICC_CONTACT;
    } else if (isIccContact() && !enable) {
      activeCardEntryMethods &= ~ICC_CONTACT;
    }
  }

  public static boolean isIccContact() {
    return (activeCardEntryMethods & ICC_CONTACT) != 0;
  }

  public static void setNfcContactless(Context context, boolean enabled) {
    if (enabled && isNfcContactlessCapable(context)) {
      activeCardEntryMethods |= NFC_CONTACTLESS;
    } else {
      activeCardEntryMethods &= ~NFC_CONTACTLESS;
    }
  }

  public static boolean isNfcContactless() {
    return (activeCardEntryMethods & NFC_CONTACTLESS) != 0;
  }

  public static void setManual(boolean enabled) {
    if (enabled) {
      activeCardEntryMethods |= MANUAL;
    } else {
      activeCardEntryMethods &= ~MANUAL;
    }
  }

  public static boolean isManual() {
    return (activeCardEntryMethods & MANUAL) != 0;
  }

  public static void setActiveCardEntryMethods(Context context, byte methods) {
    if ((methods & NFC_CONTACTLESS) == NFC_CONTACTLESS) {
      if (!isNfcContactlessCapable(context)) {
        methods &= ~NFC_CONTACTLESS;
      }
    }

    activeCardEntryMethods = methods;
  }

  public static byte getActiveCardEntryMethods() {
    return activeCardEntryMethods;
  }

  private static boolean isNfcContactlessCapable(Context context) {
    boolean capable = true;

    Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
    int plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
    if (status != BatteryManager.BATTERY_STATUS_FULL && plugged != BatteryManager.BATTERY_PLUGGED_AC) {
      int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
      int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
      int batteryPct = (level * 100) / scale;
      if (batteryPct < CardEntryMethods.BATTERY_PCT_NFC_READER_THRESHOLD) {
        //ALog.w(CardEntryMethods.class, "Disabling NFC due to low battery: %s%", batteryPct);
        capable = false;
      }
    }

    return capable;
  }

}
