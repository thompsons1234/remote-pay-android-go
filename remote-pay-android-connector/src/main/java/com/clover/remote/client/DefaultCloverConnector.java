package com.clover.remote.client;

import com.clover.remote.Challenge;
import com.clover.remote.InputOption;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.CapturePreAuthRequest;
import com.clover.remote.client.messages.CloseoutRequest;
import com.clover.remote.client.messages.ManualRefundRequest;
import com.clover.remote.client.messages.PreAuthRequest;
import com.clover.remote.client.messages.ReadCardDataRequest;
import com.clover.remote.client.messages.RefundPaymentRequest;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.client.messages.TipAdjustAuthRequest;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentRequest;
import com.clover.remote.order.DisplayOrder;
import com.clover.sdk.v3.payments.Payment;

import android.graphics.Bitmap;

import java.util.List;

public class DefaultCloverConnector implements ICloverConnector {
  protected CloverConnectorBroadcaster broadcaster = new CloverConnectorBroadcaster();

  @Override
  public void addCloverConnectorListener(ICloverConnectorListener connectorListener) {
    broadcaster.add(connectorListener);
  }

  @Override
  public void removeCloverConnectorListener(ICloverConnectorListener connectorListener) {
    broadcaster.remove(connectorListener);
  }

  @Override
  public void printText(List<String> messages) {
    
  }

  @Override
  public void showMessage(String message) {
    
  }

  @Override
  public void showWelcomeScreen() {
    
  }

  @Override
  public void printImage(Bitmap image) {
    
  }

  @Override
  public void showThankYouScreen() {
    
  }

  @Override
  public void printImageFromURL(String url) {
    
  }

  @Override
  public void displayPaymentReceiptOptions(String orderId, String paymentId) {
    
  }

  @Override
  public void openCashDrawer(String reason) {
    
  }

  @Override
  public void initializeConnection() {
    
  }

  @Override
  public void showDisplayOrder(DisplayOrder order) {
    
  }

  @Override
  public void sale(SaleRequest request) {
    
  }

  @Override
  public void removeDisplayOrder(DisplayOrder order) {
    
  }

  @Override
  public void acceptSignature(VerifySignatureRequest request) {
    
  }

  @Override
  public void invokeInputOption(InputOption io) {
    
  }

  @Override
  public void rejectSignature(VerifySignatureRequest request) {
    
  }

  @Override
  public void dispose() {
    
  }

  @Override
  public void resetDevice() {
    
  }

  @Override
  public void acceptPayment(Payment payment) {
    
  }

  @Override
  public void cancel() {
    
  }

  @Override
  public void rejectPayment(Payment payment, Challenge challenge) {
    
  }

  @Override
  public void auth(AuthRequest request) {
    
  }

  @Override
  public void preAuth(PreAuthRequest request) {
    
  }

  @Override
  public void capturePreAuth(CapturePreAuthRequest request) {
    
  }

  @Override
  public void tipAdjustAuth(TipAdjustAuthRequest request) {
    
  }

  @Override
  public void voidPayment(VoidPaymentRequest request) {
    
  }

  @Override
  public void refundPayment(RefundPaymentRequest request) {
    
  }

  @Override
  public void manualRefund(ManualRefundRequest request) {
    
  }

  @Override
  public void vaultCard(Integer cardEntryMethods) {
    
  }

  @Override
  public void retrievePendingPayments() {
    
  }

  @Override
  public void readCardData(ReadCardDataRequest request) {
    
  }

  @Override
  public void closeout(CloseoutRequest request) {
    
  }
}
