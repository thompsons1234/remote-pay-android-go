package com.clover.remote.client;

import com.clover.common2.Signature2;
import com.clover.remote.client.transport.CloverTransportObserver;
import com.clover.remote.terminal.InputOption;
import com.clover.remote.terminal.KeyPress;
import com.clover.remote.terminal.TxState;
import com.clover.remote.terminal.UiState;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Refund;

public interface CloverDeviceObserver extends CloverTransportObserver {

    void onTxState(TxState txState);
    void onUiState(UiState uiState, String uiText, UiState.UiDirection uiDirection, InputOption[] inputOptions);
    void onTipAdded(long tipAmount);
    void onAuthTipAdjusted(String paymentId, long amount, boolean success);
    void onCashbackSelected(long cashbackAmount);
    void onPartialAuth(long partialAuthAmount);
    void onFinishOk(Payment payment, Signature2 signature2);
    void onFinishOk(Credit credit);
    void onFinishOk(Refund refund);
    void onFinishCancel();
    void onVerifySignature(Payment payment, Signature2 signature);
    void onPaymentVoided(Payment payment, VoidReason voidReason);
    void onKeyPressed(KeyPress keyPress);
    void onPaymentRefundResponse(String orderId, String paymentId, Refund refund, TxState code);

    void onCloseoutResponse();

    //void onPrint(Payment payment, Order order);
    //void onPrint(Credit credit);
    //void onPrintDecline(Payment payment, String reason);
    //void onPrintDecline(Credit credit, String reason);
    //void onPrintMerchantCopy(Payment payment);
    //void onModifyOrder(AddDiscountAction addDiscountAction);
    //void onModifyOrder(RemoveDiscountAction removeDiscountAction);
    //void onModifyOrder(AddLineItemAction addLineItemAction);
    //void onModifyOrder(RemoveLineItemAction removeLineItemAction);
    void onTxStartResponse(boolean success);
}