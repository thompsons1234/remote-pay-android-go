package com.clover.remote.client;

import com.clover.remote.Challenge;
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
import com.clover.sdk.v3.payments.Payment;

public interface IPaymentConnector {
  /**
   * Sale method, aka "purchase"
   *
   * @param request - A SaleRequest object containing basic information needed for the transaction
   */
  void sale(SaleRequest request);

  /**
   * If signature is captured during a Sale, this method accepts the signature as entered
   *
   * @param request -
   **/
  void acceptSignature(VerifySignatureRequest request);

  /**
   * If signature is captured during a Sale, this method rejects the signature as entered
   *
   * @param request -
   **/
  void rejectSignature(VerifySignatureRequest request);

  /**
   * If payment confirmation is required during a Sale, this method accepts the payment
   *
   * @param payment -
   **/
  void acceptPayment(Payment payment);

  /**
   * If payment confirmation is required during a Sale, this method rejects the payment
   *
   * @param payment -
   * @param challenge -
   **/
  void rejectPayment(Payment payment, Challenge challenge);

  /**
   * Auth method to obtain an Auth payment that can be used as the payment
   * to call tipAdjust
   *
   * @param request -
   **/
  void auth(AuthRequest request);

  /**
   * PreAuth method to obtain a Pre-Auth for a card
   *
   * @param request -
   **/
  void preAuth(PreAuthRequest request);

  /**
   * Capture a previous Auth. Note: Should only be called if request's PaymentID is from an AuthResponse
   *
   * @param request -
   **/
  void capturePreAuth(CapturePreAuthRequest request);

  /**
   * Adjust the tip for a previous Auth. Note: Should only be called if request's PaymentID is from an AuthResponse
   *
   * @param request -
   **/
  void tipAdjustAuth(TipAdjustAuthRequest request);

  /**
   * Void a transaction, given a previously used order ID and/or payment ID
   *
   * @param request - A VoidRequest object containing basic information needed to void the transaction
   **/
  void voidPayment(VoidPaymentRequest request);

  /**
   * Refund a specific payment
   *
   * @param request -
   **/
  void refundPayment(RefundPaymentRequest request);

  /**
   * Manual refund method, aka "naked credit"
   *
   * @param request - A ManualRefundRequest object
   **/
  void manualRefund(ManualRefundRequest request);

  /**
   * Vault card information. Requests the mini capture card information and request a payment token from the payment gateway.
   * The value returned in the response is a card, with all the information necessary to use for payment in a SaleRequest or AuthRequest
   *
   * @param cardEntryMethods - The card entry methods allowed to capture the payment token. null will provide default values
   **/
  void vaultCard(Integer cardEntryMethods);

  /**
   * Used to request a list of pending payments that have been taken offline, but
   * haven't processed yet. will trigger an onRetrievePendingPaymentsResponse callback
   */
  void retrievePendingPayments();

  /**
   * Used to request card information. Specifically track1 and track2 information
   *
   * @param request - The card entry methods allowed to request track information. null will provide default values
   */
  void readCardData(ReadCardDataRequest request);

  /**
   * Request a closeout of all orders.
   *
   * @param request -
   */
  void closeout(CloseoutRequest request);

}
