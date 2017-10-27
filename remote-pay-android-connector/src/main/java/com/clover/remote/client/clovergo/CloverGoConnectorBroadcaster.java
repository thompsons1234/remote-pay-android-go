package com.clover.remote.client.clovergo;


import com.clover.remote.client.MerchantInfo;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CapturePreAuthResponse;
import com.clover.remote.client.messages.CardApplicationIdentifier;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
import com.clover.remote.client.messages.CustomActivityResponse;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.PrintManualRefundDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintManualRefundReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentMerchantCopyReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentReceiptMessage;
import com.clover.remote.client.messages.PrintRefundPaymentReceiptMessage;
import com.clover.remote.client.messages.ReadCardDataResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.RetrievePendingPaymentsResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.TipAddedMessage;
import com.firstdata.clovergo.domain.model.Order;
import com.firstdata.clovergo.domain.model.Payment;
import com.firstdata.clovergo.domain.model.ReaderInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Akhani, Avdhesh on 5/19/17.
 */

public class CloverGoConnectorBroadcaster extends CopyOnWriteArrayList<ICloverGoConnectorListener> {

    public void notifyOnTipAdded(long tip) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onTipAdded(new TipAddedMessage(tip));
        }
    }

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

    public void notifyOnDeviceActivityStart(CloverDeviceEvent deviceEvent) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onDeviceActivityStart(deviceEvent);
        }
    }

    public void notifyOnDeviceActivityEnd(CloverDeviceEvent deviceEvent) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onDeviceActivityEnd(deviceEvent);
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

    public void notifyOnManualRefundResponse(ManualRefundResponse response) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onManualRefundResponse(response);
        }
    }

    public void notifyOnVerifySignatureRequest(VerifySignatureRequest request) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onVerifySignatureRequest(request);
        }
    }

    public void notifyOnVoidPaymentResponse(VoidPaymentResponse response) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onVoidPaymentResponse(response);
        }
    }

    public void notifyOnConnect() {
        for (ICloverGoConnectorListener listener : this) {
            listener.onDeviceConnected();
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

    public void notifyOnDisconnect() {
        for (ICloverGoConnectorListener listener : this) {
            listener.onDeviceDisconnected();
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

    public void notifyOnVaultCardRespose(VaultCardResponse ccr) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onVaultCardResponse(ccr);
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

    public void notifyOnPrintRefundPaymentReceipt(PrintRefundPaymentReceiptMessage printRefundPaymentReceiptMessage) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onPrintRefundPaymentReceipt(printRefundPaymentReceiptMessage);
        }
    }

    public void notifyOnPrintPaymentMerchantCopyReceipt(PrintPaymentMerchantCopyReceiptMessage printPaymentMerchantCopyReceiptMessage) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onPrintPaymentMerchantCopyReceipt(printPaymentMerchantCopyReceiptMessage);
        }
    }

    public void notifyOnPrintPaymentDeclineReceipt(PrintPaymentDeclineReceiptMessage printPaymentDeclineReceiptMessage) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onPrintPaymentDeclineReceipt(printPaymentDeclineReceiptMessage);
        }
    }

    public void notifyOnPrintPaymentReceipt(PrintPaymentReceiptMessage printPaymentReceiptMessage) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onPrintPaymentReceipt(printPaymentReceiptMessage);
        }
    }

    public void notifyOnPrintCreditReceipt(PrintManualRefundReceiptMessage printManualRefundReceiptMessage) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onPrintManualRefundReceipt(printManualRefundReceiptMessage);
        }
    }

    public void notifyOnPrintCreditDeclineReceipt(PrintManualRefundDeclineReceiptMessage printManualRefundDeclineReceiptMessage) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onPrintManualRefundDeclineReceipt(printManualRefundDeclineReceiptMessage);
        }
    }

    public void notifyOnConfirmPaymentRequest(ConfirmPaymentRequest confirmPaymentRequest) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onConfirmPaymentRequest(confirmPaymentRequest);
        }
    }

    public void notifyOnRetrievePendingPaymentResponse(RetrievePendingPaymentsResponse rppr) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onRetrievePendingPaymentsResponse(rppr);
        }
    }

    public void notifyOnReadCardDataResponse(ReadCardDataResponse rcdr) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onReadCardDataResponse(rcdr);
        }
    }

    public void notifyOnActivityResponse(CustomActivityResponse car) {
        for (ICloverGoConnectorListener listener : this) {
            listener.onCustomActivityResponse(car);
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
}