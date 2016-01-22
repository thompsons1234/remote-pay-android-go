package com.clover.remote.client;

import com.clover.remote.client.messages.*;
import com.clover.remote.protocol.message.TipAddedMessage;
import com.clover.remote.terminal.TxState;

import java.util.ArrayList;

/**
 * Created by blakewilliams on 12/16/15.
 */
public class CloverConnectorBroadcaster extends ArrayList<ICloverConnectorListener> {

    public void notifyOnTipAdded(long tip) {
        for (ICloverConnectorListener listener : this) {
            listener.onTipAdded(new TipAddedMessage(tip));
        }
    }

    public void notifyOnRefundPaymentResponse(RefundPaymentResponse refundPaymentResponse) {
        for (ICloverConnectorListener listener : this) {
            listener.onRefundPaymentResponse(refundPaymentResponse);
        }
    }

    public void notifyCloseout(CloseoutResponse closeoutResponse) {
        for(ICloverConnectorListener listener : this) {
            listener.onCloseoutResponse(closeoutResponse);
        }
    }

    public void notifyOnDeviceActivityStart(CloverDeviceEvent deviceEvent) {
        for(ICloverConnectorListener listener : this) {
            listener.onDeviceActivityStart(deviceEvent);
        }
    }

    public void notifyOnDeviceActivityEnd(CloverDeviceEvent deviceEvent) {
        for(ICloverConnectorListener listener : this) {
            listener.onDeviceActivityEnd(deviceEvent);
        }

    }

    public void notifyOnSaleResponse(SaleResponse response) {
        for(ICloverConnectorListener listener : this) {
            listener.onSaleResponse(response);
        }
    }

    public void notifyOnAuthResponse(AuthResponse response) {
        for(ICloverConnectorListener listener : this) {
            listener.onAuthResponse(response);
        }
    }

    public void notifyOnManualRefundResponse(ManualRefundResponse response) {
        for(ICloverConnectorListener listener : this) {
            listener.onManualRefundResponse(response);
        }
    }

    public void notifyOnSignatureVerifyRequest(SignatureVerifyRequest request) {
        for(ICloverConnectorListener listener : this) {
            listener.onSignatureVerifyRequest(request);
        }
    }

    public void notifyOnVoidPaymentResponse(VoidPaymentResponse response) {
        for(ICloverConnectorListener listener : this) {
            listener.onVoidPaymentResponse(response);
        }
    }

    public void notifyOnConnect() {
        for(ICloverConnectorListener listener : this) {
            listener.onConnected();
        }
    }
    public void notifyOnDisconnect() {
        for(ICloverConnectorListener listener : this) {
            listener.onDisconnected();
        }
    }
    public void notifyOnReady() {
        for(ICloverConnectorListener listener : this) {
            listener.onReady();
        }
    }

    public void notifyOnTipAdjustAuthResponse(TipAdjustAuthResponse response) {
        for(ICloverConnectorListener listener : this) {
            listener.onAuthTipAdjustResponse(response);
        }
    }

    public void notifyOnTxState(TxState txState) {
        for(ICloverConnectorListener listener : this) {
            listener.onTransactionState(txState);
        }
    }
}
