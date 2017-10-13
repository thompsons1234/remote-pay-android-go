package com.clover.remote.client.clovergo;

import android.text.TextUtils;
import android.util.Log;

import com.clover.remote.Challenge;
import com.clover.remote.client.MerchantInfo;
import com.clover.remote.client.clovergo.di.DaggerApplicationComponent;
import com.clover.remote.client.clovergo.messages.BillingAddress;
import com.clover.remote.client.clovergo.messages.GoPayment;
import com.clover.remote.client.clovergo.messages.KeyedAuthRequest;
import com.clover.remote.client.clovergo.messages.KeyedPreAuthRequest;
import com.clover.remote.client.clovergo.messages.KeyedSaleRequest;
import com.clover.remote.client.lib.BuildConfig;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CapturePreAuthRequest;
import com.clover.remote.client.messages.CapturePreAuthResponse;
import com.clover.remote.client.messages.CardApplicationIdentifier;
import com.clover.remote.client.messages.CloseoutRequest;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
import com.clover.remote.client.messages.PreAuthRequest;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.RefundPaymentRequest;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.ResultCode;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.TipAdjustAuthRequest;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.TransactionRequest;
import com.clover.remote.client.messages.VoidPaymentRequest;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.CardTransaction;
import com.clover.sdk.v3.payments.CardTransactionType;
import com.clover.sdk.v3.payments.CardType;
import com.clover.sdk.v3.payments.Result;
import com.firstdata.clovergo.data.DaggerSDKDataComponent;
import com.firstdata.clovergo.data.module.NetworkModule;
import com.firstdata.clovergo.data.module.UtilityModule;
import com.firstdata.clovergo.domain.model.CreditCard;
import com.firstdata.clovergo.domain.model.EmployeeMerchant;
import com.firstdata.clovergo.domain.model.EmvCard;
import com.firstdata.clovergo.domain.model.Error;
import com.firstdata.clovergo.domain.model.Order;
import com.firstdata.clovergo.domain.model.Payment;
import com.firstdata.clovergo.domain.model.ReaderError;
import com.firstdata.clovergo.domain.model.ReaderInfo;
import com.firstdata.clovergo.domain.model.ReaderProgressEvent;
import com.firstdata.clovergo.domain.model.RefundSuccess;
import com.firstdata.clovergo.domain.model.TaxRate;
import com.firstdata.clovergo.domain.model.TransactionError;
import com.firstdata.clovergo.domain.rx.EventBus;
import com.firstdata.clovergo.domain.rx.ReusableObserver;
import com.firstdata.clovergo.domain.usecase.AddTips;
import com.firstdata.clovergo.domain.usecase.AuthOrSaleTransaction;
import com.firstdata.clovergo.domain.usecase.CancelCardRead;
import com.firstdata.clovergo.domain.usecase.CaptureSignature;
import com.firstdata.clovergo.domain.usecase.CaptureTransaction;
import com.firstdata.clovergo.domain.usecase.CloseOut;
import com.firstdata.clovergo.domain.usecase.ConnectToReader;
import com.firstdata.clovergo.domain.usecase.DisconnectReader;
import com.firstdata.clovergo.domain.usecase.GetConnectedReaders;
import com.firstdata.clovergo.domain.usecase.GetMerchantsInfoOAuth;
import com.firstdata.clovergo.domain.usecase.PreAuthTransaction;
import com.firstdata.clovergo.domain.usecase.ReadCard;
import com.firstdata.clovergo.domain.usecase.RefundTransaction;
import com.firstdata.clovergo.domain.usecase.ScanForReaders;
import com.firstdata.clovergo.domain.usecase.SendReceipt;
import com.firstdata.clovergo.domain.usecase.VoidTransaction;
import com.firstdata.clovergo.domain.usecase.WriteToCard;
import com.firstdata.clovergo.reader.module.ReaderModule;
import com.google.android.gms.iid.InstanceID;
import com.roam.roamreaderunifiedapi.utils.HexUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static com.firstdata.clovergo.domain.model.ReaderProgressEvent.EventType.EMV_DATA;
import static com.firstdata.clovergo.domain.model.ReaderProgressEvent.EventType.SWIPE_DATA;

/**
 * Created by Akhani, Avdhesh on 5/22/17.
 */
public class CloverGoConnectorImpl {
  private final static String TAG = "CloverGO ConnectorImpl";

  private final CloverGoConnectorBroadcaster mBroadcaster;
  private Disposable mScanDisposable;

  private ReusableObserver<EmployeeMerchant> mEmployeeMerchantObserver;
  private ReusableObserver<ReaderProgressEvent> mProgressObserver;
  private ReusableObserver<Payment> mPaymentObserver;
  private ReusableObserver<ReaderError> mReaderErrorObserver;
  private ReusableObserver<ReaderInfo> mReaderInfoObserver;

  private ReaderProgressEvent mReaderProgressEvent;
  private ReaderInfo mLastTransactionReader;
  private TransactionRequest mLastTransactionRequest;

  @Inject
  AuthOrSaleTransaction mAuthOrSaleTransaction;
  @Inject
  PreAuthTransaction mPreAuthTransaction;
  @Inject
  CaptureTransaction mCaptureTransaction;
  @Inject
  AddTips mAddTips;
  @Inject
  VoidTransaction mVoidTransaction;
  @Inject
  CloseOut mCloseOut;
  @Inject
  RefundTransaction mRefundTransaction;
  @Inject
  CaptureSignature mCaptureSignature;
  @Inject
  SendReceipt mSendReceipt;
  @Inject
  GetMerchantsInfoOAuth mGetMerchantsInfoOAuth;

  @Inject
  ScanForReaders mScanForReaders;
  @Inject
  ConnectToReader mConnectToReader;
  @Inject
  GetConnectedReaders mGetConnectedReaders;
  @Inject
  ReadCard mReadCard;
  @Inject
  WriteToCard mWriteToCard;
  @Inject
  DisconnectReader disconnectReader;
  @Inject
  CancelCardRead cancelCardRead;

  private Order mOrder;
  private Payment mPayment;
  private CreditCard mCreditCard;

  private TransactionError mTransactionError;
  private EmployeeMerchant mEmployeeMerchant;


  public CloverGoConnectorImpl(final CloverGoConnectorBroadcaster broadcaster, CloverGoDeviceConfiguration configuration) {
    Log.d(TAG, "CloverGoConnectorImpl env=" + configuration.getEnv());
    mBroadcaster = broadcaster;

    CloverGoDeviceConfiguration.ENV env = configuration.getEnv();
    String url;
    switch (env) {
      case LIVE:
        url = "https://api.payeezy.com/clovergosdk/v1/";
        break;
      case SANDBOX:
        url = "https://api-cert.payeezy.com/clovergosdk/v1/";
        break;
      case SANDBOX_DEV:
        url = "https://api-cert.payeezy.com/clovergosdk/v1/";
        break;
      default: // DEMO is default
        url = "https://api-cat.payeezy.com/clovergosdk/v1/";
    }

    DaggerApplicationComponent.builder().sDKDataComponent(DaggerSDKDataComponent.builder().readerModule(new ReaderModule(configuration.getContext())).
            networkModule(new NetworkModule(url, configuration.getApiKey(), configuration.getSecret(), configuration.getAccessToken(), InstanceID.getInstance(configuration.getContext()).getId(), BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME)).
            utilityModule(new UtilityModule(configuration.getContext())).build()).build().inject(this);

    initObservers();

    // Pull merchant details from the backend if it's not already in the cache
    mGetMerchantsInfoOAuth.getCachedObservable().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mEmployeeMerchantObserver);

    EventBus.getObservable().ofType(ReaderInfo.class).subscribe(mReaderInfoObserver);
    EventBus.getObservable().ofType(ReaderError.class).subscribe(mReaderErrorObserver);
    EventBus.getObservable().ofType(ReaderProgressEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(mProgressObserver);
  }

  private void initObservers() {
    Log.d(TAG, "initObservers");
    mEmployeeMerchantObserver = new ReusableObserver<EmployeeMerchant>() {
      @Override
      public void onSubscribe(Disposable d) {
        Log.d(TAG, "mEmployeeMerchantObserver subscribe");
        super.onSubscribe(d);
        mBroadcaster.notifyOnGetMerchantInfo();
      }

      @Override
      public void onNext(EmployeeMerchant employeeMerchant) {
        Log.d(TAG, "mEmployeeMerchantObserver next");
        mEmployeeMerchant = employeeMerchant;
        mBroadcaster.notifyOnGetMerchantInfoResponse(true);
      }

      @Override
      public void onError(Throwable e) {
        Log.d(TAG, "mEmployeeMerchantObserver error");
        super.onError(e);
        mBroadcaster.notifyOnGetMerchantInfoResponse(false);
      }
    };

    // Observer for handling sale/transaction success & error response
    mPaymentObserver = new ReusableObserver<Payment>() {
      @Override
      public void onNext(Payment payment) {
        Log.d(TAG, "mPaymentObserver next");
        mPayment = payment;

        if ("EMV_CONTACT".equalsIgnoreCase(mPayment.getCard().getMode())) {
          Log.d(TAG, "mPaymentObserver next EMV_CONTACT");
          Map<String, String> data = new HashMap<>();

          if (mPayment.getCard().getExtra() != null) {
            Log.d(TAG, "mPaymentObserver next EMV_CONTACT extra");
            data.put("Authorization_Response", "3130".equalsIgnoreCase(mPayment.getCard().getExtra().getAuthResponseCode()) ? "3030" : mPayment.getCard().getExtra().getAuthResponseCode());
            data.put("Issuer_Auth_Data", mPayment.getCard().getExtra().getIssuerAuthData());
            data.put("Auth_Code", HexUtils.convertASCII2HexaDecimal(mPayment.getCard().getExtra().getAuthCode()));
            data.put("Result", "01");
            data.put("issuer_script_template1", mPayment.getCard().getExtra().getIssuerScriptTemplate1());
            data.put("issuer_script_template2", mPayment.getCard().getExtra().getIssuerScriptTemplate2());

          } else {
            Log.d(TAG, "mPaymentObserver next EMV_CONTACT non-extra");
            data.put("Result", "01");
            data.put("Auth_Code", HexUtils.convertASCII2HexaDecimal(mPayment.getAuthCode()));
            data.put("Authorization_Response", "3030");

          }
          mWriteToCard.getObservable(mLastTransactionReader, data).subscribe();

        } else {
          Log.d(TAG, "mPaymentObserver next non-EMV_CONTACT");
          notifyPaymentResponse();

        }
      }

      @Override
      public void onError(Throwable e) {
        Log.d(TAG, "mPaymentObserver error");
        mTransactionError = TransactionError.convertToError(e);

        if ("duplicate_transaction".equals(mTransactionError.getCode())) {
          ConfirmPaymentRequest confirmPaymentRequest = new ConfirmPaymentRequest();
          Challenge[] challenge = {new Challenge(mTransactionError.getMessage(), Challenge.ChallengeType.DUPLICATE_CHALLENGE, VoidReason.REJECT_DUPLICATE)};
          confirmPaymentRequest.setChallenges(challenge);
          mBroadcaster.notifyOnConfirmPaymentRequest(confirmPaymentRequest);
          return;
        } else if (("charge_declined".equals(mTransactionError.getCode()) || "charge_declined_referral".equals(mTransactionError.getCode())) && mReaderProgressEvent != null && mReaderProgressEvent.getEventType() == ReaderProgressEvent.EventType.EMV_DATA) {
          mGetConnectedReaders.getBlockingObservable().subscribe(new Consumer<ReaderInfo>() {
            @Override
            public void accept(@NonNull ReaderInfo readerInfo) throws Exception {
              final Map<String, String> completeEmvData = new HashMap<>();
              if (mTransactionError.getClientData() != null && mTransactionError.getClientData().getAuthResponseCode() != null && !mTransactionError.getClientData().getAuthResponseCode().isEmpty()) {
                completeEmvData.put("Result", "01");
                completeEmvData.put("Authorization_Response", mTransactionError.getClientData().getAuthResponseCode());
                completeEmvData.put("Issuer_Auth_Data", mTransactionError.getClientData().getIssuerAuthData());
                completeEmvData.put("Auth_Code", HexUtils.convertASCII2HexaDecimal(TextUtils.isEmpty(mTransactionError.getClientData().getAuthCode()) ? "123456" : mTransactionError.getClientData().getAuthCode()));
                completeEmvData.put("issuer_script_template1", mTransactionError.getClientData().getIssuerScriptTemplate1());
                completeEmvData.put("issuer_script_template2", mTransactionError.getClientData().getIssuerScriptTemplate2());
              } else {
                completeEmvData.put("Result", "01");
                completeEmvData.put("Authorization_Response", "3035");
                completeEmvData.put("Auth_Code", HexUtils.convertASCII2HexaDecimal("123456"));
              }
              mWriteToCard.getObservable(readerInfo, completeEmvData).subscribe();
            }
          });
          return;
        }
        notifyErrorResponse();

      }
    };

    mReaderInfoObserver = new ReusableObserver<ReaderInfo>() {
      @Override
      public void onNext(final ReaderInfo readerInfo) {
        Log.d(TAG, "mReaderInfoObserver next");
        if (readerInfo.isConnected()) {
          Log.d(TAG, "mReaderInfoObserver next connected " + String.valueOf(mEmployeeMerchant == null));

          if (mEmployeeMerchant == null) {
            mGetMerchantsInfoOAuth.getCachedObservable().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<EmployeeMerchant>() {
              @Override
              public void onSubscribe(Disposable d) {
              }

              @Override
              public void onNext(EmployeeMerchant employeeMerchant) {
                mBroadcaster.notifyOnGetMerchantInfoResponse(true);
                mEmployeeMerchant = employeeMerchant;
                mBroadcaster.notifyOnReady(getMerchantInfo(readerInfo, employeeMerchant));
              }

              @Override
              public void onError(Throwable e) {
                mBroadcaster.notifyOnGetMerchantInfoResponse(false);
                disconnectDevice(readerInfo.getReaderType());
                mBroadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, e.getMessage()));
              }

              @Override
              public void onComplete() {
              }
            });
          } else {
            mBroadcaster.notifyOnReady(getMerchantInfo(readerInfo, mEmployeeMerchant));
          }
        } else {
          Log.d(TAG, "mReaderInfoObserver next not connected");
          mBroadcaster.notifyOnDisconnect(readerInfo);
        }
      }
    };

    mProgressObserver = new ReusableObserver<ReaderProgressEvent>() {
      @Override
      public void onNext(ReaderProgressEvent readerProgressEvent) {
        EmvCard emvCard = null;
        CloverDeviceEvent deviceEvent;

        Log.d(TAG, "mProgressObserver next " + readerProgressEvent.getEventType() + " " + readerProgressEvent.getMessage());

        switch (readerProgressEvent.getEventType()) {
          case CARD_INSERTED:
            deviceEvent = new CloverDeviceEvent();
            deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.CARD_INSERTED_MSG);
            deviceEvent.setMessage("Processing Transaction, Leave card inserted");
            mBroadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);
            break;
          case CARD_TAPPED:
            deviceEvent = new CloverDeviceEvent();
            deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.CARD_TAPPED);
            deviceEvent.setMessage("Processing transaction");
            mBroadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);
            break;
          case EMV_COMPLETE:
            deviceEvent = new CloverDeviceEvent();
            deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.EMV_COMPLETE_DATA);
            deviceEvent.setMessage("Please remove card");
            mBroadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);

            if (!(readerProgressEvent.getData().get("status").equalsIgnoreCase("Success") && "TC".equalsIgnoreCase(readerProgressEvent.getData().get("cryptogram_information_data")))) {
              Log.d(TAG, "mProgressObserver next " + readerProgressEvent.getEventType() + " success and tc crypto");

              if (mPayment != null) {
                Log.d(TAG, "mProgressObserver next " + readerProgressEvent.getEventType() + " success and tc crypto and payment not null");
                mVoidTransaction.getObservable(mPayment.getOrderId(), mPayment.getPaymentId()).subscribeOn(Schedulers.io()).subscribe();
                mPayment = null;
                mTransactionError = new TransactionError("chip_decline", "Transaction declined - Chip Decline");
              }
            }
            break;
          case CARD_REMOVED:
            deviceEvent = new CloverDeviceEvent();
            deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.CARD_REMOVED_MSG);
            deviceEvent.setMessage("Card Removed");
            mBroadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);

            if (mPayment != null) {
              Log.d(TAG, "mProgressObserver next " + readerProgressEvent.getEventType() + " payment not null");
              notifyPaymentResponse();
            } else if (mTransactionError != null) {
              Log.d(TAG, "mProgressObserver next " + readerProgressEvent.getEventType() + " transaction error not null");
              notifyErrorResponse();
            } else {
              Log.d(TAG, "mProgressObserver next " + readerProgressEvent.getEventType() + " error");
              mTransactionError = new TransactionError("unknown_error", "unknown_error");
              notifyErrorResponse();
            }
            break;
          case LIST_APPLICATION_IDENTIFIERS:
            Log.d(TAG, "mProgressObserver next " + readerProgressEvent.getEventType() + " " + readerProgressEvent.getData());
            Set<String> keySet = readerProgressEvent.getData().keySet();
            List<CardApplicationIdentifier> applicationIdentifiers = new ArrayList<CardApplicationIdentifier>();

            Log.d(TAG, "mProgressObserver next " + readerProgressEvent.getEventType() + " keyset size=" + keySet.size());
            for (String key : keySet) {
              CardApplicationIdentifier cardApplicationIdentifier = new CardApplicationIdentifier();
              cardApplicationIdentifier.setApplicationLabel(key);
              cardApplicationIdentifier.setApplicationIdentifier(readerProgressEvent.getData().get(key));
              applicationIdentifiers.add(cardApplicationIdentifier);
            }
            mBroadcaster.notifyOnAidMatch(applicationIdentifiers, new ICloverGoConnectorListener.AidSelection() {
              @Override
              public void selectApplicationIdentifier(final CardApplicationIdentifier selectedCardApplicationIdentifier) {
                mGetConnectedReaders.getBlockingObservable().filter(new Predicate<ReaderInfo>() {
                  @Override
                  public boolean test(@NonNull ReaderInfo readerInfo) throws Exception {
                    return readerInfo.isConnected();
                  }
                }).flatMapCompletable(new Function<ReaderInfo, CompletableSource>() {
                  @Override
                  public CompletableSource apply(@NonNull ReaderInfo readerInfo) throws Exception {
                    Log.d(TAG, "mProgressObserver next LIST_APPLICATION_IDENTIFIERS readerCard");
                    return mReadCard.getObservable(readerInfo, selectedCardApplicationIdentifier.getApplicationIdentifier(), 0);
                  }
                }).onErrorComplete().subscribe();
              }
            });
            break;
          case COMMON_APPLICATION_IDENTIFIER:
            Iterator<String> it = readerProgressEvent.getData().keySet().iterator();
            String aid = null;
            if (it.hasNext())
              aid = readerProgressEvent.getData().get(it.next());

            Log.d(TAG, "mProgressObserver next " + readerProgressEvent.getEventType() + " aid=" + aid);
            mReadCard.getObservable(mLastTransactionReader, aid, 0).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
            break;

          case CUSTOMER_VALIDATION_REQ:
            // TODO:IAN look into this...
//            deviceEvent = new CloverDeviceEvent();
//            deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.PLEASE_SEE_PHONE_MSG); // check if correct event state used
//            deviceEvent.setMessage("Card Removed");
//            mBroadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);
            break;

          case SWIPE_DATA:
            Map<String, String> swipeData = readerProgressEvent.getData();
            emvCard = new EmvCard(swipeData.get("ksn"), swipeData.get("encryptedTrack"), swipeData.get("track1"), swipeData.get("track2"), swipeData.get("pan"));
            deviceEvent = new CloverDeviceEvent();
            deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.CARD_SWIPED);
            deviceEvent.setMessage("Processing transaction");
            mBroadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);
            break;

          case EMV_DATA:
            Map<String, String> chipData = readerProgressEvent.getData();
            emvCard = new EmvCard(chipData);
            break;

          case CANCEL_CARD_READ:
            deviceEvent = new CloverDeviceEvent();
            deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.CANCEL_CARD_READ);
            deviceEvent.setMessage("Card Reader Transaction Cancelled");
            mBroadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);
            break;
        }

        if (readerProgressEvent.getEventType() == SWIPE_DATA || readerProgressEvent.getEventType() == EMV_DATA) {
          Log.d(TAG, "mProgressObserver next SWIPE_DATA/EMV_DATE confirmed");
          mPayment = null;
          mTransactionError = null;
          CloverGoConnectorImpl.this.mReaderProgressEvent = readerProgressEvent;

          if (mLastTransactionRequest instanceof SaleRequest) {
            Log.d(TAG, "mProgressObserver next SWIPE_DATA/EMV_DATE SaleRequest");
            mAuthOrSaleTransaction.getObservable(mOrder, emvCard).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentObserver);
          } else if (mLastTransactionRequest instanceof AuthRequest) {
            Log.d(TAG, "mProgressObserver next SWIPE_DATA/EMV_DATE AuthRequest");
            mAuthOrSaleTransaction.getObservable(mOrder, emvCard).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentObserver);
          } else if (mLastTransactionRequest instanceof PreAuthRequest) {
            Log.d(TAG, "mProgressObserver next SWIPE_DATA/EMV_DATE PreAuthRequest");
            mPreAuthTransaction.getObservable(mOrder, emvCard).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentObserver);
          }
        }
      }
    };

    mReaderErrorObserver = new ReusableObserver<ReaderError>() {
      @Override
      public void onNext(ReaderError readerError) {
        Log.d(TAG, "mReaderErrorObserver next " + readerError.getErrorType() + " " + readerError.getMessage());
        mReaderProgressEvent = null;
        CloverDeviceErrorEvent deviceErrorEvent = null;

        switch (readerError.getErrorType()) {
          case EMV_CARD_SWIPED_ERROR:
            deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.EMV_CARD_SWIPED_ERROR, 0, null, "Please insert card");
            mBroadcaster.notifyOnDeviceError(deviceErrorEvent);
            break;
          case CONTACT_LESS_FAILED_TRY_CONTACT_ERROR:
            deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.CONTACT_LESS_FAILED_TRY_CONTACT_ERROR, 0, null, "Contactless failed " + "Please insert/swipe card");
            mBroadcaster.notifyOnDeviceError(deviceErrorEvent);
            break;
          case DIP_FAILED_ALL_ATTEMPTS_ERROR:
            deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.DIP_FAILED_ALL_ATTEMPTS_ERROR, 0, null, readerError.getMessage());
            mBroadcaster.notifyOnDeviceError(deviceErrorEvent);
            break;
          case DIP_FAILED_ERROR:
            deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.DIP_FAILED_ERROR, 0, null, readerError.getMessage());
            mBroadcaster.notifyOnDeviceError(deviceErrorEvent);
            break;
          case SWIPE_FAILED_ERROR:
            deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.SWIPE_FAILED_ERROR, 0, null, "Please swipe again");
            mBroadcaster.notifyOnDeviceError(deviceErrorEvent);
            break;
          case READER_NOT_CONNECTED:
            deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.READER_NOT_CONNECTED, 0, null, "Reader not connected");
            mBroadcaster.notifyOnDeviceError(deviceErrorEvent);
            break;
          case MULTIPLE_CONTACT_LESS_CARD_DETECTED_ERROR:
            deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.MULTIPLE_CONTACT_LESS_CARD_DETECTED_ERROR, 0, null, "Multiple Contactless card Detected");
            mBroadcaster.notifyOnDeviceError(deviceErrorEvent);
            break;
          case READER_ERROR:
            deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.READER_ERROR, 0, null, readerError.getMessage());
            mBroadcaster.notifyOnDeviceError(deviceErrorEvent);
            break;
          case READER_TIMEOUT:
            deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.READER_TIMEOUT, 0, null, "Reader Transaction Time Out, Start Transaction Again");
            mBroadcaster.notifyOnDeviceError(deviceErrorEvent);
            break;
          case CARD_ERROR:
            deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.CARD_ERROR, 0, null, readerError.getMessage());
            mBroadcaster.notifyOnDeviceError(deviceErrorEvent);
            break;
          case LOW_BATTERY:
            deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.LOW_BATTERY, 0, null, "Reader Battery low");
            mBroadcaster.notifyOnDeviceError(deviceErrorEvent);
            break;
        }
      }
    };
  }

  private MerchantInfo getMerchantInfo(ReaderInfo readerInfo, EmployeeMerchant employeeMerchant) {
    boolean supportsSales = false;
    boolean supportAuths = false;
    boolean supportsPreAuths = false;
    boolean supportsVaultCards = false;
    boolean supportsManualRefunds = false;
    boolean supportsVoids = false;
    boolean supportsTipAdjust = false;
    for (String feature : mEmployeeMerchant.getMerchant().getFeatures()) {
      switch (feature) {
        case "auths":
          supportAuths = true;
          break;
        case "manualRefunds":
          supportsManualRefunds = true;
          break;
        case "preAuths":
          supportsPreAuths = true;
          break;
        case "sales":
          supportsSales = true;
          break;
        case "tip_adjust":
          supportsTipAdjust = true;
          break;
        case "vaultCards":
          supportsVaultCards = true;
          break;
        case "voids":
          supportsVoids = true;
          break;

      }
    }

    return new MerchantInfo(mEmployeeMerchant.getMerchant().getId(), mEmployeeMerchant.getMerchant().getName(), supportsSales, supportAuths, supportsPreAuths, supportsVaultCards, supportsManualRefunds, supportsVoids, supportsTipAdjust, readerInfo.getBluetoothName(), readerInfo.getSerialNo(), readerInfo.getReaderType().name());

  }

  public void initializeConnection(ReaderInfo.ReaderType readerType) {
    Log.d(TAG, "initializeConnection " + readerType.name());
    if (readerType == ReaderInfo.ReaderType.RP450) {
      mScanForReaders.getObservable(null, 15000).subscribe(new Observer<ReaderInfo>() {
        @Override
        public void onSubscribe(Disposable d) {
          mScanDisposable = d;
          Log.d(TAG, "initializeConnection subscribe");
        }

        @Override
        public void onNext(ReaderInfo readerInfo) {
          Log.d(TAG, "initializeConnection next");
          mBroadcaster.notifyOnDiscovered(readerInfo);
        }

        @Override
        public void onError(Throwable e) {
          Log.d(TAG, "initializeConnection error");
        }

        @Override
        public void onComplete() {
          Log.d(TAG, "initializeConnection complete");
        }
      });
    } else if (readerType == ReaderInfo.ReaderType.RP350) {
      ReaderInfo readerInfo = ReaderInfo.createRP350ReaderType();
      mConnectToReader.getObservable(readerInfo.getReaderType(), readerInfo.getBluetoothName(), readerInfo.getBluetoothIdentifier()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }
  }

  public void connectToDevice(ReaderInfo readerInfo) {
    Log.d(TAG, "connectToBluetoothDevice");
    stopDeviceScan();
    mConnectToReader.getObservable(readerInfo.getReaderType(), readerInfo.getBluetoothName(), readerInfo.getBluetoothIdentifier()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
  }

  public void disconnectDevice(ReaderInfo.ReaderType readerType) {
    Log.d(TAG, "disconnectDevice");
    disconnectReader.getObservable(readerType).onErrorComplete().subscribe();
  }

  public void stopDeviceScan() {
    Log.d(TAG, "stopDeviceScan");
    if (mScanDisposable != null && !mScanDisposable.isDisposed())
      mScanDisposable.dispose();
  }

  public void sale(SaleRequest saleRequest, final ReaderInfo.ReaderType readerType, boolean allowDuplicate) {
    Log.d(TAG, "sale");

    if (mEmployeeMerchant != null && !mEmployeeMerchant.getMerchant().getFeatures().contains("sales")) {
      Log.d(TAG, "sale not supported");
      mBroadcaster.notifyOnSaleResponse(new SaleResponse(false, ResultCode.UNSUPPORTED));
      return;
    }

    mLastTransactionRequest = saleRequest;
    clearReferenceData();
    mOrder = new Order();
    TaxRate taxRate = null;
    mOrder.addCustomItem(new Order.CustomItem("item", ((double) saleRequest.getAmount()) / 100, 1, taxRate));
    if (saleRequest.getTipAmount() != null)
      mOrder.setTip(((double) saleRequest.getTipAmount()) / 100);
    mOrder.setExternalPaymentId(saleRequest.getExternalId());
    mOrder.allowDuplicates = allowDuplicate;

    if (saleRequest instanceof KeyedSaleRequest) {
      mCreditCard = new CreditCard(((KeyedSaleRequest) saleRequest).getCardNumber(), ((KeyedSaleRequest) saleRequest).getExpDate(), ((KeyedSaleRequest) saleRequest).getCvv());
      mCreditCard.setCardPresent(((KeyedSaleRequest) saleRequest).isCardPresent());
      if (((KeyedSaleRequest) saleRequest).getBillingAddress() != null) {
        BillingAddress address = ((KeyedSaleRequest) saleRequest).getBillingAddress();
        mCreditCard.setBillingAddress(new CreditCard.BillingAddress(address.getStreet(), address.getZipPostalCode(), address.getCountry()));
      }
      mAuthOrSaleTransaction.getObservable(mOrder, mCreditCard).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentObserver);
    } else {
      startCardReaderTransaction(readerType);
    }
  }

  public void preAuth(PreAuthRequest preAuthRequest, final ReaderInfo.ReaderType readerType, boolean allowDuplicate) {
    Log.d(TAG, "preauth");
    if (mEmployeeMerchant != null && !mEmployeeMerchant.getMerchant().getFeatures().contains("preAuths")) {
      Log.d(TAG, "preauth not supported");
      mBroadcaster.notifyOnPreAuthResponse(new PreAuthResponse(false, ResultCode.UNSUPPORTED));
      return;
    }

    mLastTransactionRequest = preAuthRequest;
    clearReferenceData();
    mOrder = new Order();
    TaxRate taxRate = null;
    mOrder.addCustomItem(new Order.CustomItem("item", ((double) preAuthRequest.getAmount()) / 100, 1, taxRate));
    mOrder.setTip(-1);
    mOrder.setExternalPaymentId(preAuthRequest.getExternalId());
    mOrder.allowDuplicates = allowDuplicate;
    //TODO: setTax amount in Order

    if (preAuthRequest instanceof KeyedPreAuthRequest) {
      CreditCard creditCard = new CreditCard(((KeyedPreAuthRequest) preAuthRequest).getCardNumber(), ((KeyedPreAuthRequest) preAuthRequest).getExpDate(), ((KeyedPreAuthRequest) preAuthRequest).getCvv());
      creditCard.setCardPresent(((KeyedPreAuthRequest) preAuthRequest).isCardPresent());
      if (((KeyedPreAuthRequest) preAuthRequest).getBillingAddress() != null) {
        BillingAddress address = ((KeyedPreAuthRequest) preAuthRequest).getBillingAddress();
        creditCard.setBillingAddress(new CreditCard.BillingAddress(address.getStreet(), address.getZipPostalCode(), address.getCountry()));
      }
      mAuthOrSaleTransaction.getObservable(mOrder, creditCard).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentObserver);
    } else {
      startCardReaderTransaction(readerType);
    }
  }

  public void auth(AuthRequest authRequest, final ReaderInfo.ReaderType readerType, boolean allowDuplicate) {
    Log.d(TAG, "auth");
    if (mEmployeeMerchant != null && !mEmployeeMerchant.getMerchant().getFeatures().contains("auths")) {
      Log.d(TAG, "preauth not supported");
      mBroadcaster.notifyOnAuthResponse(new AuthResponse(false, ResultCode.UNSUPPORTED));
      return;
    }

    mLastTransactionRequest = authRequest;
    clearReferenceData();
    mOrder = new Order();
    TaxRate taxRate = null;
    mOrder.addCustomItem(new Order.CustomItem("item", ((double) authRequest.getAmount()) / 100, 1, taxRate));
    mOrder.setTip(-1);
    mOrder.setExternalPaymentId(authRequest.getExternalId());
    mOrder.allowDuplicates = allowDuplicate;
    //TODO: setTax amount in Order

    if (authRequest instanceof KeyedAuthRequest) {
      CreditCard creditCard = new CreditCard(((KeyedAuthRequest) authRequest).getCardNumber(), ((KeyedAuthRequest) authRequest).getExpDate(), ((KeyedAuthRequest) authRequest).getCvv());
      creditCard.setCardPresent(((KeyedAuthRequest) authRequest).isCardPresent());
      if (((KeyedAuthRequest) authRequest).getBillingAddress() != null) {
        BillingAddress address = ((KeyedAuthRequest) authRequest).getBillingAddress();
        creditCard.setBillingAddress(new CreditCard.BillingAddress(address.getStreet(), address.getZipPostalCode(), address.getCountry()));
      }
      mAuthOrSaleTransaction.getObservable(mOrder, creditCard).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentObserver);
    } else {
      startCardReaderTransaction(readerType);
    }
  }

  private void startCardReaderTransaction(final ReaderInfo.ReaderType readerType) {
    Log.d(TAG, "startCardReaderTransaction");
    mGetConnectedReaders.getBlockingObservable().filter(new Predicate<ReaderInfo>() {
      @Override
      public boolean test(@NonNull ReaderInfo readerInfo) throws Exception {
        return readerInfo.getReaderType() == readerType;
      }
    }).switchIfEmpty(Observable.<ReaderInfo>error(new Error("", ""))).subscribe(new Observer<ReaderInfo>() {
      @Override
      public void onSubscribe(Disposable d) {
        Log.d(TAG, "startCardReaderTransaction subscribe");
      }

      @Override
      public void onNext(ReaderInfo readerInfo) {
        Log.d(TAG, "startCardReaderTransaction next");
        mLastTransactionReader = readerInfo;
        mReadCard.getObservable(readerInfo, (int) Math.round(mOrder.getTotalAmount() * 100)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
      }

      @Override
      public void onError(Throwable e) {
        Log.d(TAG, "startCardReaderTransaction error");
        if (mLastTransactionRequest instanceof SaleRequest) {
          Log.d(TAG, "startCardReaderTransaction error SaleRequest");
          SaleResponse saleResponse = new SaleResponse(false, ResultCode.CANCEL);
          saleResponse.setMessage("Connect Reader");
          saleResponse.setReason("Reader Not Connected");
          mBroadcaster.notifyOnSaleResponse(saleResponse);
        } else if (mLastTransactionRequest instanceof AuthRequest) {
          Log.d(TAG, "startCardReaderTransaction error AuthRequest");
          AuthResponse authResponse = new AuthResponse(false, ResultCode.CANCEL);
          authResponse.setMessage("Connect Reader");
          authResponse.setReason("Reader Not Connected");
          mBroadcaster.notifyOnAuthResponse(authResponse);
        } else if (mLastTransactionRequest instanceof PreAuthRequest) {
          Log.d(TAG, "startCardReaderTransaction error PreAuthRequest");
          PreAuthResponse preAuthResponse = new PreAuthResponse(false, ResultCode.CANCEL);
          preAuthResponse.setMessage("Connect Reader");
          preAuthResponse.setReason("Reader Not Connected");
          mBroadcaster.notifyOnPreAuthResponse(preAuthResponse);
        }
      }

      @Override
      public void onComplete() {
        Log.d(TAG, "startCardReaderTransaction complete");
      }
    });
  }

  public void tipAdjustAuth(final TipAdjustAuthRequest authTipAdjustRequest) {
    if (mEmployeeMerchant != null && mEmployeeMerchant.getMerchant().getFeatures().contains("tip_adjust")) {
      mAddTips.getObservable(authTipAdjustRequest.getPaymentId(), ((double) authTipAdjustRequest.getTipAmount() / 100)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
        @Override
        public void onSubscribe(Disposable d) {
        }

        @Override
        public void onComplete() {
          Log.d(TAG, "TipAdjustAuth onComplete");
          TipAdjustAuthResponse tipAdjustAuthResponse = new TipAdjustAuthResponse(true, ResultCode.SUCCESS);
          tipAdjustAuthResponse.setPaymentId(authTipAdjustRequest.getPaymentId());
          tipAdjustAuthResponse.setTipAmount(authTipAdjustRequest.getTipAmount());
          mBroadcaster.notifyOnTipAdjustAuthResponse(tipAdjustAuthResponse);
        }

        @Override
        public void onError(Throwable e) {
          Error error = Error.convertToError(e);
          Log.d(TAG, "TipAdjustAuth onError");
          TipAdjustAuthResponse tipAdjustAuthResponse = new TipAdjustAuthResponse(false, ResultCode.FAIL);
          tipAdjustAuthResponse.setTipAmount(authTipAdjustRequest.getTipAmount());
          tipAdjustAuthResponse.setPaymentId(authTipAdjustRequest.getPaymentId());
          tipAdjustAuthResponse.setReason(error.getCode());
          tipAdjustAuthResponse.setMessage(error.getMessage());
          mBroadcaster.notifyOnTipAdjustAuthResponse(tipAdjustAuthResponse);
        }
      });
    } else {
      TipAdjustAuthResponse tipAdjustAuthResponse = new TipAdjustAuthResponse(false, ResultCode.UNSUPPORTED);
      tipAdjustAuthResponse.setTipAmount(authTipAdjustRequest.getTipAmount());
      tipAdjustAuthResponse.setPaymentId(authTipAdjustRequest.getPaymentId());
      mBroadcaster.notifyOnTipAdjustAuthResponse(tipAdjustAuthResponse);
    }
  }

  public void capturePreAuth(final CapturePreAuthRequest capturePreAuthRequest) {
    mCaptureTransaction.getObservable(capturePreAuthRequest.getPaymentID(), ((double) capturePreAuthRequest.getAmount()) / 100, ((double) capturePreAuthRequest.getTipAmount()) / 100).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Payment>() {
      @Override
      public void onSubscribe(Disposable d) {
      }

      @Override
      public void onNext(Payment payment) {
        Log.d(TAG, "Capture Transaction onNext");
        CapturePreAuthResponse response = new CapturePreAuthResponse(true, ResultCode.SUCCESS);
        response.setPaymentID(payment.getPaymentId());

        response.setTipAmount((long) (payment.getTipCharged() * 100));
        response.setAmount((long) (payment.getAmountCharged() * 100) - (long) (payment.getTipCharged() * 100));
        mBroadcaster.notifyOnCapturePreAuth(response);
      }

      @Override
      public void onError(Throwable e) {
        Error error = Error.convertToError(e);
        Log.d(TAG, "Capture Transaction onError");
        CapturePreAuthResponse response = new CapturePreAuthResponse(true, ResultCode.FAIL);
        response.setPaymentID(capturePreAuthRequest.getPaymentID());
        response.setAmount(capturePreAuthRequest.getAmount());
        response.setTipAmount(capturePreAuthRequest.getTipAmount());
        response.setReason(error.getCode());
        response.setMessage(error.getMessage());
        mBroadcaster.notifyOnCapturePreAuth(response);
      }

      @Override
      public void onComplete() {
      }
    });
  }

  public void voidPayment(final VoidPaymentRequest voidPaymentRequest) {
    if (mEmployeeMerchant != null && mEmployeeMerchant.getMerchant().getFeatures().contains("voids")) {
      mVoidTransaction.getObservable(voidPaymentRequest.getOrderId(), voidPaymentRequest.getPaymentId()).subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
        @Override
        public void onSubscribe(Disposable d) {
        }

        @Override
        public void onComplete() {
          Log.d(TAG, "Void transaction onComplete");
          VoidPaymentResponse response = new VoidPaymentResponse(true, ResultCode.SUCCESS);
          response.setPaymentId(voidPaymentRequest.getPaymentId());
          mBroadcaster.notifyOnVoidPaymentResponse(response);
        }

        @Override
        public void onError(Throwable e) {
          Log.d(TAG, "Void transaction onError " + e.getMessage());
          Error error = Error.convertToError(e);
          VoidPaymentResponse response = new VoidPaymentResponse(false, ResultCode.FAIL);
          response.setPaymentId(voidPaymentRequest.getPaymentId());
          response.setReason(error.getCode());
          response.setMessage(error.getMessage());
          mBroadcaster.notifyOnVoidPaymentResponse(response);
        }
      });
    } else {
      VoidPaymentResponse voidPaymentResponse = new VoidPaymentResponse(false, ResultCode.FAIL);
      voidPaymentResponse.setPaymentId(voidPaymentRequest.getPaymentId());
      mBroadcaster.notifyOnVoidPaymentResponse(voidPaymentResponse);
    }
  }

  public void refundPayment(final RefundPaymentRequest refundPaymentRequest) {
    if (refundPaymentRequest.getPaymentId() == null) {
      RefundPaymentResponse response = new RefundPaymentResponse(false, ResultCode.ERROR);
      response.setReason("payment_id_null");
      response.setMessage("Payment ID must not be null");

      mBroadcaster.notifyOnRefundPaymentResponse(response);
      return;
    }

    String amount = null;
    if (!refundPaymentRequest.isFullRefund()) {
      amount = String.valueOf(refundPaymentRequest.getAmount());
    }
    mRefundTransaction.getObservable(refundPaymentRequest.getPaymentId(), amount).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<RefundSuccess>() {
      @Override
      public void onSubscribe(Disposable d) {
      }

      @Override
      public void onNext(RefundSuccess refundSuccess) {
        RefundPaymentResponse response = new RefundPaymentResponse(true, ResultCode.SUCCESS);
        response.setPaymentId(refundSuccess.getRefundPaymentId());
        response.setOrderId(refundPaymentRequest.getOrderId());
        com.clover.sdk.v3.payments.Refund _refund = new com.clover.sdk.v3.payments.Refund();
        _refund.setId(refundSuccess.getRefundId());
        _refund.setAmount((long) (refundSuccess.getRefundAmount() * 100));
        response.setRefund(_refund);
        mBroadcaster.notifyOnRefundPaymentResponse(response);
      }

      @Override
      public void onError(Throwable e) {
        Log.d(TAG, "Refund transaction onError " + e.getMessage());
        Error error = Error.convertToError(e);
        RefundPaymentResponse response = new RefundPaymentResponse(false, ResultCode.FAIL);
        response.setPaymentId(refundPaymentRequest.getPaymentId());
        response.setOrderId(refundPaymentRequest.getOrderId());
        response.setReason(error.getCode());
        response.setMessage(error.getMessage());
        mBroadcaster.notifyOnRefundPaymentResponse(response);

      }

      @Override
      public void onComplete() {
      }
    });

  }

  public void closeout(final CloseoutRequest closeoutRequest) {
    mCloseOut.getObservable().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
      @Override
      public void onSubscribe(Disposable d) {
      }

      @Override
      public void onComplete() {
        Log.d(TAG, "CloseOut onComplete");
        CloseoutResponse closeoutResponse = new CloseoutResponse(true, ResultCode.SUCCESS);
        mBroadcaster.notifyCloseout(closeoutResponse);
      }

      @Override
      public void onError(Throwable e) {
        Error error = Error.convertToError(e);
        CloseoutResponse closeoutResponse = new CloseoutResponse(false, ResultCode.FAIL);
        closeoutResponse.setReason(error.getMessage());

        mBroadcaster.notifyCloseout(closeoutResponse);
      }
    });
  }

  public void acceptPayment(com.clover.sdk.v3.payments.Payment payment) {
    mOrder.allowDuplicates = true;
    if (mLastTransactionRequest instanceof KeyedSaleRequest || mLastTransactionRequest instanceof KeyedAuthRequest) {
      mAuthOrSaleTransaction.getObservable(mOrder, mCreditCard).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentObserver);
    } else if (mLastTransactionRequest instanceof KeyedPreAuthRequest) {
      mPreAuthTransaction.getObservable(mOrder, mCreditCard).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentObserver);
    } else {
      EventBus.post(mReaderProgressEvent);
    }
  }

  public void rejectPayment(com.clover.sdk.v3.payments.Payment payment, Challenge challenge) {
    if (mReaderProgressEvent != null && mReaderProgressEvent.getEventType() == ReaderProgressEvent.EventType.EMV_DATA) {
      mGetConnectedReaders.getBlockingObservable().subscribe(new Consumer<ReaderInfo>() {
        @Override
        public void accept(@NonNull ReaderInfo readerInfo) throws Exception {
          final Map<String, String> completeEmvData = new HashMap<>();
          completeEmvData.put("Result", "02");
          completeEmvData.put("Authorization_Response", "5A33");
          mWriteToCard.getObservable(readerInfo, completeEmvData).subscribe();
        }
      });
    }
  }

  public void captureSignature(String paymentId, int[][] xy) {
    if (paymentId == null) {
      throw new NullPointerException("Payment ID cannot be null");
    } else if (xy == null) {
      throw new NullPointerException("Signature cannot be null");
    } else {
      mCaptureSignature.getObservable(paymentId, xy).retry(3).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).onErrorComplete().subscribe();
    }
  }

  public void sendReceipt(String emailAddress, String phoneNo, String orderId) {
    mSendReceipt.getObservable(emailAddress != null && !emailAddress.isEmpty() ? emailAddress : null, phoneNo != null && !phoneNo.isEmpty() ? phoneNo : null, orderId).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).onErrorComplete().subscribe();
  }

  private void notifyPaymentResponse() {
    Log.d(TAG, "notifyPaymentResponse");
    GoPayment goPayment = new GoPayment();

    goPayment.setId(mPayment.getPaymentId());
    goPayment.setAmount((long) (mPayment.getAmountCharged() * 100) - (long) (mPayment.getTipCharged() * 100));
    goPayment.setTipAmount((long) (mPayment.getTipCharged() * 100));
    goPayment.setExternalPaymentId(mPayment.getExternalPaymentId());
    goPayment.setSignatureRequired(mPayment.getCard().isSignatureRequired());

    Reference orderRef = new Reference();
    orderRef.setId(mPayment.getOrderId());
    goPayment.setOrder(orderRef);

    CardTransaction cardTransaction = new CardTransaction();
    cardTransaction.setCardholderName(mPayment.getCard().getCardholderName());
    cardTransaction.setCardType(CardType.valueOf(mPayment.getCard().getCardType()));

    if (mLastTransactionRequest instanceof SaleRequest) {
      Log.d(TAG, "notifyPaymentResponse SaleRequest");
      SaleResponse response = new SaleResponse(true, ResultCode.SUCCESS);
      goPayment.setResult(Result.SUCCESS);
      cardTransaction.setType(CardTransactionType.AUTH);

      goPayment.setCardTransaction(cardTransaction);
      response.setPayment(goPayment);

      mBroadcaster.notifyOnSaleResponse(response);

    } else if (mLastTransactionRequest instanceof AuthRequest) {
      Log.d(TAG, "notifyPaymentResponse AuthRequest");
      AuthResponse response = new AuthResponse(true, ResultCode.SUCCESS);
      goPayment.setResult(Result.SUCCESS);
      cardTransaction.setType(CardTransactionType.PREAUTH);

      goPayment.setCardTransaction(cardTransaction);
      response.setPayment(goPayment);

      mBroadcaster.notifyOnAuthResponse(response);

    } else if (mLastTransactionRequest instanceof PreAuthRequest) {
      Log.d(TAG, "notifyPaymentResponse PreAuthRequest");
      PreAuthResponse response = new PreAuthResponse(true, ResultCode.SUCCESS);
      goPayment.setResult(Result.AUTH);
      cardTransaction.setType(CardTransactionType.PREAUTH);

      goPayment.setCardTransaction(cardTransaction);
      response.setPayment(goPayment);

      mBroadcaster.notifyOnPreAuthResponse(response);
    }
  }

  private void clearReferenceData() {
    mReaderProgressEvent = null;
    mCreditCard = null;
    mLastTransactionReader = null;
  }

  private void notifyErrorResponse() {
    if (mLastTransactionRequest instanceof SaleRequest) {
      Log.d(TAG, "notifyErrorResponse SaleRequest" + mTransactionError.getMessage());
      SaleResponse response = new SaleResponse(false, ResultCode.FAIL);
      response.setReason(mTransactionError.getCode());
      response.setMessage(mTransactionError.getMessage());
      mBroadcaster.notifyOnSaleResponse(response);
    } else if (mLastTransactionRequest instanceof AuthRequest) {
      Log.d(TAG, "notifyErrorResponse AuthRequest" + mTransactionError.getMessage());
      AuthResponse response = new AuthResponse(false, ResultCode.FAIL);
      response.setReason(mTransactionError.getCode());
      response.setMessage(mTransactionError.getMessage());
      mBroadcaster.notifyOnAuthResponse(response);
    } else if (mLastTransactionRequest instanceof PreAuthRequest) {
      Log.d(TAG, "notifyErrorResponse PreAuthRequest" + mTransactionError.getMessage());
      PreAuthResponse response = new PreAuthResponse(false, ResultCode.FAIL);
      response.setReason(mTransactionError.getCode());
      response.setMessage(mTransactionError.getMessage());
      mBroadcaster.notifyOnPreAuthResponse(response);
    }
  }

  public void cancelReaderTransaction(final ReaderInfo.ReaderType readerType) {
    mGetConnectedReaders.getBlockingObservable().filter(new Predicate<ReaderInfo>() {
      @Override
      public boolean test(@NonNull ReaderInfo readerInfo) throws Exception {
        return readerInfo.getReaderType() == readerType;
      }
    }).subscribe(new Consumer<ReaderInfo>() {
      @Override
      public void accept(@NonNull ReaderInfo readerInfo) throws Exception {
        cancelCardRead.getObservable(readerInfo).subscribe();
      }
    });
  }
}