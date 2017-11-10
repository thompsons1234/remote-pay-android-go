package com.clover.remote.client.clovergo;

import com.clover.remote.client.MerchantInfo;
import com.clover.remote.client.clovergo.CloverGoConstants.TransactionType;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CapturePreAuthResponse;
import com.clover.remote.client.messages.CardApplicationIdentifier;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.TransactionRequest;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.firstdata.clovergo.domain.model.Order;
import com.firstdata.clovergo.domain.model.Payment;
import com.firstdata.clovergo.domain.model.ReaderInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Akhani, Avdhesh on 5/19/17.
 */

public class CloverGoConnectorBroadcaster extends CopyOnWriteArrayList<ICloverGoConnectorListener> {

  public void notifyOnRefundPaymentResponse(RefundPaymentResponse refundPaymentResponse) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onRefundPaymentResponse(refundPaymentResponse);
    }
  }

  public void notifyCloseout(CloseoutResponse closeoutResponse) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onCloseoutResponse(closeoutResponse);
    }
  }

  public void notifyOnSaleResponse(SaleResponse response) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onSaleResponse(response);
    }
  }

  public void notifyOnAuthResponse(AuthResponse response) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onAuthResponse(response);
    }
  }

  public void notifyOnVoidPaymentResponse(VoidPaymentResponse response) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onVoidPaymentResponse(response);
    }
  }

  public void notifyOnDiscovered(ReaderInfo readerInfo) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onDeviceDiscovered(readerInfo);
    }
  }

  public void notifyOnAidMatch(List<CardApplicationIdentifier> cardApplicationIdentifiers, ICloverGoConnectorListener.AidSelection aidSelection) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onAidMatch(cardApplicationIdentifiers, aidSelection);
    }
  }

  public void notifyOnPaymentTypeRequired(int cardEntryMethods, List<ReaderInfo> connectedReaders, ICloverGoConnectorListener.PaymentTypeSelection paymentTypeSelection) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onPaymentTypeRequired(cardEntryMethods, connectedReaders, paymentTypeSelection);
    }
  }

  public void notifyOnManualCardEntryRequired(TransactionType transactionType, TransactionRequest transactionRequest, ICloverGoConnector.GoPaymentType goPaymentType,
                                              ReaderInfo.ReaderType readerType, boolean allowDuplicate, ICloverGoConnectorListener.ManualCardEntry manualCardEntry) {

    for (ICloverGoConnectorListener listener : this) {
      listener.onManualCardEntryRequired(transactionType, transactionRequest, goPaymentType, readerType, allowDuplicate, manualCardEntry);
    }
  }

  public void notifyOnSignatureRequired(Payment payment, ICloverGoConnectorListener.SignatureCapture signatureCapture) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onSignatureRequired(payment, signatureCapture);
    }
  }

  public void notifyOnSendReceipt(Order order, ICloverGoConnectorListener.SendReceipt sendReceipt) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onSendReceipt(order, sendReceipt);
    }
  }

  public void notifyOnDisconnect(ReaderInfo readerInfo) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onDeviceDisconnected(readerInfo);
    }
  }

  public void notifyOnReady(MerchantInfo merchantInfo) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onDeviceReady(merchantInfo);
    }
  }

  public void notifyOnTipAdjustAuthResponse(TipAdjustAuthResponse response) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onTipAdjustAuthResponse(response);
    }
  }

  public void notifyOnPreAuthResponse(PreAuthResponse response) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onPreAuthResponse(response);
    }
  }

  public void notifyOnCapturePreAuth(CapturePreAuthResponse response) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onCapturePreAuthResponse(response);
    }
  }

  public void notifyOnDeviceError(CloverDeviceErrorEvent errorEvent) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onDeviceError(errorEvent);
    }
  }

  public void notifyOnConfirmPaymentRequest(ConfirmPaymentRequest confirmPaymentRequest) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onConfirmPaymentRequest(confirmPaymentRequest);
    }
  }

  public void notifyOnCloverGoDeviceActivity(CloverDeviceEvent deviceEvent) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onCloverGoDeviceActivity(deviceEvent);
    }
  }

  public void notifyOnGetMerchantInfo() {
    for (ICloverGoConnectorListener listener : this) {
      listener.onGetMerchantInfo();
    }
  }

  public void notifyOnGetMerchantInfoResponse(boolean isSuccess) {
    for (ICloverGoConnectorListener listener : this) {
      listener.onGetMerchantInfoResponse(isSuccess);
    }
  }

  public void notifyOnProgressDialog(String title, String message, boolean isCancelable) {
    for (ICloverGoConnectorListener listener : this) {
      listener.notifyOnProgressDialog(title, message, isCancelable);
    }
  }
}