package com.clover.remote.terminal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runs on the terminal, this defines the interface between the kiosk app and protocol app.
 */
public abstract class RemoteControl extends BroadcastReceiver {

  // sent by client, received by service
  protected static final String ACTION_V1_TX_STATE = "com.clover.remote.terminal.remotecontrol.action.V1_TX_STATE";
  protected static final String ACTION_V1_UI_STATE = "com.clover.remote.terminal.remotecontrol.action.V1_UI_STATE";
  protected static final String ACTION_V1_FINISH_OK = "com.clover.remote.terminal.remotecontrol.action.V1_FINISH_OK";
  protected static final String ACTION_V1_FINISH_CANCEL = "com.clover.remote.terminal.remotecontrol.action.V1_FINISH_CANCEL";
  protected static final String ACTION_V1_ADD_TIP = "com.clover.remote.terminal.remotecontrol.action.V1_ADD_TIP";
  protected static final String ACTION_V1_CASHBACK_SELECTED = "com.clover.remote.terminal.remotecontrol.action.V1_CASHBACK_SELECTED";
  protected static final String ACTION_V1_PARTIAL_AUTH = "com.clover.remote.terminal.remotecontrol.action.V1_PARTIAL_AUTH";
  protected static final String ACTION_V1_VERIFY_SIGNATURE = "com.clover.remote.terminal.remotecontrol.action.V1_VERIFY_SIGNATURE";
  protected static final String ACTION_V1_PAYMENT_VOIDED = "com.clover.remote.terminal.remotecontrol.action.V1_PAYMENT_VOIDED";
  protected static final String ACTION_V1_PRINT_PAYMENT = "com.clover.remote.terminal.remotecontrol.action.V1_PRINT_PAYMENT";
  protected static final String ACTION_V1_PRINT_CREDIT = "com.clover.remote.terminal.remotecontrol.action.V1_PRINT_CREDIT";
  protected static final String ACTION_V1_PRINT_REFUND = "com.clover.remote.terminal.remotecontrol.action.V1_PRINT_REFUND";

  protected static final String ACTION_V1_PRINT_PAYMENT_DECLINE = "com.clover.remote.terminal.remotecontrol.action.V1_PRINT_PAYMENT_DECLINE";
  protected static final String ACTION_V1_PRINT_CREDIT_DECLINE = "com.clover.remote.terminal.remotecontrol.action.V1_PRINT_CREDIT_DECLINE";
  protected static final String ACTION_V1_PRINT_PAYMENT_MERCHANT_COPY = "com.clover.remote.terminal.remotecontrol.action.V1_PRINT_PAYMENT_MERCHANT_COPY";
  protected static final String ACTION_V1_MODIFY_ORDER = "com.clover.remote.terminal.remotecontrol.action.V1_MODIFY_ORDER";
  protected static final String ACTION_V1_TX_START_RESPONSE = "com.clover.remote.terminal.remotecontrol.action.V1_TX_START_RESPONSE";
  protected static final String ACTION_V1_REFUND_RESPONSE = "com.clover.remote.terminal.remotecontrol.action.V1_REFUND_RESPONSE";
  protected static final String ACTION_V1_TIP_ADJUST_RESPONSE = "com.clover.remote.terminal.remotecontrol.action.V1_TIP_ADJUST_RESPONSE";

  /**
   * A notification that the kiosk is prepared to start processing messages
   */
  public static final String ACTION_V1_KIOSK_READY = "com.clover.remote.terminal.extra.ACTION_KIOSK_READY";

  // sent by client, received by service

  // sent by service, received by client
  protected static final String ACTION_V1_SHOW_WELCOME = "com.clover.remote.terminal.remotecontrol.action.V1_SHOW_WELCOME";
  protected static final String ACTION_V1_SHOW_ORDER = "com.clover.remote.terminal.remotecontrol.action.V1_SHOW_ORDER";
  protected static final String ACTION_V1_SHOW_RECEIPT = "com.clover.remote.terminal.remotecontrol.action.V1_SHOW_RECEIPT";
  protected static final String ACTION_V1_SHOW_THANK_YOU = "com.clover.remote.terminal.remotecontrol.action.V1_SHOW_THANK_YOU";
  protected static final String ACTION_V1_TX_START_REQUEST = "com.clover.remote.terminal.remotecontrol.action.V1_TX_START";
  protected static final String ACTION_V1_KEYPRESS = "com.clover.remote.terminal.remotecontrol.action.V1_INPUT";
  protected static final String ACTION_V1_VOID_PAYMENT = "com.clover.remote.terminal.remotecontrol.action.V1_VOID_PAYMENT";
  protected static final String ACTION_V1_REFUND = "com.clover.remote.terminal.remotecontrol.action.ACTION_V1_REFUND";
  protected static final String ACTION_V1_OPEN_CASH_DRAWER = "com.clover.remote.terminal.remotecontrol.action.ACTION_V1_OPEN_CASH_DRAWER";
  protected static final String ACTION_V1_TIP_ADJUST = "com.clover.remote.terminal.remotecontrol.action.ACTION_V1_TIP_ADJUST";
  protected static final String ACTION_V1_SIGNATURE_VERIFIED = "com.clover.remote.terminal.remotecontrol.action.V1_SIGNATURE_VERIFIED";
  protected static final String ACTION_V1_TERMINAL_MESSAGE = "com.clover.remote.terminal.remotecontrol.action.V1_TERMINAL_MESSAGE";
  protected static final String ACTION_V1_BREAK = "com.clover.remote.terminal.remotecontrol.action.V1_BREAK";
  protected static final String ACTION_V1_PRINT_TEXT = "com.clover.remote.terminal.remotecontrol.action.V1_PRINT_TEXT";
  protected static final String ACTION_V1_SHOW_PAYMENT_RECEIPT_OPTIONS = "com.clover.remote.terminal.remotecontrol.action.V1_SHOW_PAYMENT_RECEIPT_OPTIONS";
  protected static final String ACTION_V1_PRINT_IMAGE = "com.clover.remote.terminal.remotecontrol.action.V1_PRINT_IMAGE";
  protected static final String ACTION_V1_ORDER_ACTION_RESPONSE = "com.clover.remote.terminal.remotecontrol.action.V1_ORDER_ACTION_RESPONSE";
  // sent by service, received by client

  protected static final String EXTRA_UI_STATE = "com.clover.remote.terminal.remotecontrol.extra.UI_STATE";
  protected static final String EXTRA_UI_TEXT = "com.clover.remote.terminal.remotecontrol.extra.UI_TEXT";
  protected static final String EXTRA_UI_DIRECTION = "com.clover.remote.terminal.remotecontrol.extra.UI_DIRECTION";
  protected static final String EXTRA_TX_STATE = "com.clover.remote.terminal.remotecontrol.extra.TX_STATE";
  protected static final String EXTRA_INPUT_OPTIONS = "com.clover.remote.terminal.remotecontrol.extra.INPUT_OPTIONS";
  protected static final String EXTRA_PAYMENT = "com.clover.remote.terminal.remotecontrol.extra.PAYMENT";
  protected static final String EXTRA_CREDIT = "com.clover.remote.terminal.remotecontrol.extra.CREDIT";
  protected static final String EXTRA_SIGNATURE = "com.clover.remote.terminal.remotecontrol.extra.SIGNATURE";
  protected static final String EXTRA_TIP_AMOUNT = "com.clover.remote.terminal.remotecontrol.extra.TIP_AMOUNT";
  protected static final String EXTRA_CASHBACK_AMOUNT = "com.clover.remote.terminal.remotecontrol.extra.CASHBACK_AMOUNT";
  protected static final String EXTRA_PARTIAL_AUTH_AMOUNT = "com.clover.remote.terminal.remotecontrol.extra.PARTIAL_AUTH_AMOUNT";
  protected static final String EXTRA_DISPLAY_ORDER = "com.clover.remote.terminal.remotecontrol.extra.DISPLAY_ORDER";
  protected static final String EXTRA_ORDER = "com.clover.remote.terminal.remotecontrol.extra.ORDER";
  protected static final String EXTRA_ORDER_ID = "com.clover.remote.terminal.remotecontrol.extra.ORDER_ID";
  protected static final String EXTRA_PAYMENT_ID = "com.clover.remote.terminal.remotecontrol.extra.PAYMENT_ID";
  protected static final String EXTRA_REFUND = "com.clover.remote.terminal.remotecontrol.extra.EXTRA_REFUND";

  protected static final String EXTRA_OPEN_CASH_DRAWER_REASON = "com.clover.remote.terminal.remotecontrol.extra.OPEN_CASH_DRAWER_REASON";
  protected static final String EXTRA_REFUND_AMOUNT = "com.clover.remote.terminal.remotecontrol.extra.REFUND_AMOUNT";
  protected static final String EXTRA_EMPLOYEE_ID = "com.clover.remote.terminal.remotecontrol.extra.EMPLOYEE_ID";
  protected static final String EXTRA_KEYPRESS = "com.clover.remote.terminal.remotecontrol.extra.KEY_PRESS";
  protected static final String EXTRA_VOID_REASON = "com.clover.remote.terminal.remotecontrol.extra.VOID_REASON";
  protected static final String EXTRA_PACKAGE_NAME = "com.clover.remote.terminal.remotecontrol.extra.PACKAGE_NAME";
  protected static final String EXTRA_SIGNATURE_VERIFIED = "com.clover.remote.terminal.remotecontrol.extra.SIGNATURE_VERIFIED";
  protected static final String EXTRA_TEXT = "com.clover.remote.terminal.remotecontrol.extra.TEXT";
  protected static final String EXTRA_TEXT_LINES = "com.clover.remote.terminal.remotecontrol.extra.TEXT_LINES";
  protected static final String EXTRA_PNG = "com.clover.remote.terminal.remotecontrol.extra.PNG";
  protected static final String EXTRA_ORDER_OPERATION = "com.clover.remote.terminal.remotecontrol.extra.ORDER_OPERATION";
  protected static final String EXTRA_DECLINE_REASON = "com.clover.remote.terminal.remotecontrol.extra.DECLINE_REASON";
  protected static final String EXTRA_ERROR_CODE = "com.clover.remote.terminal.remotecontrol.extra.ERROR_CODE";
  protected static final String EXTRA_ERROR_MSG = "com.clover.remote.terminal.remotecontrol.extra.ERROR_MSG";
  protected static final String EXTRA_SUPPORTS_ORDER_MODIFICATION = "com.clover.remote.terminal.remotecontrol.extra.SUPPORTS_ORDER_MODIFICATION";
  protected static final String EXTRA_ADD_DISCOUNT_ACTION = "com.clover.remote.terminal.remotecontrol.extra.ADD_DISCOUNT_ACTION";
  protected static final String EXTRA_ADD_LINE_ITEM_ACTION = "com.clover.remote.terminal.remotecontrol.extra.ADD_LINE_ITEM_ACTION";
  protected static final String EXTRA_REMOVE_DISCOUNT_ACTION = "com.clover.remote.terminal.remotecontrol.extra.REMOVE_DISCOUNT_ACTION";
  protected static final String EXTRA_REMOVE_LINE_ITEM_ACTION = "com.clover.remote.terminal.remotecontrol.extra.REMOVE_LINE_ITEM_ACTION";
  protected static final String EXTRA_ORDER_ACTION_RESPONSE = "com.clover.remote.terminal.remotecontrol.extra.ORDER_ACTION_RESPONSE";
  protected static final String EXTRA_SUCCESS = "com.clover.remote.terminal.remotecontrol.extra.SUCCESS";
  protected static final String EXTRA_TX_START_RESPONSE_RESULT = "com.clover.remote.terminal.remotecontrol.extra.TX_START_RESPONSE_RESULT";

  public enum TxStartResponseResult {
    SUCCESS(true, 0),
    ORDER_MODIFIED(false, 0),
    ORDER_LOAD(false, 0),
    FAIL(false, 0);

    public final boolean success;
    public final int messageId;

    TxStartResponseResult(boolean success, int messageId) {
      this.success = success;
      this.messageId = messageId;
    }
  }

  private static final Set<CharSequence> COUNTED_ACTIONS = new HashSet<CharSequence>() {{
    add(ACTION_V1_TX_STATE);
    add(ACTION_V1_VOID_PAYMENT);
    add(ACTION_V1_TX_START_REQUEST);
    add(ACTION_V1_TX_START_RESPONSE);
    add(ACTION_V1_FINISH_OK);
    add(ACTION_V1_FINISH_CANCEL);
  }};

  protected final Context context;
  private boolean registered;

  public RemoteControl(Context context) {
    this.context = context;
    this.registered = false;
  }

  public void register() {
    register(null);
  }

  public void register(Handler handler) {
    if (!registered) {
      context.registerReceiver(this, getIntentFilter(), "com.clover.remote.terminal.permission.REMOTE_TERMINAL", handler);
      registered = true;
    }
  }

  public void unregister() {
    if (registered) {
      context.unregisterReceiver(this);
      registered = false;
    }
  }

  protected abstract IntentFilter getIntentFilter();

  @Override
  public void onReceive(Context context, Intent intent) {
    countAction(intent);
  }

  private static final Pattern ACTION_PATTERN = Pattern.compile("(?:([^.]+)\\.?)+");

  private static String getRelative(CharSequence action) {
    Matcher m = ACTION_PATTERN.matcher(action);
    if (!m.find()) {
      return null;
    }
    return m.group(1);
  }

  protected void countAction(Intent intent) {
    if (!COUNTED_ACTIONS.contains(intent.getAction())) {
      return;
    }

    String relativeAction = getRelative(intent.getAction());
    if (relativeAction == null) {
      return;
    }

    String key = String.format("action.%s", relativeAction.toLowerCase());

    // special handling for particular actions
    if (ACTION_V1_TX_STATE.equals(intent.getAction())) {
      TxState txState = intent.getParcelableExtra(EXTRA_TX_STATE);
      key += "." + txState.name().toLowerCase();
    }

  }
}
