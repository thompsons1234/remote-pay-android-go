package com.clover.remote.client.clovergo;

import android.graphics.Bitmap;

import com.clover.remote.Challenge;
import com.clover.remote.InputOption;
import com.clover.remote.client.ICloverConnectorListener;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.CapturePreAuthRequest;
import com.clover.remote.client.messages.CloseoutRequest;
import com.clover.remote.client.messages.CustomActivityRequest;
import com.clover.remote.client.messages.DisplayReceiptOptionsRequest;
import com.clover.remote.client.messages.ManualRefundRequest;
import com.clover.remote.client.messages.MessageToActivity;
import com.clover.remote.client.messages.OpenCashDrawerRequest;
import com.clover.remote.client.messages.PreAuthRequest;
import com.clover.remote.client.messages.PrintJobStatusRequest;
import com.clover.remote.client.messages.PrintRequest;
import com.clover.remote.client.messages.ReadCardDataRequest;
import com.clover.remote.client.messages.RefundPaymentRequest;
import com.clover.remote.client.messages.RetrieveDeviceStatusRequest;
import com.clover.remote.client.messages.RetrievePaymentRequest;
import com.clover.remote.client.messages.RetrievePrintersRequest;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.client.messages.TipAdjustAuthRequest;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentRequest;
import com.clover.remote.order.DisplayOrder;
import com.clover.sdk.v3.payments.Payment;
import com.firstdata.clovergo.domain.model.ReaderInfo;

import java.util.List;

public class DefaultCloverGoConnector implements ICloverGoConnector {
  protected CloverGoConnectorBroadcaster broadcaster = new CloverGoConnectorBroadcaster();

  @Override
  public void initializeConnection() {

  }

  @Override
  public void addCloverConnectorListener(ICloverConnectorListener listener) {

  }

  @Override
  public void removeCloverConnectorListener(ICloverConnectorListener listener) {

  }


  @Override
  public void dispose() {

  }


  @Override
  public void printText(List<String> messages) {

  }

  @Override
  public void printImage(Bitmap image) {

  }

  @Override
  public void printImageFromURL(String url) {

  }

  @Override
  public void showMessage(String message) {

  }

  @Override
  public void sendDebugLog(String message) {

  }

  @Override
  public void showWelcomeScreen() {

  }

  @Override
  public void showThankYouScreen() {

  }

  @Override
  public void displayPaymentReceiptOptions(String orderId, String paymentId) {

  }

  @Override
  public void displayPaymentReceiptOptions(DisplayReceiptOptionsRequest request) {

  }

  @Override
  public void openCashDrawer(String reason) {

  }

  @Override
  public void showDisplayOrder(DisplayOrder order) {

  }

  @Override
  public void removeDisplayOrder(DisplayOrder order) {

  }

  @Override
  public void invokeInputOption(InputOption io) {

  }

  @Override
  public void resetDevice() {

  }

  @Override
  public void cancel() {

  }

  @Override
  public void startCustomActivity(CustomActivityRequest request) {

  }

  @Override
  public void retrieveDeviceStatus(RetrieveDeviceStatusRequest request) {

  }

  @Override
  public void retrievePayment(RetrievePaymentRequest request) {

  }

  @Override
  public void sale(SaleRequest request) {

  }

  @Override
  public void acceptSignature(VerifySignatureRequest request) {

  }

  @Override
  public void rejectSignature(VerifySignatureRequest request) {

  }

  @Override
  public void acceptPayment(Payment payment) {

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
  public void sendMessageToActivity(MessageToActivity request) {

  }

  @Override
  public void closeout(CloseoutRequest request) {

  }

  @Override
  public void print(PrintRequest request) {

  }

  @Override
  public void retrievePrinters(RetrievePrintersRequest request) {

  }

  @Override
  public void retrievePrintJobStatus(PrintJobStatusRequest request) {

  }

  @Override
  public void openCashDrawer(OpenCashDrawerRequest request) {

  }

  @Override
  public void addCloverGoConnectorListener(ICloverGoConnectorListener connectorListener) {
    broadcaster.add(connectorListener);
  }

  @Override
  public void removeCloverGoConnectorListener(ICloverGoConnectorListener connectorListener) {
    broadcaster.remove(connectorListener);

  }

  @Override
  public void connectToBluetoothDevice(ReaderInfo readerInfo) {

  }

  @Override
  public void disconnectDevice() {

  }

  @Override
  public void stopDeviceScan() {

  }

  @Override
  public void sendReceipt(String email, String phoneNo, String orderId) {

  }
}
