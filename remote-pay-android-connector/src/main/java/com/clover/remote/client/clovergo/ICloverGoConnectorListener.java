package com.clover.remote.client.clovergo;

import com.clover.remote.client.ICloverConnectorListener;
import com.clover.remote.client.clovergo.CloverGoConstants.TransactionType;
import com.clover.remote.client.messages.CardApplicationIdentifier;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.TransactionRequest;
import com.firstdata.clovergo.domain.model.Order;
import com.firstdata.clovergo.domain.model.Payment;
import com.firstdata.clovergo.domain.model.ReaderInfo;

import java.util.List;

/**
 * Created by Akhani, Avdhesh on 5/19/17.
 */

public interface ICloverGoConnectorListener extends ICloverConnectorListener {


  /**
   * Called when the Clover Go Bluetooth device is Discovered to connect
   */
  void onDeviceDiscovered(ReaderInfo readerInfo);

  /**
   * Called when the Clover device is disconnected
   */
  void onDeviceDisconnected(ReaderInfo readerInfo);

  /**
   * Chip cards have application identifiers which negotiates with the card reader on what application identifier to use to send card data back to reader to process transaction.
   * <p>
   * In case card has multiple application identifiers and reader is not able to negotiate, explicit consent from customer is needed to proceed.
   * Please return one of the application identifiers from the list to proceed or null to cancel transaction
   *
   * @param applicationIdentifierList - application identifier from the card.
   * @return selected application identifier
   */
  void onAidMatch(List<CardApplicationIdentifier> applicationIdentifierList, AidSelection aidSelection);

  void onPaymentTypeRequired(int cardEntryMethods, List<ReaderInfo> connectedReaders, PaymentTypeSelection paymentTypeSelection);

  void onManualCardEntryRequired(TransactionType transactionType, TransactionRequest saleRequest, ICloverGoConnector.GoPaymentType goPaymentType,
                                 ReaderInfo.ReaderType readerType, boolean allowDuplicate, ManualCardEntry manualCardEntry);

  void notifyOnProgressDialog(String title, String message, boolean isCancelable);

  /**
   * on AidSelection return selected Application Identifier
   */
  interface AidSelection {
    void selectApplicationIdentifier(CardApplicationIdentifier selectedCardApplicationIdentifier);
  }

  /**
   * on payment type selected
   */
  interface PaymentTypeSelection {
    void selectPaymentType(ICloverGoConnector.GoPaymentType goPaymentType, ReaderInfo.ReaderType readerType);
  }

  /**
   * on manual card data entered (keyed)
   */
  interface ManualCardEntry {
    void cardDataEntered(TransactionRequest transactionRequest, TransactionType transactionType);
  }

  void onCloverGoDeviceActivity(CloverDeviceEvent deviceEvent);

  void onGetMerchantInfo();

  void onGetMerchantInfoResponse(boolean isSuccess);

  void onSignatureRequired(Payment payment, SignatureCapture signatureCapture);

  interface SignatureCapture {
    void captureSignature(String paymentId, int[][] xy);
  }

  void onSendReceipt(Order order, SendReceipt sendReceipt);

  void onDisplayMessage(String message);

  void onVoidPayment(com.firstdata.clovergo.domain.model.Payment payment, String reason);

  interface SendReceipt {
    void sendRequestedReceipt(String email, String phone, String orderId);

    void noReceipt();
  }
}
