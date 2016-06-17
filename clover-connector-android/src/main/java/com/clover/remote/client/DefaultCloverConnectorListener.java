package com.clover.remote.client;

import com.clover.remote.TxState;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CapturePreAuthResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ConfigErrorResponse;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.TipAddedMessage;

/**
 * Created by blakewilliams on 6/14/16.
 */
public class DefaultCloverConnectorListener implements ICloverConnectorListener {
  private boolean ready = false;
  MerchantInfo merchantInfo;

  public boolean isReady() {
    return ready;
  }

  @Override public void onDeviceDisconnected() {
    ready = false;
  }

  @Override public void onDeviceConnected() {
    ready = false;
  }

  @Override public void onDeviceReady(MerchantInfo merchantInfo) {
    ready = true;
    this.merchantInfo = merchantInfo;
  }

  @Override public void onDeviceActivityStart(CloverDeviceEvent deviceEvent) {

  }

  @Override public void onDeviceActivityEnd(CloverDeviceEvent deviceEvent) {

  }

  @Override public void onDeviceError(CloverDeviceErrorEvent deviceErrorEvent) {

  }

  @Override public void onAuthResponse(AuthResponse response) {

  }

  @Override public void onTipAdjustAuthResponse(TipAdjustAuthResponse response) {

  }

  @Override public void onCapturePreAuthResponse(CapturePreAuthResponse response) {

  }

  @Override public void onVerifySignatureRequest(VerifySignatureRequest request) {

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

}
