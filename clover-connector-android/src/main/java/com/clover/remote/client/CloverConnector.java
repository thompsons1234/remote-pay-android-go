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

import com.clover.remote.InputOption;
import com.clover.remote.KeyPress;
import com.clover.remote.ResultStatus;
import com.clover.remote.TxState;
import com.clover.remote.UiState;
import com.clover.remote.client.device.CloverDevice;
import com.clover.remote.client.device.CloverDeviceConfiguration;
import com.clover.remote.client.device.CloverDeviceFactory;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CapturePreAuthRequest;
import com.clover.remote.client.messages.CapturePreAuthResponse;
import com.clover.remote.client.messages.CloseoutRequest;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.PreAuthRequest;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.ResultCode;
import com.clover.remote.client.messages.TransactionRequest;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ManualRefundRequest;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.RefundPaymentRequest;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.TipAdjustAuthRequest;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VoidPaymentRequest;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.DiscoveryResponseMessage;
import com.clover.remote.order.DisplayDiscount;
import com.clover.remote.order.DisplayLineItem;
import com.clover.remote.order.DisplayOrder;
import com.clover.remote.order.operation.DiscountsAddedOperation;
import com.clover.remote.order.operation.DiscountsDeletedOperation;
import com.clover.remote.order.operation.LineItemsAddedOperation;
import com.clover.remote.order.operation.LineItemsDeletedOperation;
import com.clover.remote.order.operation.OrderDeletedOperation;
import com.clover.sdk.internal.PayIntent;
import com.clover.sdk.internal.Signature2;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Batch;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Refund;
import com.clover.sdk.v3.payments.VaultedCard;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class CloverConnector implements ICloverConnector {

  private static final int KIOSK_CARD_ENTRY_METHODS = 1 << 15;
  public static final int CARD_ENTRY_METHOD_MAG_STRIPE = 0b0001 | 0b0001_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33026
  public static final int CARD_ENTRY_METHOD_ICC_CONTACT = 0b0010 | 0b0010_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33282
  public static final int CARD_ENTRY_METHOD_NFC_CONTACTLESS = 0b0100 | 0b0100_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33796
  public static final int CARD_ENTRY_METHOD_MANUAL = 0b1000 | 0b1000_00000000 | KIOSK_CARD_ENTRY_METHODS; // 34824

  public static final InputOption CANCEL_INPUT_OPTION = new InputOption(KeyPress.ESC, "Cancel");

  //List<ICloverConnectorListener> listeners = new ArrayList<>();
  Gson gson = new Gson();
  private Exception lastException = null;
  private Object lastRequest;

  // manual is not enabled by default
  private final int cardEntryMethods = CARD_ENTRY_METHOD_MAG_STRIPE | CARD_ENTRY_METHOD_ICC_CONTACT | CARD_ENTRY_METHOD_NFC_CONTACTLESS;// | CARD_ENTRY_METHOD_MANUAL;

  protected CloverDevice device;
  private InnerDeviceObserver deviceObserver;

  private CloverConnectorBroadcaster broadcaster = new CloverConnectorBroadcaster();

  private MerchantInfo merchantInfo;

  private CloverDeviceConfiguration configuration;

  public CloverConnector() {

  }

  /**
   * CloverConnector constructor
   *
   * @param config - A CloverDeviceConfiguration object; TestDeviceConfiguration can be used for testing
   */
  public CloverConnector(CloverDeviceConfiguration config) {
    this.configuration = config;
  }

  public void addCloverConnectorListener(ICloverConnectorListener connectorListener) {
    broadcaster.add(connectorListener);
  }

  public void removeCloverConnectorListener(ICloverConnectorListener connectorListener) {
    broadcaster.remove(connectorListener);
  }

  /// <summary>
  /// Initialize the connector with a given configuration
  /// </summary>
  /// <param name="config">A CloverDeviceConfiguration object; TestDeviceConfiguration can be used for testing</param>
  private void initialize(final CloverDeviceConfiguration config) {
    if (device != null) {
      device.dispose();
    }
    this.configuration = config;
    deviceObserver = new InnerDeviceObserver(this);

    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        device = CloverDeviceFactory.get(config); // network access, so needs to be off UI thread
        if (device != null) {
          device.Subscribe(deviceObserver);
        }
        return null;
      }
    }.execute();
  }

  @Override
  public void initializeConnection() {
    if(device == null) {
      initialize(configuration);
    }
  }
  /**
   * Sale method, aka "purchase"
   *
   * @param request
   */
  public void sale(SaleRequest request) {
    lastRequest = request;
    if(device == null) {
      deviceObserver.onFinishCancel(ResultCode.ERROR, "Device connection Error", "In Sale : SaleRequest - The Clover device is not connected.");
      return;
    } else if(request == null) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid argument.", "In Sale : SaleRequest - The request that was passed in for processing is null.");
      return;
    } else if(request.getAmount() <= 0) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Request validation error", "In Sale: SaleRequest - the request amount cannot be zero. Original Request = " + request);
      return;
    } else if (request.getExternalId() == null || request.getExternalId().trim().length() == 0 || request.getExternalId().trim().length() > 32){
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid argument.", "In Sale : SaleRequest - The externalId is invalid. It is required and the max length is 32. Original Request = " + request);
      return;
    } else if (request.getVaultedCard() != null && !merchantInfo.supportsVaultCards) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In Sale : SaleRequest - Vault Card support is not enabled for the payment gateway. Original Request = " + request);
      return;
    }


    if (request.getTipAmount() == null) {
      request.setTipAmount(0L);
    }
    try {
      saleAuth(request, false);
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      deviceObserver.onFinishCancel(ResultCode.ERROR, e.getMessage(), sw.toString());
    }

  }

  /**
   * A common PayIntent builder method for Sale, Auth and PreAuth
   *
   * @param request
   */
  private void saleAuth(TransactionRequest request, boolean suppressTipScreen) throws Exception {
    //payment, finishOK(payment), finishCancel, onPaymentVoided
    if (device != null) {
        lastRequest = request;

        PayIntent.Builder builder = new PayIntent.Builder();

        builder.transactionType(request.getType()); // difference between sale, auth and auth(preAuth)
        builder.amount(request.getAmount());
        builder.cardEntryMethods(request.getCardEntryMethods() != null ? request.getCardEntryMethods() : cardEntryMethods);
        if(request.getDisablePrinting() != null) {
          builder.remotePrint(request.getDisablePrinting());
        }
        if(request.getCardNotPresent() != null) {
          builder.cardNotPresent(request.getCardNotPresent());
        }
        if(request.getDisableRestartTransactionOnFail() != null) {
          builder.disableRestartTransactionWhenFailed(request.getDisableRestartTransactionOnFail());
        }
        builder.vaultedCard(request.getVaultedCard());
        builder.externalPaymentId(request.getExternalId().trim());



        if (request instanceof PreAuthRequest) {
          // nothing extra as of now
        }
        else if (request instanceof AuthRequest) {

          AuthRequest req = (AuthRequest)request;
          if(req.getAllowOfflinePayment() != null) {
            builder.allowOfflinePayment(req.getAllowOfflinePayment());
          }
          if(req.getApproveOfflinePaymentWithoutPrompt() != null) {
            builder.approveOfflinePaymentWithoutPrompt(req.getApproveOfflinePaymentWithoutPrompt());
          }
          if(req.getDisableCashback() != null) {
            builder.disableCashback(req.getDisableCashback());
          }
          if(req.getTaxAmount() != null) {
            builder.taxAmount(req.getTaxAmount());
          }
        }
        else if (request instanceof SaleRequest) {

          SaleRequest req = (SaleRequest) request;
          // shared with AuthRequest
          if(req.getAllowOfflinePayment() != null) {
            builder.allowOfflinePayment(req.getAllowOfflinePayment());
          }
          if(req.getApproveOfflinePaymentWithoutPrompt() != null) {
            builder.approveOfflinePaymentWithoutPrompt(req.getApproveOfflinePaymentWithoutPrompt());
          }
          if(req.getDisableCashback() != null) {
            builder.disableCashback(req.getDisableCashback());
          }
          if(req.getTaxAmount() != null) {
            builder.taxAmount(req.getTaxAmount());
          }
          // SaleRequest
          if(req.getTippableAmount() != null) {
            builder.tippableAmount(req.getTippableAmount());
          }
          if(req.getTipAmount() != null) {
            builder.tipAmount(req.getTipAmount());
          }

          // sale could pass in the tipAmount and not override on the screen,
          // but that is the exceptional case
          if (req.getDisableTipOnScreen() != null) {
            if(req.getDisableTipOnScreen() != null) {
              suppressTipScreen = req.getDisableTipOnScreen();
            }
          }
        }

        PayIntent payIntent = builder.build();

        device.doTxStart(payIntent, null, suppressTipScreen); //

    }
  }

  /**
   * If signature is captured during a Sale, this method accepts the signature as entered
   *
   * @param request
   */
  public void acceptSignature(VerifySignatureRequest request) {
    if(request == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In acceptSignature : VerifySignatureRequest cannot be null."));
    }
    if(request.getPayment() == null || request.getPayment().getId() == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In acceptSignature : VerifySignatureRequest.Payment must have an Id."));
    }
    device.doSignatureVerified(request.getPayment(), true);
  }

  /**
   * If signature is captured during a Sale, this method rejects the signature as entered
   *
   * @param request
   */
  public void rejectSignature(VerifySignatureRequest request) {
    if(request == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In acceptSignature : VerifySignatureRequest cannot be null."));
    }
    if(request.getPayment() == null || request.getPayment().getId() == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In acceptSignature : VerifySignatureRequest.Payment must have an Id."));
    }
    device.doSignatureVerified(request.getPayment(), false);
  }

  /**
   * Auth method to obtain an Auth or Pre-Auth(deprecated - use preAuth()), based on the AuthRequest IsPreAuth flag
   *
   * @param request
   */
  public void auth(AuthRequest request) {
    lastRequest = request;
    if(device == null) {
      deviceObserver.onFinishCancel(ResultCode.ERROR, "Device connection Error", "In Auth : Auth Request - The Clover device is not connected.");
      return;
    } else if(request == null) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid argument.", "In Auth : AuthRequest - The request that was passed in for processing is null.");
      return;
    } else if(request.getAmount() <= 0) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Request validation error", "In Auth: AuthRequest - the request amount cannot be zero. Original Request = " + request);
      return;
    } else if (request.getExternalId() == null || request.getExternalId().trim().length() == 0 || request.getExternalId().trim().length() > 32){
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid argument.", "In Auth : AuthRequest - The externalId is invalid. It is required and the max length is 32. Original Request = " + request);
      return;
    } else if (request.getVaultedCard() != null && !merchantInfo.supportsVaultCards) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In Auth : AuthRequest - Vault Card support is not enabled for the payment gateway. Original Request = " + request);
      return;
    } else if (!merchantInfo.supportsAuths) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In Auth : AuthRequest - Auth's are not enabled for the payment gateway. Original Request = " + request);
      return;
    }

    try {
      saleAuth(request, true);
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      deviceObserver.onFinishCancel(ResultCode.ERROR, e.getMessage(), sw.toString());
    }

  }


  public void preAuth(PreAuthRequest request) {
    lastRequest = request;
    if(device == null) {
      deviceObserver.onFinishCancel(ResultCode.ERROR, "Device connection Error", "In Auth : Auth Request - The Clover device is not connected.");
      return;
    } else if(request == null) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid argument.", "In Auth : AuthRequest - The request that was passed in for processing is null.");
      return;
    } else if(request.getAmount() <= 0) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Request validation error", "In Auth: AuthRequest - the request amount cannot be zero. Original Request = " + request);
      return;
    } else if (request.getExternalId() == null || request.getExternalId().trim().length() == 0 || request.getExternalId().trim().length() > 32){
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid argument.", "In Auth : AuthRequest - The externalId is invalid. It is required and the max length is 32. Original Request = " + request);
      return;
    } else if (request.getVaultedCard() != null && !merchantInfo.supportsVaultCards) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In Auth : AuthRequest - Vault Card support is not enabled for the payment gateway. Original Request = " + request);
      return;
    } else if (!merchantInfo.supportsPreAuths) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In Auth : AuthRequest - PreAuth's are not enabled for the payment gateway. Original Request = " + request);
      return;
    }

    try {
      saleAuth(request, true);
    } catch (Exception e) {
      lastRequest = null;
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      deviceObserver.onFinishCancel(ResultCode.ERROR, e.getMessage(), sw.toString());
    }

  }

  /**
   * Capture a previous Auth. Note: Should only be called if request's PaymentID is from an AuthResponse
   *
   * @param request
   */

  public void capturePreAuth(CapturePreAuthRequest request) {
    if(device == null) {
      deviceObserver.onCapturePreAuth(ResultCode.ERROR, "Device connection Error", "In capturePreAuth : CapturePreAuth - The Clover device is not connected.", null, null);
      return;
    } else if(request == null) {
      deviceObserver.onCapturePreAuth(ResultCode.FAIL, "Invalid argument.", "In capturePreAuth : CapturePreAuth - The request that was passed in for processing is null.", null, null);
      return;
    } else if(request.getAmount() < 0 || request.getTipAmount() < 0) {
      deviceObserver.onCapturePreAuth(ResultCode.FAIL, "Request validation error", "In capturePreAuth: CapturePreAuth - the request amount must be greater than zero and the tip must be greater than or equal to zero. Original Request = " + request, null, null);
      return;
    } else if (!merchantInfo.supportsPreAuths) {
      deviceObserver.onCapturePreAuth(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In capturePreAuth : CapturePreAuth - Tip Adjustments are not enabled for the payment gateway. Original Request = " + request, null, null);
      return;
    }

      if (request != null && !merchantInfo.supportsPreAuths) {
        CapturePreAuthResponse response = new CapturePreAuthResponse(false, ResultCode.UNSUPPORTED);
        response.setReason("Pre Auths unsupported");
        response.setMessage("The currently configured merchant gateway does not support Capture Auth requests.");
        broadcaster.notifyOnCapturePreAuth(response);
      } else {
        device.doCaptureAuth(request.paymentID, request.amount, request.tipAmount);
      }
  }


  /**
   * Adjust the tip for a previous Auth. Note: Should only be called if request's PaymentID is from an AuthResponse
   *
   * @param request
   */
  public void tipAdjustAuth(TipAdjustAuthRequest request) {
    if(device == null) {
      deviceObserver.onAuthTipAdjusted(ResultCode.ERROR, "Device connection Error", "In tipAdjustAuth : TipAdjustAuthRequest - The Clover device is not connected.");
      return;
    } else if(request == null) {
      deviceObserver.onAuthTipAdjusted(ResultCode.FAIL, "Invalid argument.",
          "In tipAdjustAuth : TipAdjustAuthRequest - The request that was passed in for processing is null.");
      return;
    } else if(request.getPaymentId() == null) {
      deviceObserver.onAuthTipAdjusted(ResultCode.FAIL, "Invalid argument.",
          "In tipAdjustAuth : TipAdjustAuthRequest - The paymentId is required.");
      return;
    } else if(request.getTipAmount() < 0) {
      deviceObserver.onAuthTipAdjusted(ResultCode.FAIL, "Request validation error", "In tipAdjustAuth: TipAdjustAuthRequest - the request amount cannot be less than zero. Original Request = " + request);
      return;
    } else if (!merchantInfo.supportsTipAdjust) {
      deviceObserver.onAuthTipAdjusted(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In tipAdjustAuth : TipAdjustAuthRequest - Tip Adjustments are not enabled for the payment gateway. Original Request = " + request);
      return;
    }

    TipAdjustAuthResponse response = new TipAdjustAuthResponse(false, ResultCode.UNSUPPORTED);
    response.setReason("Tip Adjust aren't supported");
    response.setMessage("The currently configured merchant gateway does not support Tip Adjust Auth requests.");
    broadcaster.notifyOnTipAdjustAuthResponse(response);
  }

  public void vaultCard(Integer cardEntryMethods) {
    if(device == null) {
      deviceObserver.onAuthTipAdjusted(ResultCode.ERROR, "Device connection Error", "In vaultCard : Integer - The Clover device is not connected.");
      return;
    } else if (!merchantInfo.supportsTipAdjust) {
      deviceObserver.onAuthTipAdjusted(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In vaultCard : Integer - Vaulting Cards is not enabled for the payment gateway.");
      return;
    }

    device.doVaultCard(cardEntryMethods != null ? cardEntryMethods : getCardEntryMethods());
  }

  /**
   * Void a transaction, given a previously used order ID and/or payment ID
   * TBD - defining a payment or order ID to be used with a void without requiring a response from Sale()
   *
   * @param request
   */
  public void voidPayment(VoidPaymentRequest request) // SaleResponse is a Transaction? or create a Transaction from a SaleResponse
  {

    if(device == null) {
      deviceObserver.onPaymentVoided(ResultCode.ERROR, "Device connection Error", "In tipAdjustAuth : TipAdjustAuthRequest - The Clover device is not connected.");
      return;
    } else if(request == null) {
      deviceObserver.onPaymentVoided(ResultCode.FAIL, "Invalid argument.", "In tipAdjustAuth : TipAdjustAuthRequest - The request that was passed in for processing is null.");
      return;
    } else if(request.getPaymentId() == null) {
      deviceObserver.onPaymentVoided(ResultCode.FAIL, "Invalid argument.", "In tipAdjustAuth : TipAdjustAuthRequest - The paymentId is required.");
      return;
    }

    Payment payment = new Payment();
    payment.setId(request.getPaymentId());
    payment.setOrder(new Reference());
    payment.getOrder().setId(request.getOrderId());
    payment.setEmployee(new Reference());
    payment.getEmployee().setId(request.getEmployeeId());
    VoidReason reason = VoidReason.valueOf(request.getVoidReason());
    device.doVoidPayment(payment, reason);
  }

  /**
   * called when requesting a payment be voided when only the request UUID is available
   * @param request
   */
    /*public void VoidTransaction(VoidTransactionRequest request) {
        return 0;
    }*/

  /**
   * Refund a specific payment
   *
   * @param request
   */
  public void refundPayment(RefundPaymentRequest request) {
    if (device == null)
    {
      RefundPaymentResponse prr = new RefundPaymentResponse(false, ResultCode.ERROR);
      prr.setRefund(null);
      prr.setReason("Device Connection Error");
      prr.setMessage("In RefundPayment : RefundPaymentRequest - The Clover device is not connected.");
      deviceObserver.lastPRR = prr;
      deviceObserver.onFinishCancel();
      return;
    }
    if (request == null)
    {
      RefundPaymentResponse prr = new RefundPaymentResponse(false, ResultCode.FAIL);
      prr.setRefund(null);
      prr.setReason("Request Validation Error");
      prr.setMessage("In RefundPayment : RefundPaymentRequest - The request that was passed in for processing is empty.");
      deviceObserver.lastPRR = prr;
      deviceObserver.onFinishCancel();
      return;
    }
    if (request.getPaymentId() == null)
    {
      RefundPaymentResponse prr = new RefundPaymentResponse(false, ResultCode.FAIL);
      prr.setRefund(null);
      prr.setReason("Request Validation Error");
      prr.setMessage("In RefundPayment : RefundPaymentRequest PaymentID cannot be empty. " + request);
      deviceObserver.lastPRR = prr;
      deviceObserver.onFinishCancel();
      return;
    }
    if (request.getAmount() <= 0 && !request.isFullRefund())
    {
      RefundPaymentResponse prr = new RefundPaymentResponse(false, ResultCode.FAIL);
      prr.setRefund(null);
      prr.setReason("Request Validation Error");
      prr.setMessage("In RefundPayment : RefundPaymentRequest Amount must be greater than zero when FullRefund is set to false. " + request);
      deviceObserver.lastPRR = prr;
      deviceObserver.onFinishCancel();
      return;
    }
    device.doPaymentRefund(request.getOrderId(), request.getPaymentId(), request.getAmount());
  }

  /**
   * Manual refund method, aka "naked credit"
   *
   * @param request
   */
  public void manualRefund(ManualRefundRequest request) // NakedRefund is a Transaction, with just negative amount
  {
    lastRequest = request;
    if(device == null) {
      deviceObserver.onFinishCancel(ResultCode.ERROR, "Device connection Error", "In ManualRefund : ManualRefundRequest - The Clover device is not connected.");
      return;
    } else if(request == null) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid argument.", "In ManualRefund : ManualRefundRequest - The request that was passed in for processing is null.");
      return;
    } else if(request.getAmount() <= 0) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Request validation error", "In ManualRefund: ManualRefundRequest - the request amount cannot be zero. Original Request = " + request);
      return;
    } else if (request.getExternalId() == null || request.getExternalId().trim().length() == 0 || request.getExternalId().trim().length() > 32){
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid argument.", "In ManualRefund : ManualRefundRequest - The externalId is invalid. It is required and the max length is 32. Original Request = " + request);
      return;
    } else if (request.getVaultedCard() != null && !merchantInfo.supportsVaultCards) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In ManualRefund : ManualRefundRequest - Vault Card support is not enabled for the payment gateway. Original Request = " + request);
      return;
    } else if(!merchantInfo.isSupportsManualRefunds()) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In ManualRefund : ManualRefundRequest - Manual Refunds are not enabled for the payment gateway. Original Request = " + request);
    }

    PayIntent.Builder builder = new PayIntent.Builder();
    builder.amount(-Math.abs(request.getAmount()))
        .cardEntryMethods(request.getCardEntryMethods() != null ? request.getCardEntryMethods() : cardEntryMethods)
        .transactionType(PayIntent.TransactionType.PAYMENT.CREDIT)
        .vaultedCard(request.getVaultedCard())
        .externalPaymentId(request.getExternalId());

    if(request.getDisablePrinting() != null) {
      builder.remotePrint(request.getDisablePrinting());
    }

    if(request.getDisableRestartTransactionOnFail() != null) {
      builder.disableRestartTransactionWhenFailed(request.getDisableRestartTransactionOnFail());
    }

    PayIntent payIntent = builder.build();
    device.doTxStart(payIntent, null, true);
  }

  /**
   * Send a request to the server to closeout all orders.
   */
  public void closeout(CloseoutRequest request) {
    if(device != null) {
      device.doCloseout(request.isAllowOpenTabs(), request.getBatchId());
    }
  }


  /**
   * Cancels the device from waiting for payment card
   */
  public void cancel() {
    invokeInputOption(CANCEL_INPUT_OPTION);
  }

  /**
   * Print simple lines of text to the Clover Mini printer
   *
   * @param messages - list of messages that will be printed, one per line
   */
  public void printText(List<String> messages) {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "printText: The Clover device is not connected."));
      return;
    }
    if(messages == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "printText: Invalid argument, null not allowed."));
      return;
    }
    if (device != null) {
      device.doPrintText(messages);
    }
  }

  /**
   * Print an image on the Clover Mini printer
   *
   * @param bitmap
   */
  public void printImage(Bitmap bitmap) //Bitmap img
  {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "printImage: The Clover device is not connected."));
      return;
    }
    if(bitmap == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "printImage: Invalid argument, null not allowed."));
      return;
    }
    if (device != null) {
      device.doPrintImage(bitmap);
    }
  }

  public void printImageFromURL(String url) {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "printImageFromURL: The Clover device is not connected."));
      return;
    }
    if(url == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "printImageFromURL: Invalid argument, null not allowed."));
      return;
    }
    if (device != null) {
      device.doPrintImage(url);
    }
  }

  /**
   * Show a message on the Clover Mini screen
   *
   * @param message
   */
  public void showMessage(String message) {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "showMessage : The Clover device is not connected."));
      return;
    }
    if(message == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "showMessage: Invalid argument, null not allowed."));
      return;
    }
    if (device != null) {
      device.doTerminalMessage(message);
    }
  }

  /**
   * Return the device to the Welcome Screen
   */
  public void showWelcomeScreen() {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "showWelcomeScreen : The Clover device is not connected."));
      return;
    }
    device.doShowWelcomeScreen();
  }

  /**
   * Show the thank you screen on the device
   */
  public void showThankYouScreen() {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "showThankYouScreen : The Clover device is not connected."));
      return;
    }
    device.doShowThankYouScreen();
  }

  /**
   * Show the customer facing receipt option screen for the specified order/payment.
   */
  public void displayPaymentReceiptOptions(String orderId, String paymentId) {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "displayPaymentReceiptOptions : The Clover device is not connected."));
      return;
    }
    if(orderId == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "displayPaymentReceiptOptions: Invalid argument, orderId can't be null."));
      return;
    }
    if(paymentId == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "displayPaymentReceiptOptions: Invalid argument, paymentId can't be null."));
      return;
    }
    device.doShowPaymentReceiptScreen(orderId, paymentId);
  }

  /**
   * Will trigger cash drawer to open that is connected to Clover Mini
   *
   * @param reason
   */

  public void openCashDrawer(String reason) {
    if (device != null) {
      device.doOpenCashDrawer(reason);
    }
  }

  /// <summary>
  /// Show the DisplayOrder on the device. Replaces the existing DisplayOrder on the device.
  /// </summary>
  /// <param name="order"></param>
  public void showDisplayOrder(DisplayOrder order) {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "showDisplayOrder : The Clover device is not connected."));
      return;
    }
    if(order == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "showDisplayOrder: Invalid argument, order can't be null."));
      return;
    }

    device.doOrderUpdate(order, null);
  }

  /// <summary>
  /// Notify the device of a DisplayLineItem being added to a DisplayOrder
  /// </summary>
  /// <param name="order"></param>
  /// <param name="lineItem"></param>
  public void lineItemAddedToDisplayOrder(final DisplayLineItem lineItem, DisplayOrder order) {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "lineItemAddedToDisplayOrder : The Clover device is not connected."));
      return;
    }
    if(order == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "lineItemAddedToDisplayOrder: Invalid argument, order can't be null."));
      return;
    }
    if(lineItem == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "lineItemAddedToDisplayOrder: Invalid argument, lineItem can't be null."));
      return;
    }

    LineItemsAddedOperation liao = new LineItemsAddedOperation();
    liao.setOrderId(order.getId());
    List<String> lineItemIds = new ArrayList<String>();
    lineItemIds.add(lineItem.getId());
    liao.setIds(lineItemIds);

    device.doOrderUpdate(order, liao);
  }

  /// <summary>
  /// Notify the device of a DisplayLineItem being removed from a DisplayOrder
  /// </summary>
  /// <param name="order"></param>
  /// <param name="lineItem"></param>
  public void lineItemRemovedFromDisplayOrder(DisplayLineItem lineItem, DisplayOrder order) {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "lineItemRemovedFromDisplayOrder : The Clover device is not connected."));
      return;
    }
    if(order == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "lineItemRemovedFromDisplayOrder: Invalid argument, order can't be null."));
      return;
    }
    if(lineItem == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "lineItemRemovedFromDisplayOrder: Invalid argument, lineItem can't be null."));
      return;
    }

    LineItemsDeletedOperation lido = new LineItemsDeletedOperation();
    lido.setOrderId(order.getId());
    List<String> lineItemIds = new ArrayList<String>();
    lineItemIds.add(lineItem.getId());
    lido.setIds(lineItemIds);

    device.doOrderUpdate(order, lido);
  }

  /// <summary>
  /// Notify device of a discount being added to the order.
  /// Note: This is independent of a discount being added to a display line item.
  /// </summary>
  /// <param name="order"></param>
  /// <param name="discount"></param>
  public void discountAddedToDisplayOrder(DisplayDiscount discount, DisplayOrder order) {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "discountAddedToDisplayOrder : The Clover device is not connected."));
      return;
    }
    if(order == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "discountAddedToDisplayOrder: Invalid argument, order can't be null."));
      return;
    }
    if(discount == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "discountAddedToDisplayOrder: Invalid argument, discount can't be null."));
      return;
    }

    DiscountsAddedOperation dao = new DiscountsAddedOperation();
    dao.setOrderId(order.getId());
    List<String> discountIds = new ArrayList<String>();
    discountIds.add(discount.getId());
    dao.setIds(discountIds);

    device.doOrderUpdate(order, dao);
  }

  /// <summary>
  /// Notify the device that a discount was removed from the order.
  /// Note: This is independent of a discount being removed from a display line item.
  /// </summary>
  /// <param name="order"></param>
  /// <param name="discount"></param>
  public void discountRemovedFromDisplayOrder(DisplayDiscount discount, DisplayOrder order) {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "discountRemovedFromDisplayOrder : The Clover device is not connected."));
      return;
    }
    if(order == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "discountRemovedFromDisplayOrder: Invalid argument, order can't be null."));
      return;
    }
    if(discount == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "discountRemovedFromDisplayOrder: Invalid argument, discount can't be null."));
      return;
    }

    DiscountsDeletedOperation dao = new DiscountsDeletedOperation();
    dao.setOrderId(order.getId());
    List<String> discountIds = new ArrayList<String>();
    discountIds.add(discount.getId());
    dao.setIds(discountIds);

    device.doOrderUpdate(order, dao);
  }

  /// <summary>
  /// Remove the DisplayOrder from the device.
  /// </summary>
  /// <param name="order"></param>
  public void removeDisplayOrder(DisplayOrder order) {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "removeDisplayOrder : The Clover device is not connected."));
      return;
    }
    if(order == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "removeDisplayOrder: Invalid argument, order can't be null."));
      return;
    }

    OrderDeletedOperation dao = new OrderDeletedOperation();
    dao.setId(order.getId());
    device.doOrderUpdate(order, dao);
  }

  /**
   *
   */
  public void dispose() {
    broadcaster.clear();
    if (device != null) {
      device.dispose();
    }
  }

  /// <summary>
  /// Invoke the InputOption on the device
  /// </summary>
  /// <param name="io"></param>
  public void invokeInputOption(InputOption io) {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "invokeInputOption : The Clover device is not connected."));
      return;
    }
    device.doKeyPress(io.keyPress);
  }

  @Override
  public void resetDevice() {
    if(device == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "resetDevice : The Clover device is not connected."));
      return;
    }
    device.doResetDevice();
  }

  private int getCardEntryMethods() {
    return cardEntryMethods;
  }


  private class InnerDeviceObserver implements CloverDeviceObserver {

    private RefundPaymentResponse lastPRR;
    CloverConnector cloverConnector;

    class SVR extends VerifySignatureRequest {
      CloverDevice _device;

      public SVR(CloverDevice device) {
        _device = device;
      }

      public void Accept() {
        _device.doSignatureVerified(getPayment(), true);
      }

      public void Reject() {
        _device.doSignatureVerified(getPayment(), false);
      }
    }

    public InnerDeviceObserver(CloverConnector cc) {
      this.cloverConnector = cc;
    }

    public void onTxState(TxState txState) {
      //TODO: For future use
    }

    public void onPartialAuth(long partialAmount) {
      //TODO: For future use
    }

    public void onTipAdded(long tip) {
      cloverConnector.broadcaster.notifyOnTipAdded(tip);
    }

    public void onAuthTipAdjusted(String paymentId, long amount, boolean success) {
      TipAdjustAuthResponse response = new TipAdjustAuthResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setPaymentId(paymentId);
      response.setTipAmount(amount);
      if(!response.isSuccess()) {
        response.setReason("Failure");
        response.setMessage("TipAdjustAuth failed to process for payment ID: " + paymentId);
      }
      cloverConnector.broadcaster.notifyOnTipAdjustAuthResponse(response);
    }

    public void onAuthTipAdjusted(ResultCode resultCode, String reason, String message) {
      TipAdjustAuthResponse taar = new TipAdjustAuthResponse(resultCode == ResultCode.SUCCESS, resultCode);
      taar.setPaymentId(null);
      taar.setTipAmount(0);
      taar.setReason(reason);
      taar.setMessage(message);

      cloverConnector.broadcaster.notifyOnTipAdjustAuthResponse(taar);
    }

    public void onCashbackSelected(long cashbackAmount) {
      //TODO: Implement
    }

    public void onKeyPressed(KeyPress keyPress) {
      //TODO: Implement
    }

    public void onPaymentRefundResponse(String orderId, String paymentId, Refund refund, TxState code) {
      // hold the response for finishOk for the refund. See comments in onFinishOk(Refund)
      RefundPaymentResponse prr = new RefundPaymentResponse("SUCCESS".equals(code.toString()), code.toString() == "SUCCESS" ? ResultCode.SUCCESS : ResultCode.FAIL);
      prr.setOrderId(orderId);
      prr.setPaymentId(paymentId);
      prr.setRefund(refund);
      lastPRR = prr; // set this so we have the appropriate information for when onFinish(Refund) is called
      //cloverConnector.broadcaster.notifyOnRefundPaymentResponse(prr);
    }

    public void onCloseoutResponse(ResultStatus status, String reason, Batch batch) {
      CloseoutResponse cr = new CloseoutResponse(status == ResultStatus.SUCCESS, status.toString() == "SUCCESS" ? ResultCode.SUCCESS : ResultCode.FAIL);
      cr.setReason(reason);
      cr.setBatch(batch);
      cloverConnector.broadcaster.notifyCloseout(cr);
    }

    public void onUiState(UiState uiState, String uiText, UiState.UiDirection uiDirection, InputOption[] inputOptions) {
      //Console.WriteLine(uiText  + " inputOptions: " + inputOptions.Length);
      CloverDeviceEvent deviceEvent = new CloverDeviceEvent();
      deviceEvent.setInputOptions(inputOptions);
      deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.valueOf(uiState.toString()));
      deviceEvent.setMessage(uiText);
      if (uiDirection == UiState.UiDirection.ENTER) {
        cloverConnector.broadcaster.notifyOnDeviceActivityStart(deviceEvent);
      } else if (uiDirection == UiState.UiDirection.EXIT) {
        cloverConnector.broadcaster.notifyOnDeviceActivityEnd(deviceEvent);
        if (uiState.toString().equals(CloverDeviceEvent.DeviceEventState.RECEIPT_OPTIONS.toString())) {
          cloverConnector.device.doShowWelcomeScreen();
        }
      }
    }

    public void onFinishOk(Payment payment, Signature2 signature2) {
      try {
        Object lastReq = cloverConnector.lastRequest;
        cloverConnector.lastRequest = null;
        if (lastReq instanceof PreAuthRequest) {
          PreAuthResponse response = new PreAuthResponse(true, ResultCode.SUCCESS);
          response.setPayment(payment);
          response.setSignature(signature2);
          cloverConnector.broadcaster.notifyOnPreAuthResponse(response);
        } else if (lastReq instanceof AuthRequest) {
          AuthResponse response = new AuthResponse(true, ResultCode.SUCCESS);
          response.setPayment(payment);
          response.setSignature(signature2);
          cloverConnector.broadcaster.notifyOnAuthResponse(response);
        } else if (lastReq instanceof SaleRequest) {
          SaleResponse response = new SaleResponse(true, ResultCode.SUCCESS);
          response.setPayment(payment);
          response.setSignature(signature2);
          cloverConnector.broadcaster.notifyOnSaleResponse(response);
        } else {
          Log.e(getClass().getSimpleName(), String.format("Failed to pair this response: %s", payment));
        }
      } finally {
        cloverConnector.device.doShowThankYouScreen();
      }
    }

    public void onFinishOk(Credit credit) {

      try {
        cloverConnector.lastRequest = null;
        ManualRefundResponse response = new ManualRefundResponse(true, ResultCode.SUCCESS);
        response.setCredit(credit);
        cloverConnector.broadcaster.notifyOnManualRefundResponse(response);
      } finally {
        cloverConnector.device.doShowWelcomeScreen();
      }
    }

    public void onFinishOk(Refund refund) {
      try {
        cloverConnector.lastRequest = null;
        RefundPaymentResponse lastRefundResponse = lastPRR;
        lastPRR = null;
        // Since finishOk is the more appropriate/consistent location in the "flow" to
        // publish the RefundResponse (like SaleResponse, AuthResponse, etc., rather
        // than after the server call, which calls onPaymetRefund),
        // we will hold on to the response from
        // onRefundResponse (Which has more information than just the refund) and publish it here
        if (lastRefundResponse != null) {
          if (lastRefundResponse.getRefund().getId().equals(refund.getId())) {
            cloverConnector.broadcaster.notifyOnRefundPaymentResponse(lastRefundResponse);
          } else {
            Log.e(this.getClass().getName(), "The last PaymentRefundResponse has a different refund than this refund in finishOk");
          }
        } else {
          Log.e(this.getClass().getName(), "Shouldn't get an onFinishOk with having gotten an onPaymentRefund!");
        }
      } finally {
        cloverConnector.device.doShowWelcomeScreen();
      }
    }

    private void onFinishCancel(ResultCode result, String reason, String message) {
      try {
        Object lastReq = lastRequest;
        lastRequest = null;
        if (lastReq instanceof PreAuthRequest) {
          PreAuthResponse preAuthResponse = new PreAuthResponse(false, ResultCode.CANCEL);
          preAuthResponse.setReason(reason != null ? reason : "Request Canceled");
          preAuthResponse.setMessage(message != null ? message : "PreAuth Request canceled by user.");
          preAuthResponse.setPayment(null);
          broadcaster.notifyOnPreAuthResponse(preAuthResponse);
        } else if (lastReq instanceof SaleRequest) {
          SaleResponse saleResponse = new SaleResponse(false, ResultCode.CANCEL);
          saleResponse.setReason(reason != null ? reason : "Request Canceled");
          saleResponse.setMessage(message != null ? message : "SaleRequest canceled by user.");
          saleResponse.setPayment(null);
          broadcaster.notifyOnSaleResponse(saleResponse);
        } else if (lastReq instanceof AuthRequest) {
          AuthResponse authResponse = new AuthResponse(false, ResultCode.CANCEL);
          authResponse.setReason(reason != null ? reason : "Request Canceled");
          authResponse.setMessage(message != null ? message : "AuthRequest canceled by user.");
          authResponse.setPayment(null);
          broadcaster.notifyOnAuthResponse(authResponse);
        } else if (lastReq instanceof ManualRefundRequest) {
          ManualRefundResponse refundResponse = new ManualRefundResponse(false, ResultCode.CANCEL);
          refundResponse.setReason(reason != null ? reason : "Request Canceled");
          refundResponse.setMessage(message != null ? message : "ManualRefundRequest canceled by user.");
          refundResponse.setCredit(null);
          broadcaster.notifyOnManualRefundResponse(refundResponse);
        } else if (lastPRR instanceof RefundPaymentResponse) {
          broadcaster.notifyOnRefundPaymentResponse(lastPRR);
          lastPRR = null;
        }
      } finally {
        if(device != null) {
          device.doShowWelcomeScreen();
        }
      }
    }
    public void onFinishCancel() {
      onFinishCancel(ResultCode.CANCEL, null, null);
    }

    public void onVerifySignature(Payment payment, Signature2 signature) {
      SVR request = new SVR(cloverConnector.device);
      request.setSignature(signature);
      request.setPayment(payment);
      broadcaster.notifyOnSignatureVerifyRequest(request);
    }

    public void onPaymentVoided(ResultCode code, String reason, String message) {
      VoidPaymentResponse response = new VoidPaymentResponse(code == ResultCode.SUCCESS, code);
      response.setReason(reason != null ? reason : code.toString());
      response.setMessage(message != null ? message : "No extended information provided.");
      response.setPaymentId(null);
      cloverConnector.broadcaster.notifyOnVoidPaymentResponse(response);
    }

    public void onPaymentVoided(Payment payment, VoidReason reason) {
      VoidPaymentResponse response = new VoidPaymentResponse(true, ResultCode.SUCCESS);
      response.setPaymentId(payment.getId());

      cloverConnector.broadcaster.notifyOnVoidPaymentResponse(response);
      cloverConnector.device.doShowWelcomeScreen();
    }

    public void onCapturePreAuth(ResultStatus status, String reason, String paymentId, long amount, long tipAmount) {
      CapturePreAuthResponse response = new CapturePreAuthResponse(true, status == ResultStatus.SUCCESS ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setReason(reason);
      response.setPaymentID(paymentId);
      response.setAmount(amount);
      response.setTipAmount(tipAmount);

      cloverConnector.broadcaster.notifyOnCapturePreAuth(response);
    }

    public void onCapturePreAuth(ResultCode code, String reason, String paymentId, Long amount, Long tipAmount) {
      CapturePreAuthResponse response = new CapturePreAuthResponse(code == ResultCode.SUCCESS, code);
      response.setReason(reason);
      response.setPaymentID(paymentId);
      if(amount != null) {
        response.setAmount(amount);
      }
      if(tipAmount != null) {
        response.setTipAmount(tipAmount);
      }

      cloverConnector.broadcaster.notifyOnCapturePreAuth(response);
    }

    public void onVaultCardResponse(VaultedCard vaultedCard, String code, String reason) {
      device.doShowWelcomeScreen();
      VaultCardResponse ccr = new VaultCardResponse(code == "SUCCESS", code == "SUCCESS" ? ResultCode.SUCCESS : ResultCode.FAIL, vaultedCard);
      cloverConnector.broadcaster.notifyOnVaultCardRespose(ccr);
    }

    public void onTxStartResponse(boolean success) {
      //Console.WriteLine("Tx Started? " + success);
      // TODO: when don't we get this? if a transaction has already begun and we try a 2nd?
    }

    public void onDeviceConnected(CloverDevice device) {
      Log.d(getClass().getSimpleName(), "Connected");
      cloverConnector.broadcaster.notifyOnConnect();
    }

    public void onDeviceReady(CloverDevice device, DiscoveryResponseMessage drm) {
      Log.d(getClass().getSimpleName(), "Ready");
      cloverConnector.device.doShowWelcomeScreen();
      MerchantInfo merchantInfo = new MerchantInfo(drm);
      cloverConnector.merchantInfo = merchantInfo;

      if (drm.ready) { //TODO: is this a valid check?
        cloverConnector.broadcaster.notifyOnReady(merchantInfo);
      } else {
        Log.e(CloverConnector.class.getName(), "DiscoveryResponseMessage, not ready...");
      }
    }

    @Override public void onDeviceError(CloverDeviceErrorEvent errorEvent) {
      cloverConnector.broadcaster.notifyOnDeviceError(errorEvent);
    }

    public void onDeviceDisconnected(CloverDevice device) {
      Log.d(getClass().getSimpleName(), "Disconnected");
      cloverConnector.broadcaster.notifyOnDisconnect();
    }

    public void onMessage(String message) {
      //Console.WriteLine("onMessage: " + message);
    }

  }

  private static final SecureRandom random = new SecureRandom();
  private static final char[] vals = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'}; // Crockford's base 32 chars

  // providing a simplified version so we don't have a dependency on common's Ids
  private String getNextId() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 13; i++) {
      int idx = random.nextInt(vals.length);
      sb.append(vals[idx]);
    }
    return sb.toString();
  }
}


