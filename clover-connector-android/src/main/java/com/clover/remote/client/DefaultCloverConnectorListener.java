package com.clover.remote.client;

import com.clover.remote.TxState;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CaptureAuthResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ConfigErrorResponse;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.SignatureVerifyRequest;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.TipAddedMessage;

/**
 * A default implementation that provides only an auto-accept signature
 * capability and boolean isReady() method.
 */
public class DefaultCloverConnectorListener implements ICloverConnectorListener {

  private ICloverConnector cloverConnector;
  boolean isReady = false;

  public DefaultCloverConnectorListener(ICloverConnector cloverConnector) {
    this.cloverConnector = cloverConnector;
  }

  public boolean isReady() {
    return isReady;
  }

  @Override public void onDisconnected() {
    isReady = false;
  }

  @Override public void onConnected() {
    isReady = false;
  }

  @Override public void onReady(MerchantInfo merchantInfo) {
    isReady = true;
  }

  @Override public void onError(Exception e) {

  }

  @Override public void onDebug(String s) {

  }

  @Override public void onDeviceActivityStart(CloverDeviceEvent deviceEvent) {

  }

  @Override public void onDeviceActivityEnd(CloverDeviceEvent deviceEvent) {

  }

  @Override public void onDeviceError(CloverDeviceErrorEvent deviceErrorEvent) {

  }

  @Override public void onAuthResponse(AuthResponse response) {

  }

  @Override public void onAuthTipAdjustResponse(TipAdjustAuthResponse response) {

  }

  @Override public void onPreAuthCaptureResponse(CaptureAuthResponse response) {

  }

  @Override public void onSignatureVerifyRequest(SignatureVerifyRequest request) {
    if (cloverConnector != null) {
      cloverConnector.acceptSignature(request);
    }
  }

  @Override public void onCloseoutResponse(CloseoutResponse response) {

  }

  @Override public void onSaleResponse(SaleResponse response) {

  }

  @Override public void onPreAuthResponse(PreAuthResponse response) {

  }

  @Override public void onManualRefundResponse(ManualRefundResponse response) {

  }

  @Override public void onRefundPaymentResponse(RefundPaymentResponse response) {

  }

  @Override public void onTipAdded(TipAddedMessage message) {

  }

  @Override public void onVoidPaymentResponse(VoidPaymentResponse response) {

  }

  @Override public void onVaultCardResponse(VaultCardResponse response) {

  }

  @Override public void onTransactionState(TxState txState) {

  }

  @Override public void onConfigErrorResponse(ConfigErrorResponse response) {

  }
}
