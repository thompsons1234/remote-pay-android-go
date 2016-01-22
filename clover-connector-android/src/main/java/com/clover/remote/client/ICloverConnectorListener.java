package com.clover.remote.client;

import com.clover.remote.client.messages.*;
import com.clover.remote.protocol.message.TipAddedMessage;
import com.clover.remote.terminal.TxState;

import java.lang.Exception;
import java.lang.String;

/**
 * Created by blakewilliams on 12/12/15.
 */
public interface ICloverConnectorListener {
    public void onDisconnected();
    public void onConnected();
    public void onReady();
    public void onError(Exception e);

    public void onDebug(String s);
    public void onDeviceActivityStart(CloverDeviceEvent deviceEvent);
    public void onDeviceActivityEnd(CloverDeviceEvent deviceEvent);
    public void onDeviceError(CloverDeviceErrorEvent deviceErrorEvent);
    public void onAuthResponse(AuthResponse response);
    public void onAuthTipAdjustResponse(TipAdjustAuthResponse response);
    public void onAuthCaptureResponse(CaptureAuthResponse response);
    public void onSignatureVerifyRequest(SignatureVerifyRequest request);
    public void onCloseoutResponse(CloseoutResponse response);
    public void onSaleResponse(SaleResponse response);
    public void onManualRefundResponse(ManualRefundResponse response);
    public void onRefundPaymentResponse(RefundPaymentResponse response);
    public void onTipAdded(TipAddedMessage message);
    //public void OnVoidTransactionResponse(VoidTransactionResponse response);
    public void onVoidPaymentResponse(VoidPaymentResponse response);
    //public void OnDisplayReceiptOptionsResponse(DisplayReceiptOptionsResponse response);
    public void onCaptureCardResponse(CaptureCardResponse response);
    public void onTransactionState(TxState txState);
}
