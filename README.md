# Clover SDK for Android PoS Integration

Current version: 1.1

## Overview

This SDK provides an API with which to allow your Android-based Point-of-Sale (POS) system to interface with a [Clover® Mini device] (https://www.clover.com/pos-hardware/mini). From the Mini, merchants can accept payments using: credit, debit, EMV contact and contactless (including Apple Pay), gift cards, EBT (electronic benefit transfer), and more. Learn more about integrations at [clover.com/integrations](https://www.clover.com/integrations).

The Android project includes both a connector and example. To effectively work with the project you'll need:
- [Gradle](https://gradle.org) (suggested version 2.10).
- An [Android SDK](http://developer.android.com/sdk/index.html) (17+).
- An [IDE](http://developer.android.com/tools/studio/index.html), Android Studio works well .

To complete a transaction end to end, we recommend getting a [Clover Mini Dev Kit](http://cloverdevkit.com/collections/devkits/products/clover-mini-dev-kit).

## Release Notes
# Version 1.1
* Renamed/Added/Removed a number of API operations and request/response objects to establish 
  better consistency across platforms
  
  * ICloverConnector (Operations)
    * Added 
      * printImageFromURL
      * initializeConnection (REQUIRED) 
      * addCloverConnectorListener 
      * removeCloverConnectorListener
      * acceptPayment - (REQUIRED) Takes a payment object - possible response to a ConfirmPaymentRequest
      * rejectPayment - (REQUIRED) Takes a payment object and the challenge that was associated with
                        the rejection - possible response to a ConfirmPaymentRequest
      * retrievePendingPayments - retrieves a list of payments that were taken offline and are pending
                                  server submission/processing.
    * Renamed
      * capturePreAuth - formerly captureAuth
      * showDisplayOrder - formerly displayOrder - this is now the only operation needed 
        to display/change order information displayed on the mini
      * removeDisplayOrder - formerly displayOrderDelete
    * Removed 
      * displayOrderLineItemAdded - showDisplayOrder now handles this
      * displayOrderLineItemRemoved - showDisplayOrder now handles this
      * displayOrderDiscountAdded - showDisplayOrder now handles this
      * displayOrderDiscountRemoved - showDisplayOrder now handles this
  * ICloverConnectorListener (Notifications)
    * Added
      * onPaymentConfirmation - (REQUIRED) consists of a Payment and a list of challenges/void reasons  
      * onDeviceError - general callback when there is an error communicating with the device
      * onPrintManualRefundReceipt - if disablePrinting=true on the request, this will get called to indicate the POS can print this receipt
      * onPrintManualRefundDeclineReceipt - if disablePrinting=true on the request, this will get called to indicate the POS can print this receipt
      * onPrintPaymentReceipt - if disablePrinting=true on the request, this will get called to indicate the POS can print this receipt
      * onPrintPaymentDeclineReceipt - if disablePrinting=true on the request, this will get called to indicate the POS can print this receipt
      * onPrintPaymentMerchantCopyReceipt - if disablePrinting=true on the request, this will get called to indicate the POS can print this receipt
      * onPrintRefundPaymentReceipt - if disablePrinting=true on the request, this will get called to indicate the POS can print this receipt
      * onRetrievePendingPaymentsResponse - called with the list of payments taken on the device that aren't processed on the server yet
    * Renamed
      * onDeviceDisconnected - formerly onDisconnected
      * onDeviceConnected - formerly on onConnected
      * onDeviceReady - formerly onReady
      * onTipAdjustAuthResponse - formerly onAuthTipAdjustResponse
      * onCapturePreAuthResponse - formerly onPreAuthCaptureResponse
      * onVerifySignatureRequest - formerly onSignatureVerifyRequest
    * Removed
      * onTransactionState
      * onConfigErrorResponse - These are now processed as normal operation responses
      * onError - now handled by onDeviceError or through normal operation responses
      * onDebug
  * Request/Response Objects
    * Added
      * ConfirmPaymentRequest - Contains a Payment and a list of "challenges" from the 
        Clover device during payment operations, if there are questions for the merchant
        on their willingness to accept whatever risk is associated with that payment's 
        challenge. 
      * RetrievePendingPaymentsResponse - Contains a list of PendingPaymentEntry objects,
                                          which have the paymentId and amount for each 
                                          payment that has yet to be sent to the server
                                          for processing.
      * PrintManualRefundReceiptMessage - Contains the Credit object to be printed
      * PrintManualRefundDeclineReceiptMessage - Contains the declined Credit object to be printed 
      * PrintPaymentReceiptMessage - Contains the Order and Payment to be printed
      * PrintPaymentDeclineReceiptMessage - Contains the declined Payment and reason to be printed
      * PrintPaymentMerchantCopyReceiptMessage - Contains the payment to be printed
      * PrintRefundPaymentReceiptMessage - Contains Payment, Refund and Order
    * Renamed
      * VerifySignatureRequest - formerly SignatureVerifyRequest
      * CapturePreAuthRequest - formerly CaptureAuthRequest
      * VoidPaymentRequest - formerly VoidTransactionRequest
      * CloseoutRequest - formerly separate field-level parameters
      * TipAdjustAuthResponse - formerly AuthTipAdjustResponse
    * Removed
      * ConfigErrorResponse - These are now processed as normal operation responses
* All Response Messages now contain success(boolean), result, reason and message      
* voidPayment operation fix to verify connection status and check for void request
  acknowledgement from the Clover device prior to issuing a successful response
* Added DefaultCloverConnectorListener, which automatically accepts signature if a verify
  signature request is received
* Behavior change for RefundPaymentRequest - In the prior versions, a value of zero for 
  the amount field would trigger a refund of the full payment amount. With the 1.1 version, 
  passing zero in the amount field will trigger a validation failure. 
  Set fullRefund:boolean to `true` to specify a full refund. NOTE: This will attempt to refund 
  the original (full) payment amount, not the remaining amount, in a partial refund scenario.
* CloverConnecter now requires ApplicationId to be set via configuration of the 
  third party application. This is provided as part of the device configuration 
  that is passed in during the creation of the CloverConnector.  The String input parameter of
  "applicationId", which is passed in when instantiating the DefaultCloverDevice, should be 
  set using the format of <company specific package>:<version> e.g. com.clover.ExamplePOS:1.2
* SaleRequest, AuthRequest, PreAuthRequest and ManualRefund require ExternalId to be set.
  ExternalId should be unique per transaction request and will prevent the Clover device from
  re-processing the prior transaction if it has already been completed.  This is provided
  as a protection in the case where connectivity with the mini is temporarily interrupted
  and the calling POS system is unsure if the prior transaction finished.  Resubmission of the
  same request with the same external id will reject as a duplicate, if the device
  recognizes it as a valid previously processed operation.
  
  ## Working with the SDK
    
  ```
      ICloverConnect cloverConnector = new CloverConnector(new USBCloverDeviceConfiguration(getContext(), "com.yourcompany.app:2.1.1"));
      cloverConnector.addCloverConnectorListener(new DefaultCloverConnectorListener() {
          public void onSaleResponse(SaleResponse response) {
             if(response.isSuccess()) {
                // do something with response.getPayment()
             } else {
                // failure processing
                // look at response.getResult() to see the reason for the failure
             }
          }
          
          public void onConfirmPaymentRequest(ConfirmPaymentRequest request) {
              // will be called if needed by the device to
              // confirm a payment. e.g. offline, duplicate check, etc.
              boolean acceptPayment = false; // prompt user, config, etc.
              if(acceptPayment) {
                  cloverConnector.acceptPayment(request.getPayment());
              } else {
                  cloverConnector.rejectPayment(request.getPayment());
              }
          }
          
          // wait until this gets called to indicate the device
          // is ready to communicate
          public void onDeviceReady(MerchantInfo merchantInfo) {
              super.onDeviceReady(merchantInfo);
          }
      }
  
      cloverConnector.initializeConnection();
      ...
      // after the connector is ready
      if(cloverConnector.isReady()) {
          SaleRequest saleRequest = new SaleRequest(2215, "b1234"); // $22.15 with externalID "b1234"
          cloverConnector.sale(saleRequest);
      }
      
  ```

  
# Version 0.5
* Fix performance issue in USB connector
* Updated action of broadcast messages for USB connect/disconnect/ready
   * CloverTransport.DEVICE_DISCONNECTED, DEVICE_CONNECTED, DEVICE_READY
* added resetDevice() to CloverConnector, to reset the clover device state

# Version 0.4
* Add support for USB (USBCloverDeviceConfiguration)
* Add support for offline payments

# Version 0.3
* Updated support for externalPaymentId in SaleRequest, AuthRequest and PreAuthRequest
* Added closeout implementation
* Updated reconnect logic in WebSocketTransport
* Updated ExamplePOS App

# Version 0.2
* Update example POS app; should demonstrate all library functions now
* Update PreAuth, PreAuthCapture, and VaultCard methods in CloverConnector

# Version 0.1
* Initial capability

## Getting Connected (LAN Pay Display - experimental)

1. Make sure your Clover Mini Dev Kit and Android POS device are on the same network submask and have ports unblocked.
2. Download the Network Pay Display app from the Clover App Market on your Clover Mini Dev Kit.
3. Open the Network Pay Display app and you should see a web socket address.
4. Run the Clover Connector Android Example POS app on your Android POS device (emulator, device etc.)
5. Enter the web socket address from step 3. Tap 'OK' and go back.
6. You should see the example POS screen and connection state listed. If everything worked you'll get a connected status. If it remains disconnected, you'll want to do some network troubleshooting. Checking firewall ports and network submasks are good starting points.

