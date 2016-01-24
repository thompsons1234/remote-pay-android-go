package com.clover.remote.protocol.message;

import com.clover.remote.protocol.RemoteMessage;

public enum Method {
  LAST_MSG_REQUEST(LastMessageRequestMessage.class),
  LAST_MSG_RESPONSE(LastMessageRequestMessage.class),
  TIP_ADJUST(TipAdjustMessage.class),
  TIP_ADJUST_RESPONSE(TipAdjustResponseMessage.class),
  OPEN_CASH_DRAWER(OpenCashDrawerMessage.class),
  SHOW_PAYMENT_RECEIPT_OPTIONS(ShowPaymentReceiptOptionsMessage.class),
  REFUND_RESPONSE(RefundResponseMessage.class),
  REFUND_REQUEST(RefundRequestMessage.class),
  TX_START(TxStartRequestMessage.class),
  TX_START_RESPONSE(TxStartResponseMessage.class),
  KEY_PRESS(KeyPressMessage.class),
  UI_STATE(UiStateMessage.class),
  TX_STATE(TxStateMessage.class),
  FINISH_OK(FinishOkMessage.class),
  FINISH_CANCEL(FinishCancelMessage.class),
  DISCOVERY_REQUEST(DiscoveryRequestMessage.class),
  DISCOVERY_RESPONSE(DiscoveryResponseMessage.class),
  TIP_ADDED(TipAddedMessage.class),
  VERIFY_SIGNATURE(VerifySignatureMessage.class),
  SIGNATURE_VERIFIED(SignatureVerifiedMessage.class),
  PAYMENT_VOIDED(PaymentVoidedMessage.class),
  PRINT_PAYMENT(PaymentPrintMessage.class),
  REFUND_PRINT_PAYMENT(RefundPaymentPrintMessage.class),
  PRINT_PAYMENT_MERCHANT_COPY(PaymentPrintMerchantCopyMessage.class),
  PRINT_CREDIT(CreditPrintMessage.class),
  PRINT_PAYMENT_DECLINE(DeclinePaymentPrintMessage.class),
  PRINT_CREDIT_DECLINE(DeclineCreditPrintMessage.class),
  PRINT_TEXT(TextPrintMessage.class),
  PRINT_IMAGE(ImagePrintMessage.class),
  TERMINAL_MESSAGE(TerminalMessage.class),
  SHOW_WELCOME_SCREEN(WelcomeMessage.class),
  SHOW_THANK_YOU_SCREEN(ThankYouMessage.class),
  SHOW_RECEIPT_SCREEN(ReceiptMessage.class),
  SHOW_ORDER_SCREEN(OrderUpdateMessage.class),
  BREAK(BreakMessage.class),
  CASHBACK_SELECTED(CashbackSelectedMessage.class),
  PARTIAL_AUTH(PartialAuthMessage.class),
  VOID_PAYMENT(VoidPaymentMessage.class),
  ORDER_ACTION_ADD_DISCOUNT(OrderActionAddDiscountMessage.class),
  ORDER_ACTION_REMOVE_DISCOUNT(OrderActionRemoveDiscountMessage.class),
  ORDER_ACTION_ADD_LINE_ITEM(OrderActionAddLineItemMessage.class),
  ORDER_ACTION_REMOVE_LINE_ITEM(OrderActionRemoveLineItemMessage.class),
  ORDER_ACTION_RESPONSE(OrderActionResponseMessage.class),
  ;

  final Class<? extends Message> cls;

  Method(Class<? extends Message> cls) {
    this.cls = cls;
  }

  public boolean isMatch(RemoteMessage message) {
    return message != null && name().equalsIgnoreCase(message.method);
  }

}
