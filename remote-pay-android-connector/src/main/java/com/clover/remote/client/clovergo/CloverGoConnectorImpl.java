package com.clover.remote.client.clovergo;

import com.clover.remote.client.DefaultCloverConnector;
import com.clover.remote.client.MerchantInfo;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ResultCode;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.payments.CardTransaction;
import com.clover.sdk.v3.payments.CardType;
import com.clover.sdk.v3.payments.Payment;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;
import com.firstdata.clovergo.client.CloverGo;
import com.firstdata.clovergo.client.callback.CardReaderCallBack;
import com.firstdata.clovergo.client.callback.TransactionCallBack;
import com.firstdata.clovergo.client.event.CardReaderErrorEvent;
import com.firstdata.clovergo.client.event.CardReaderEvent;
import com.firstdata.clovergo.client.event.TransactionEvent;
import com.firstdata.clovergo.client.model.CardApplicationIdentifier;
import com.firstdata.clovergo.client.model.CloverGoConstants;
import com.firstdata.clovergo.client.model.CloverGoError;
import com.firstdata.clovergo.client.model.Order;
import com.firstdata.clovergo.client.model.ReaderInfo;
import com.firstdata.clovergo.client.model.TransactionResponse;

import java.util.ArrayList;
import java.util.List;

public class CloverGoConnectorImpl extends DefaultCloverConnector {
  private final CloverGo cloverGo;
  private final Context context;

  private Handler handler;

  private List<ReaderInfo> readersConnected = new ArrayList<>();

//  private String merchantId = "19TZTEEHW4TF4";
//  private String employeeId = "2ZYB6HYQ117GR";
//  private String clientId = "1AST2ETARGG7C";

  public CloverGoConnectorImpl(CloverGoDeviceConfiguration configuration) {
    this.context = configuration.getContext();
    cloverGo = new CloverGo.CloverGoBuilder(configuration.getContext(), configuration.getAccessToken(),
        CloverGoConstants.ENV.STAGING, configuration.getApiKey(),
        configuration.getSecret()).allowAutoConnect(true).build();

    HandlerThread handlerThread = new HandlerThread("CloverGoConnectorImpl thread");
    handlerThread.start();

    handler = new Handler(handlerThread.getLooper());
  }

  @Override
  public void initializeConnection() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        broadcaster.notifyOnConnect();

        // TODO: fetch merchant info
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
  public void sale(final SaleRequest request) {
    ReaderInfo readerInfo  = new ReaderInfo();
    readerInfo.setReaderType(CloverGoConstants.CARD_READER_TYPE.RP450X);
    //cloverGo.releaseReader(readerInfo);
    cloverGo.useReader(readerInfo, new MyCardReaderCallBack() {
      @Override
      public void onCardReaderDiscovered(ReaderInfo readerInfo) {
        super.onCardReaderDiscovered(readerInfo);
        //if (readersConnected.contains(readerInfo)) {
          cloverGo.connectToBleReader(readerInfo);
          readersConnected.add(readerInfo);
        //}
      }

      @Override
      public void onConnected(ReaderInfo readerInfo) {
        super.onConnected(readerInfo);
        cloverGo.stopReaderScan();
      }

      @Override
      public void onDisconnected(ReaderInfo readerInfo) {
        super.onDisconnected(readerInfo);
        readersConnected.remove(readerInfo);
      }

      @Override
      public void onReady(ReaderInfo readerInfo) {
        super.onReady(readerInfo);

        CloverDeviceEvent deviceEvent = new CloverDeviceEvent(0, "Clover Go device ready");
        broadcaster.notifyOnDeviceActivityStart(deviceEvent);

        // TODO: determine how we can avoid creating the order
        Order order = new Order();
        order.addCustomItem(new Order.CustomItem("Custom Item", request.getAmount(), 1));

        cloverGo.doReaderTransaction(readerInfo, order, new MyTransactionCallback() {
          @Override
          public void onProgress(CardReaderEvent cardReaderEvent) {
            super.onProgress(cardReaderEvent);
            CloverDeviceEvent deviceEvent = new CloverDeviceEvent(0, cardReaderEvent.getEventInfo());
            deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.PROCESSING);
            broadcaster.notifyOnDeviceActivityStart(deviceEvent);
          }

          @Override
              public void onError(CloverGoError cloverGoError) {
                super.onError(cloverGoError);
                String msg = cloverGoError.getCode() != null ? cloverGoError.getCode() : "";
                msg += cloverGoError.getMessage() != null ? cloverGoError.getMessage() : "";
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
              }

              @Override
              public void onSuccess(TransactionResponse transactionResponse) {
                super.onSuccess(transactionResponse);
                SaleResponse saleResponse = new SaleResponse(true, ResultCode.SUCCESS);

                Payment payment = new Payment();
                CardTransaction cardTransaction = new CardTransaction();
                Reference orderRef = new com.clover.sdk.v3.base.Reference();
                orderRef.setId(transactionResponse.getOrderId());

                payment.setId(transactionResponse.getTransactionId());
                payment.setAmount((long) transactionResponse.getAmountCharged());
                payment.setExternalPaymentId(transactionResponse.getExternalPaymentId());
                cardTransaction.setCardholderName(transactionResponse.getCardholderName());
                cardTransaction.setCardType(CardType.valueOf(transactionResponse.getCardType()));

                payment.setCardTransaction(cardTransaction);
                payment.setOrder(orderRef);
                saleResponse.setPayment(payment);
                broadcaster.notifyOnSaleResponse(saleResponse);

                CloverDeviceEvent deviceEvent = new CloverDeviceEvent(0, "Remove card");
                deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.REMOVE_CARD);
                broadcaster.notifyOnDeviceActivityStart(deviceEvent);
              }
            }

        );
      }
    });
    cloverGo.scanForReaders();
  }

  private class MyTransactionCallback implements TransactionCallBack {
    @Override
    public void onProgress(CardReaderEvent cardReaderEvent) {
      Log.d("GoTransactionCallBack", "onProgress: cardReaderEvent = " + cardReaderEvent);
    }

    @Override
    public void onAidMatch(List<CardApplicationIdentifier> list, AidSelection aidSelection) {
      Log.d("GoTransactionCallBack", "onAidMatch");
      for (CardApplicationIdentifier aid : list) {
        Log.d("GoTransactionCallBack", aid.getApplicationIdentifier());
      }
    }

    @Override
    public boolean proceedOnError(TransactionEvent transactionEvent, ProceedOnError proceedOnError) {
      return false;
    }

    @Override
    public void onSuccess(TransactionResponse transactionResponse) {
      Log.d("GoTransactionCallBack", "onProgress: onSuccess = " + transactionResponse);
    }

    @Override
    public void onError(CloverGoError cloverGoError) {
      Log.d("GoTransactionCallBack", "onError: error = " + cloverGoError);
    }
  }

  private class MyCardReaderCallBack implements CardReaderCallBack {
    public MyCardReaderCallBack() {
    }

    @Override
    public void onConnected(ReaderInfo readerInfo) {
      Log.d("GoCardReaderCallBack", "onConnected: readerInfo = " + readerInfo);
    }

    @Override
    public void onDisconnected(ReaderInfo readerInfo) {
      Log.d("GoCardReaderCallBack", "onDisconnected");
    }

    @Override
    public void onError(CardReaderErrorEvent cardReaderErrorEvent) {
      Log.d("GoCardReaderCallBack", "onError: " + cardReaderErrorEvent.getEventInfo());
    }

    @Override
    public void onReady(ReaderInfo readerInfo) {
      Log.d("GoCardReaderCallBack", "onReady: readerInfo = " + readerInfo);
    }

    @Override
    public void onReaderResetProgress(CardReaderEvent cardReaderEvent) {
      Log.d("GoCardReaderCallBack", "onReaderResetProgress");
    }

    @Override
    public void onCardReaderDiscovered(ReaderInfo readerInfo) {
      Log.d("GoCardReaderCallBack", "onCardReaderDiscovered: readerInfo = " + readerInfo);
    }
  }
}
