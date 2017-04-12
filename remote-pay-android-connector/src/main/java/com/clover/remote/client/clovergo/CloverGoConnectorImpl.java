package com.clover.remote.client.clovergo;

import com.clover.remote.client.DefaultCloverConnector;
import com.clover.remote.client.MerchantInfo;
import com.clover.remote.client.messages.ResultCode;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.payments.CardTransaction;
import com.clover.sdk.v3.payments.CardType;
import com.clover.sdk.v3.payments.Payment;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.firstdata.clovergo.client.CloverGo;
import com.firstdata.clovergo.client.callback.TransactionCallBack;
import com.firstdata.clovergo.client.event.CardReaderEvent;
import com.firstdata.clovergo.client.event.TransactionEvent;
import com.firstdata.clovergo.client.model.CardApplicationIdentifier;
import com.firstdata.clovergo.client.model.CloverGoConstants;
import com.firstdata.clovergo.client.model.CloverGoError;
import com.firstdata.clovergo.client.model.KeyedTransactionRequest;
import com.firstdata.clovergo.client.model.Order;
import com.firstdata.clovergo.client.model.TransactionResponse;

import java.util.List;

public class CloverGoConnectorImpl extends DefaultCloverConnector {
  private final CloverGo cloverGo;

  private Handler handler;
  private HandlerThread handlerThread;

//  private String merchantId = "19TZTEEHW4TF4";
//  private String employeeId = "2ZYB6HYQ117GR";
//  private String clientId = "1AST2ETARGG7C";

  public CloverGoConnectorImpl(CloverGoDeviceConfiguration configuration) {
    cloverGo = new CloverGo.CloverGoBuilder(configuration.getContext(), configuration.getAccessToken(),
        CloverGoConstants.ENV.STAGING, configuration.getApiKey(),
        configuration.getSecret()).allowAutoConnect(true).build();
  }

  @Override
  public void initializeConnection() {
    handlerThread = new HandlerThread("CloverGoConnectorImpl thread");
    handlerThread.start();

    handler = new Handler(handlerThread.getLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        broadcaster.notifyOnConnect();

        MerchantInfo merchantInfo = new MerchantInfo("19TZTEEHW4TF4");
        broadcaster.notifyOnReady(merchantInfo);
      }
    });
  }

  @Override
  public void dispose() {
    // nothing
  }

  @Override
  public void sale(SaleRequest request) {
//    static final String CC_NUMBER = "4005571701111111";
//    static final String CARDHOLDER_NAME = "CLOVER USER";
//    static final String CVV_NUMBER = "1111";
//    static final int EXP_MONTH = 4;
//    static final int EXP_YEAR = 2020;

    Order order = new Order();
    order.addCustomItem(new Order.CustomItem("Custom Item", request.getAmount(), 1));

    KeyedTransactionRequest keyedTransactionRequest =
        new KeyedTransactionRequest("4111111111111111", "0420", "1234", order);
    cloverGo.doKeyedTransaction(keyedTransactionRequest, new TransactionCallBack() {
      @Override
      public void onProgress(CardReaderEvent cardReaderEvent) {

      }

      @Override
      public void onAidMatch(List<CardApplicationIdentifier> list, AidSelection aidSelection) {

      }

      @Override
      public boolean proceedOnError(TransactionEvent transactionEvent, ProceedOnError proceedOnError) {
        return false;
      }

      @Override
      public void onSuccess(TransactionResponse transactionResponse) {
        SaleResponse saleResponse = new SaleResponse(true, ResultCode.SUCCESS);

        Payment payment = new Payment();
        CardTransaction cardTransaction = new CardTransaction();
        Reference orderRef = new com.clover.sdk.v3.base.Reference();
        orderRef.setId(transactionResponse.getOrderId());

        payment.setId(transactionResponse.getTransactionId());
        payment.setAmount((long)transactionResponse.getAmountCharged());
        payment.setExternalPaymentId(transactionResponse.getExternalPaymentId());
        cardTransaction.setCardholderName(transactionResponse.getCardholderName());
        cardTransaction.setCardType(CardType.valueOf(transactionResponse.getCardType()));

        payment.setCardTransaction(cardTransaction);
        payment.setOrder(orderRef);
        saleResponse.setPayment(payment);
        broadcaster.notifyOnSaleResponse(saleResponse);
      }

      @Override
      public void onError(CloverGoError cloverGoError) {
        Log.e("CloverGoConnectorImpl", cloverGoError.getMessage() != null ? cloverGoError.getMessage() : "Unknown error message");
        Log.e("CloverGoConnectorImpl", cloverGoError.getCode() != null ? cloverGoError.getCode() : "Unknown error code");
        broadcaster.notifyOnSaleResponse(new SaleResponse(false, ResultCode.ERROR));
      }
    });
  }
}
