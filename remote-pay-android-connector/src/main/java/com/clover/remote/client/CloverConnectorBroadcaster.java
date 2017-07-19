/*
 * Copyright (C) 2016 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.remote.client;

import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CapturePreAuthResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.CustomActivityResponse;
import com.clover.remote.client.messages.RetrievePaymentResponse;
import com.clover.remote.client.messages.MessageFromActivity;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
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
import com.clover.remote.client.messages.ResetDeviceResponse;
import com.clover.remote.client.messages.RetrieveDeviceStatusResponse;
import com.clover.remote.client.messages.RetrievePendingPaymentsResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.TipAddedMessage;

import android.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;

public class CloverConnectorBroadcaster extends CopyOnWriteArrayList<ICloverConnectorListener> {

  public void notifyOnTipAdded(long tip) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onTipAdded(new TipAddedMessage(tip));
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnRefundPaymentResponse(RefundPaymentResponse refundPaymentResponse) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onRefundPaymentResponse(refundPaymentResponse);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyCloseout(CloseoutResponse closeoutResponse) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onCloseoutResponse(closeoutResponse);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnDeviceActivityStart(CloverDeviceEvent deviceEvent) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onDeviceActivityStart(deviceEvent);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnDeviceActivityEnd(CloverDeviceEvent deviceEvent) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onDeviceActivityEnd(deviceEvent);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnSaleResponse(SaleResponse response) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onSaleResponse(response);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnAuthResponse(AuthResponse response) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onAuthResponse(response);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnManualRefundResponse(ManualRefundResponse response) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onManualRefundResponse(response);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnVerifySignatureRequest(VerifySignatureRequest request) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onVerifySignatureRequest(request);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnVoidPaymentResponse(VoidPaymentResponse response) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onVoidPaymentResponse(response);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnConnect() {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onDeviceConnected();
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnDisconnect() {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onDeviceDisconnected();
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnReady(MerchantInfo merchantInfo) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onDeviceReady(merchantInfo);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnTipAdjustAuthResponse(TipAdjustAuthResponse response) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onTipAdjustAuthResponse(response);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnVaultCardRespose(VaultCardResponse ccr) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onVaultCardResponse(ccr);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnPreAuthResponse(PreAuthResponse response) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onPreAuthResponse(response);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnCapturePreAuth(CapturePreAuthResponse response) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onCapturePreAuthResponse(response);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnDeviceError(CloverDeviceErrorEvent errorEvent) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onDeviceError(errorEvent);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnPrintRefundPaymentReceipt(PrintRefundPaymentReceiptMessage printRefundPaymentReceiptMessage) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onPrintRefundPaymentReceipt(printRefundPaymentReceiptMessage);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnPrintPaymentMerchantCopyReceipt(PrintPaymentMerchantCopyReceiptMessage printPaymentMerchantCopyReceiptMessage) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onPrintPaymentMerchantCopyReceipt(printPaymentMerchantCopyReceiptMessage);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnPrintPaymentDeclineReceipt(PrintPaymentDeclineReceiptMessage printPaymentDeclineReceiptMessage) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onPrintPaymentDeclineReceipt(printPaymentDeclineReceiptMessage);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnPrintPaymentReceipt(PrintPaymentReceiptMessage printPaymentReceiptMessage) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onPrintPaymentReceipt(printPaymentReceiptMessage);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnPrintCreditReceipt(PrintManualRefundReceiptMessage printManualRefundReceiptMessage) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onPrintManualRefundReceipt(printManualRefundReceiptMessage);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnPrintCreditDeclineReceipt(PrintManualRefundDeclineReceiptMessage printManualRefundDeclineReceiptMessage) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onPrintManualRefundDeclineReceipt(printManualRefundDeclineReceiptMessage);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnConfirmPaymentRequest(ConfirmPaymentRequest confirmPaymentRequest) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onConfirmPaymentRequest(confirmPaymentRequest);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnRetrievePendingPaymentResponse(RetrievePendingPaymentsResponse rppr) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onRetrievePendingPaymentsResponse(rppr);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnReadCardDataResponse(ReadCardDataResponse rcdr) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onReadCardDataResponse(rcdr);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnActivityMessage(MessageFromActivity msg) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onMessageFromActivity(msg);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnActivityResponse(CustomActivityResponse car) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onCustomActivityResponse(car);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnRetrieveDeviceStatusResponse(RetrieveDeviceStatusResponse rdsr) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onRetrieveDeviceStatusResponse(rdsr);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnResetDeviceResponse(ResetDeviceResponse rdr) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onResetDeviceResponse(rdr);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }

  public void notifyOnRetrievePaymentResponse(RetrievePaymentResponse gpr) {
    for (ICloverConnectorListener listener : this) {
      try {
        listener.onRetrievePaymentResponse(gpr);
      } catch (Exception ex) {
        Log.w("Notification error", ex);
      }
    }
  }
}
