package com.clover.remote.client.clovergo;

import android.text.TextUtils;
import android.util.Log;

import com.clover.remote.Challenge;
import com.clover.remote.client.Constants;
import com.clover.remote.client.MerchantInfo;
import com.clover.remote.client.clovergo.CloverGoConstants.TransactionType;
import com.clover.remote.client.clovergo.di.CloverGoSDKApplicationData;
import com.clover.remote.client.clovergo.messages.BillingAddress;
import com.clover.remote.client.clovergo.messages.KeyedRequest;
import com.clover.remote.client.clovergo.util.NumberUtil;
import com.clover.remote.client.lib.R;
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
import com.clover.remote.client.messages.PaymentResponse;
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
import com.clover.sdk.v3.payments.CardEntryType;
import com.clover.sdk.v3.payments.CardTransaction;
import com.clover.sdk.v3.payments.CardTransactionType;
import com.clover.sdk.v3.payments.CardType;
import com.clover.sdk.v3.payments.Result;
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
import com.firstdata.clovergo.domain.model.Tender;
import com.firstdata.clovergo.domain.model.TransactionError;
import com.firstdata.clovergo.domain.rx.EventBus;
import com.firstdata.clovergo.domain.rx.ReusableObserver;
import com.firstdata.clovergo.domain.usecase.AddTips;
import com.firstdata.clovergo.domain.usecase.AuthTransaction;
import com.firstdata.clovergo.domain.usecase.CancelCardRead;
import com.firstdata.clovergo.domain.usecase.CaptureSignature;
import com.firstdata.clovergo.domain.usecase.CaptureTransaction;
import com.firstdata.clovergo.domain.usecase.CloseOut;
import com.firstdata.clovergo.domain.usecase.ConnectToReader;
import com.firstdata.clovergo.domain.usecase.DisconnectReader;
import com.firstdata.clovergo.domain.usecase.GetConnectedReaders;
import com.firstdata.clovergo.domain.usecase.GetSDKMerchantsInfo;
import com.firstdata.clovergo.domain.usecase.PreAuthTransaction;
import com.firstdata.clovergo.domain.usecase.ReadCard;
import com.firstdata.clovergo.domain.usecase.RefundTransaction;
import com.firstdata.clovergo.domain.usecase.SaleTransaction;
import com.firstdata.clovergo.domain.usecase.ScanForReaders;
import com.firstdata.clovergo.domain.usecase.SendReceipt;
import com.firstdata.clovergo.domain.usecase.UpdateReader;
import com.firstdata.clovergo.domain.usecase.VoidTransaction;
import com.firstdata.clovergo.domain.usecase.WriteToCard;
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

import static com.clover.remote.client.clovergo.CloverGoConstants.CARD_MODE_EMV_CONTACT;
import static com.firstdata.clovergo.domain.model.ReaderInfo.ReaderType.RP350;
import static com.firstdata.clovergo.domain.model.ReaderInfo.ReaderType.RP450;
import static com.firstdata.clovergo.domain.model.ReaderProgressEvent.EventType.EMV_DATA;
import static com.firstdata.clovergo.domain.model.ReaderProgressEvent.EventType.SWIPE_DATA;

/**
 * Created by Akhani, Avdhesh on 5/22/17.
 */
public class CloverGoConnectorImpl {
  private final static String TAG = "CloverGO ConnectorImpl";

  // Indicates the transaction is an auth transaction based on tip amount
  public static final int TIP_AMOUNT_AUTH = -1;

  public static final String DUPLICATE_TRANSACTION = "duplicate_transaction";
  public static final String CHARGE_DECLINED = "charge_declined";
  public static final String CHARGE_DECLINED_REFERRAL = "charge_declined_referral";

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
  SaleTransaction mSaleTransaction;
  @Inject
  AuthTransaction mAuthTransaction;
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
  GetSDKMerchantsInfo mGetMerchantsInfo;

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
  @Inject
  UpdateReader updateReader;

  private Order mOrder;
  private Payment mPayment;
  private CreditCard mCreditCard;
  private TransactionError mTransactionError;
  private ConfirmPaymentRequest mConfirmPaymentRequest;
  private Challenge[] mChallenges;
  private EmployeeMerchant mEmployeeMerchant;
  private boolean mLastErrorWasDuplicateTransaction = false;
  private boolean mLastPaymentWasForPartialAuth = false;
  private boolean isQuickChip;
  private boolean isQuickChipCardRemoved;

  private ReaderInfo mConnectedReaderInfo;

  private String noCardReadersConnected;

  public CloverGoConnectorImpl(final CloverGoConnectorBroadcaster broadcaster, CloverGoDeviceConfiguration configuration) {
    Log.d(TAG, "CloverGoConnectorImpl env=" + configuration.getEnv());
    mBroadcaster = broadcaster;

    noCardReadersConnected = configuration.getContext().getString(R.string.no_card_readers_connected_no_keyenter_allowed);

    CloverGoDeviceConfiguration.ENV env = configuration.getEnv();
    String url;
    switch (env) {
      case LIVE:
        url = "https://api.payeezy.com/clovergosdk/v2/";
        break;
      case SANDBOX:
        url = "https://api-cert.payeezy.com/clovergosdk/v2/";
        break;
      case SANDBOX_DEV:
        url = "https://api-cert.payeezy.com/clovergosdk/v2/";
        break;
      default: // DEMO is default
        url = "https://api-int.payeezy.com/clovergosdk/v2/";
    }

    isQuickChip = configuration.isQuickChip();

    CloverGoSDKApplicationData cloverGoSDKApplicationData = new CloverGoSDKApplicationData(configuration.getApplicationId(), configuration.getAppVersion(), configuration.getContext(), url, configuration.getApiKey(),
        configuration.getSecret(), configuration.getAccessToken(), InstanceID.getInstance(configuration.getContext()).getId());
    cloverGoSDKApplicationData.getApplicationComponent().inject(this);

    initObservers();

    // Pull merchant details from the backend if it's not already in the cache
    mGetMerchantsInfo.getCachedObservable().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mEmployeeMerchantObserver);

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
        mBroadcaster.notifyOnGetMerchantInfoResponse(getMerchantInfo(null));
      }

      @Override
      public void onError(Throwable e) {
        Log.d(TAG, "mEmployeeMerchantObserver error");
        super.onError(e);
        mBroadcaster.notifyOnGetMerchantInfoResponse(null);
      }
    };

    // Observer for handling sale/transaction success & error response
    mPaymentObserver = new ReusableObserver<Payment>() {
      @Override
      public void onNext(Payment payment) {
        Log.d(TAG, "mPaymentObserver next");

        mPayment = payment;

        if (!mLastPaymentWasForPartialAuth && mOrder.getStatus() == Order.STATUS_PARTIAL_PAYMENT) {
          Log.d(TAG, "mPaymentObserver next PARTIAL AUTH");
          ConfirmPaymentRequest confirmPaymentRequest = new ConfirmPaymentRequest();

          String challengeMessage = "This card has been approved for " +
            NumberUtil.getCurrencyString(payment.getAmountCharged()) +
            " with a remaining balance of " +
            NumberUtil.getCurrencyString(mOrder.getPendingAuthAmount()) +
            "\n\nWould you like to authorize this card for partial payment?";

          Challenge[] challenge = {new Challenge(challengeMessage, Challenge.ChallengeType.PARTIAL_AUTH_CHALLENGE, VoidReason.REJECT_PARTIAL_AUTH)};
          confirmPaymentRequest.setChallenges(challenge);

          mLastPaymentWasForPartialAuth = true;
          mBroadcaster.notifyOnConfirmPaymentRequest(confirmPaymentRequest);

        } else if (!isQuickChip && CARD_MODE_EMV_CONTACT.equalsIgnoreCase(mPayment.getCard().getMode())) {

          Log.d(TAG, "mPaymentObserver next EMV_CONTACT");
          Map<String, String> data = new HashMap<>();

          if (mPayment.getClientData() != null) {
            Log.d(TAG, "mPaymentObserver next EMV_CONTACT extra");
            data.put("Authorization_Response", "3130".equalsIgnoreCase(mPayment.getClientData().getAuthResponseCode()) ? "3030" : mPayment.getClientData().getAuthResponseCode());
            data.put("Issuer_Auth_Data", mPayment.getClientData().getIssuerAuthData());
            data.put("Auth_Code", HexUtils.convertASCII2HexaDecimal(mPayment.getClientData().getAuthCode()));
            data.put("Result", "01");
            data.put("issuer_script_template1", mPayment.getClientData().getIssuerScriptTemplate1());
            data.put("issuer_script_template2", mPayment.getClientData().getIssuerScriptTemplate2());

          } else {
            Log.d(TAG, "mPaymentObserver next EMV_CONTACT non-extra");
            data.put("Result", "01");
            data.put("Auth_Code", HexUtils.convertASCII2HexaDecimal(mPayment.getCard().getAuthCode()));
            data.put("Authorization_Response", "3030");

          }
          mWriteToCard.getObservable(mLastTransactionReader, data).subscribe();

        } else if (isQuickChip && !isQuickChipCardRemoved && !(mLastTransactionRequest instanceof KeyedRequest)) {
          Log.d(TAG, "mPaymentObserver next EMV_CONTACT " + isQuickChip + " " + isQuickChipCardRemoved);
          // don't do anything and wait for card to be removed

        } else if (mPayment.getCard().isSignatureRequired()) {
          Log.d(TAG, "mPaymentObserver next do signature");
          notifySignatureRequired();
        } else {
          Log.d(TAG, "mPaymentObserver next do receipt");
          notifySendReceipt();
        }
      }

      @Override
      public void onError(Throwable e) {
        Log.d(TAG, "mPaymentObserver error");

        mTransactionError = TransactionError.convertToError(e);
        mLastErrorWasDuplicateTransaction = false;

        if (DUPLICATE_TRANSACTION.equals(mTransactionError.getCode())) {
          Log.d(TAG, "mPaymentObserver error duplicate");
          mLastErrorWasDuplicateTransaction = true;
          mConfirmPaymentRequest = new ConfirmPaymentRequest();
          mChallenges = new Challenge[]{new Challenge(mTransactionError.getMessage(),
              Challenge.ChallengeType.DUPLICATE_CHALLENGE,
              VoidReason.REJECT_DUPLICATE)};
          mConfirmPaymentRequest.setChallenges(mChallenges);

          if (!isQuickChip || isQuickChipCardRemoved || mLastTransactionRequest instanceof KeyedRequest)
            mBroadcaster.notifyOnConfirmPaymentRequest(mConfirmPaymentRequest);

          return;
        } else if ((CHARGE_DECLINED.equals(mTransactionError.getCode()) ||
            CHARGE_DECLINED_REFERRAL.equals(mTransactionError.getCode())) &&
            mReaderProgressEvent != null &&
            mReaderProgressEvent.getEventType() == ReaderProgressEvent.EventType.EMV_DATA) {
          Log.d(TAG, "mPaymentObserver error declined");

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

        if (!isQuickChip) {
          notifyErrorResponse(ResultCode.FAIL, mTransactionError.getCode(), mTransactionError.getMessage());
        }

      }
    };

    mReaderInfoObserver = new ReusableObserver<ReaderInfo>() {
      @Override
      public void onNext(final ReaderInfo readerInfo) {
        Log.d(TAG, "mReaderInfoObserver next");
        if (readerInfo.isConnected()) {
          Log.d(TAG, "mReaderInfoObserver next connected " + String.valueOf(mEmployeeMerchant == null));

          if (mEmployeeMerchant == null) {
            mGetMerchantsInfo.getCachedObservable().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<EmployeeMerchant>() {
              @Override
              public void onSubscribe(Disposable d) {
              }

              @Override
              public void onNext(EmployeeMerchant employeeMerchant) {
                mEmployeeMerchant = employeeMerchant;
                mConnectedReaderInfo = readerInfo;
                updateReader.getObservable(readerInfo).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
              }

              @Override
              public void onError(Throwable e) {
                disconnectDevice(readerInfo.getReaderType());
                mBroadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, e.getMessage()));
              }

              @Override
              public void onComplete() {
              }
            });
          } else {
            mConnectedReaderInfo = readerInfo;
            updateReader.getObservable(readerInfo).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
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
          case NO_UPDATE:
            mBroadcaster.notifyOnReady(getMerchantInfo(mConnectedReaderInfo));
            break;
          case UPDATE_COMPLETE:
            deviceEvent = new CloverDeviceEvent();
            deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.UPDATE_COMPLETED);
            deviceEvent.setMessage("Reader update completed");
            mBroadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);
            mBroadcaster.notifyOnReady(getMerchantInfo(null));

            if (mConnectedReaderInfo.getReaderType() == RP350)
              disconnectDevice(mConnectedReaderInfo.getReaderType());

            break;
          case UPDATE_BEGAN:
            deviceEvent = new CloverDeviceEvent();
            deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.UPDATE_STARTED);
            deviceEvent.setMessage("Updating reader");
            mBroadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);
            break;
          case CARD_INSERTED:
            deviceEvent = new CloverDeviceEvent();
            deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.CARD_INSERTED);
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
            deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.EMV_COMPLETE);
            deviceEvent.setMessage("Please remove card");

            if (!isQuickChip) {
              if (!(readerProgressEvent.getData().get("status").equalsIgnoreCase("Success") && "TC".equalsIgnoreCase(
                  readerProgressEvent.getData().get("cryptogram_information_data")))) {
                Log.d(TAG, "mProgressObserver next " + readerProgressEvent.getEventType() + " success and tc crypto");

                if (mPayment != null) {
                  Log.d(TAG,
                      "mProgressObserver next " + readerProgressEvent.getEventType() + " success and tc crypto and payment not null");

                  mVoidTransaction.getObservable(mPayment.getOrderId(), mPayment.getPaymentId())
                      .subscribeOn(Schedulers.io())
                      .subscribe();
                  mPayment = null;
                  mTransactionError = new TransactionError("chip_decline", "Transaction declined - Chip Decline");
                }
              }
            } else if (isQuickChipCardRemoved) {
              if (mPayment == null) {
                deviceEvent.setMessage("Processing Transaction");
              } else {
                if (mPayment.getCard().isSignatureRequired()) {
                  notifySignatureRequired();
                } else {
                  notifyPaymentResponse();
                }
              }
            }
            mBroadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);
            break;
          case CARD_REMOVED:
            isQuickChipCardRemoved = !isQuickChip ? false : true;
            deviceEvent = new CloverDeviceEvent();
            deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.CARD_REMOVED);
            deviceEvent.setMessage("Card Removed");
            mBroadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);

            if (mPayment != null) {
              Log.d(TAG, "mProgressObserver next " + readerProgressEvent.getEventType() + " payment not null");
              if (mPayment.getCard().isSignatureRequired())
                notifySignatureRequired();
              else
                notifyPaymentResponse();
            } else if (mTransactionError != null) {
              if (mLastErrorWasDuplicateTransaction) {
                mBroadcaster.notifyOnConfirmPaymentRequest(mConfirmPaymentRequest);
              } else {
                Log.d(TAG,
                    "mProgressObserver next " + readerProgressEvent.getEventType() + " transaction error not null");
                notifyErrorResponse(ResultCode.FAIL, mTransactionError.getCode(), mTransactionError.getMessage());
              }
            } else if (isQuickChip) {
              // do nothing and wait
            } else {
              Log.d(TAG, "mProgressObserver next " + readerProgressEvent.getEventType() + " error");
              mTransactionError = new TransactionError("unknown_error", "unknown_error");
              notifyErrorResponse(ResultCode.FAIL, mTransactionError.getCode(), mTransactionError.getMessage());
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
            if (it.hasNext()) {
              aid = readerProgressEvent.getData().get(it.next());
            }

            Log.d(TAG, "mProgressObserver next " + readerProgressEvent.getEventType() + " aid=" + aid);
            mReadCard.getObservable(mLastTransactionReader, aid, 0).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
            break;

          case CUSTOMER_VALIDATION_REQ:
            break;

          case SWIPE_DATA:
            Map<String, String> swipeData = readerProgressEvent.getData();
            emvCard = new EmvCard(swipeData.get("ksn"),
                swipeData.get("encryptedTrack"),
                swipeData.get("track1"),
                swipeData.get("track2"),
                swipeData.get("pan"),
                swipeData.get("pos_entry_mode"));
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
          mReaderProgressEvent = readerProgressEvent;

          if (isQuickChip) {
            Map<String, String> data = new HashMap<>();
            data.put("Result", "01");
            data.put("Authorization_Response", "5A33");
            mWriteToCard.getObservable(mLastTransactionReader, data).subscribe();
          }

          doTransaction(emvCard);
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

  private MerchantInfo getMerchantInfo(ReaderInfo readerInfo) {
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

    String readerBtName = readerInfo == null ? "" : readerInfo.getBluetoothName();
    String readerSerialNo = readerInfo == null ? "" : readerInfo.getSerialNo();
    String readerType = readerInfo == null ? "" : readerInfo.getReaderType().name();

    return new MerchantInfo(mEmployeeMerchant.getMerchant().getId(), mEmployeeMerchant.getMerchant().getName(),
        supportsSales, supportAuths, supportsPreAuths, supportsVaultCards, supportsManualRefunds, supportsVoids,
        supportsTipAdjust, readerBtName, readerSerialNo, readerType);
  }

  public void initializeConnection(ReaderInfo.ReaderType readerType) {
    Log.d(TAG, "initializeConnection " + readerType.name());
    if (readerType == RP450) {
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
    } else if (readerType == RP350) {
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
    mConnectedReaderInfo = null;
  }

  public void stopDeviceScan() {
    Log.d(TAG, "stopDeviceScan");
    if (mScanDisposable != null && !mScanDisposable.isDisposed())
      mScanDisposable.dispose();
  }

  public void preAuth(PreAuthRequest preAuthRequest, final ReaderInfo.ReaderType readerType, boolean allowDuplicate) {
    Log.d(TAG, "preauth");

    if (mEmployeeMerchant != null && !mEmployeeMerchant.getMerchant().getFeatures().contains("preAuths")) {
      notifyErrorResponse(ResultCode.UNSUPPORTED, "", "");
      return;
    }

    beginTransaction(TransactionType.PRE_AUTH, preAuthRequest, allowDuplicate);
  }

  public void auth(final AuthRequest authRequest, final ReaderInfo.ReaderType readerType, boolean allowDuplicate) {
    Log.d(TAG, "auth");

    if (mEmployeeMerchant != null && !mEmployeeMerchant.getMerchant().getFeatures().contains("auths")) {
      notifyErrorResponse(ResultCode.UNSUPPORTED, "", "");
      return;
    }

    beginTransaction(TransactionType.AUTH, authRequest, allowDuplicate);
  }

  public void sale(final SaleRequest saleRequest, final boolean allowDuplicate) {
    Log.d(TAG, "sale");

    if (mEmployeeMerchant != null && !mEmployeeMerchant.getMerchant().getFeatures().contains("sales")) {
      notifyErrorResponse(ResultCode.UNSUPPORTED, "", "");
      return;
    }

    beginTransaction(TransactionType.SALE, saleRequest, allowDuplicate);
  }

  private void beginTransaction(final TransactionType transactionType, final TransactionRequest transactionRequest, final boolean allowDuplicate) {

    int cardEntryMethods = transactionRequest.getCardEntryMethods();
    List<ReaderInfo> connectedReaders = mGetConnectedReaders.getBlockingObservable().toList().blockingGet();

    // If either Key Entered card entry method is allowed OR at least one card reader is allowed and connected,
    // then go to the payment options screen.
    if (cardEntrySwitchIsOn(cardEntryMethods, Constants.CARD_ENTRY_METHOD_MANUAL) ||
        atLeastOneCardReaderAllowedAndConnected(cardEntryMethods, connectedReaders)) {

      mBroadcaster.notifyOnPaymentTypeRequired(cardEntryMethods, connectedReaders, new ICloverGoConnectorListener.PaymentTypeSelection() {
        @Override
        public void selectPaymentType(ICloverGoConnector.GoPaymentType goPaymentType, ReaderInfo.ReaderType readerType) {
          continueTransactionAfterPaymentTypeChosen(transactionType, transactionRequest, goPaymentType, readerType, allowDuplicate);
        }
      });

    } else {

      Log.d(TAG, "mBroadcaster.notifyOnDeviceError");

      // Otherwise display an error because either nothing was allowed or connected
      mBroadcaster.notifyOnDeviceError(
          new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.READER_NOT_CONNECTED,
              0,
              null,
            noCardReadersConnected));
    }
  }

  private boolean atLeastOneCardReaderAllowedAndConnected(int cardEntryMethods, List<ReaderInfo> connectedReaders) {

    boolean rp350Available = ((cardEntrySwitchIsOn(cardEntryMethods, Constants.CARD_ENTRY_METHOD_ICC_CONTACT) ||
        cardEntrySwitchIsOn(cardEntryMethods, Constants.CARD_ENTRY_METHOD_MAG_STRIPE)) &&
        readerConnected(RP350, connectedReaders));

    boolean rp450Available = (cardEntrySwitchIsOn(cardEntryMethods, Constants.CARD_ENTRY_METHOD_NFC_CONTACTLESS) &&
        readerConnected(RP450, connectedReaders));

    return rp350Available || rp450Available;
  }

  private boolean readerConnected(ReaderInfo.ReaderType readerType, List<ReaderInfo> connectedReaders) {

    boolean connected = false;

    if (connectedReaders != null && connectedReaders.size() > 0) {
      for (ReaderInfo connectedReader : connectedReaders) {
        if (connectedReader.getReaderType() == readerType) {
          connected = true;
        }
      }
    }
    return connected;
  }

  private boolean cardEntrySwitchIsOn(int cardEntryMethods, int cardEntryOption) {
    return ((cardEntryMethods & cardEntryOption) == cardEntryOption);
  }

  public void continueTransactionAfterPaymentTypeChosen(final TransactionType transactionType,
                                                        final TransactionRequest transactionRequest,
                                                        final ICloverGoConnector.GoPaymentType goPaymentType,
                                                        final ReaderInfo.ReaderType readerType,
                                                        final boolean allowDuplicate) {

    Log.d(TAG, "continueTransactionAfterPaymentTypeChosen");

    if (goPaymentType == ICloverGoConnector.GoPaymentType.KEYED) {

      mBroadcaster.notifyOnManualCardEntryRequired(transactionType, transactionRequest, goPaymentType, readerType, allowDuplicate, new ICloverGoConnectorListener.ManualCardEntry() {
        @Override
        public void cardDataEntered(TransactionRequest transactionRequest, TransactionType transactionType) {
          continueTransactionAfterCardEnteredManually(transactionType, transactionRequest, goPaymentType, readerType, allowDuplicate);
        }
      });

    } else {

      // Card reader transaction (not keyed)
      prepOrder(transactionType, transactionRequest, allowDuplicate);
      startCardReaderTransaction(readerType, transactionType);
    }
  }

  public void continueTransactionAfterCardEnteredManually(TransactionType transactionType,
                                                          final TransactionRequest transactionRequest,
                                                          final ICloverGoConnector.GoPaymentType goPaymentType,
                                                          final ReaderInfo.ReaderType readerType,
                                                          boolean allowDuplicate) {

    Log.d(TAG, "continueTransactionAfterCardEnteredManually");

    prepOrder(transactionType, transactionRequest, allowDuplicate);
    prepKeyedCreditCardForTransaction((KeyedRequest) transactionRequest);

    doTransaction(mCreditCard);
  }

  private void prepOrder(TransactionType transactionType, TransactionRequest transactionRequest, boolean allowDuplicate) {
    mLastTransactionRequest = transactionRequest;
    clearReferenceData();
    mOrder = new Order();
    mOrder.setOrderNote(transactionRequest.getNote());
    mOrder.addCustomItem(new Order.CustomItem("item", ((double) transactionRequest.getAmount()) / 100, 1, null, null));

    if (transactionType == TransactionType.AUTH || transactionType == TransactionType.PRE_AUTH) {
      mOrder.setTip(TIP_AMOUNT_AUTH);

    } else if (transactionType == TransactionType.SALE) {

      Long tipAmount = ((SaleRequest) transactionRequest).getTipAmount();

      if (tipAmount != null) {
        mOrder.setTip(((double) tipAmount) / 100);
      }
    }
    mOrder.setExternalPaymentId(transactionRequest.getExternalId());
    mOrder.allowDuplicates = allowDuplicate;
  }

  private void prepKeyedCreditCardForTransaction(KeyedRequest keyedRequest) {
    mCreditCard = new CreditCard(keyedRequest.getCardNumber(), keyedRequest.getExpDate(), keyedRequest.getCvv());
    mCreditCard.setCardPresent(keyedRequest.isCardPresent());
    if (keyedRequest.getBillingAddress() != null) {
      BillingAddress address = keyedRequest.getBillingAddress();
      mCreditCard.setBillingAddress(new CreditCard.BillingAddress(address.getStreet(), address.getZipPostalCode(), address.getCountry()));
    }
  }

  private void startCardReaderTransaction(final ReaderInfo.ReaderType readerType, TransactionType transactionType) {

    String message = "";

    if (readerType == RP350) {
      message = "Swipe or Dip card for Payment";
    } else {
      message = "Swipe, Tap or Dip card for Payment";
    }

    mBroadcaster.notifyOnProgressDialog(transactionType.name(), message, true);

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
        notifyErrorResponse(ResultCode.CANCEL, "Reader Not Connected", "Connect Reader");
      }

      @Override
      public void onComplete() {
        Log.d(TAG, "startCardReaderTransaction complete");
      }
    });
  }

  private void doTransaction(Tender tender) {
    if (mLastTransactionRequest instanceof SaleRequest) {
      Log.d(TAG, "doTransaction SaleRequest");
      mSaleTransaction.getObservable(mOrder, tender).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentObserver);

    } else if (mLastTransactionRequest instanceof AuthRequest) {
      Log.d(TAG, "doTransaction AuthRequest");
      mAuthTransaction.getObservable(mOrder, tender).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentObserver);

    } else if (mLastTransactionRequest instanceof PreAuthRequest) {
      Log.d(TAG, "doTransaction PreAuthRequest");
      mPreAuthTransaction.getObservable(mOrder, tender).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentObserver);

    }
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
    mCaptureTransaction.getObservable(capturePreAuthRequest.getPaymentID(),
      ((double) capturePreAuthRequest.getAmount()) / 100,
      ((double) capturePreAuthRequest.getTipAmount()) / 100).
      subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Payment>() {

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
        CapturePreAuthResponse response = new CapturePreAuthResponse(false, ResultCode.FAIL);
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
    Log.d(TAG, "acceptPayment");
    mOrder.allowDuplicates = true;

    // User has accepted the partial auth challenge and at this point, they should proceed with the signature
    // or send receipt process. Since payment has already been processed and accepted, don't try to make another
    // transaction.
    if (mOrder.getStatus() == Order.STATUS_PARTIAL_PAYMENT) {
      Log.d(TAG, "acceptPayment partial pay");
      if (mOrder.getPayments().get(0).getCard().isSignatureRequired())
        notifySignatureRequired();
      else
        notifySendReceipt();

    } else if (mLastTransactionRequest instanceof KeyedRequest) {
      doTransaction(mCreditCard);

    } else {
      Log.d(TAG, "acceptPayment eventbus post " + mReaderProgressEvent.getEventType().name() + " " + mReaderProgressEvent.getMessage());
      EventBus.post(mReaderProgressEvent);
    }
  }

  public void rejectPayment(com.clover.sdk.v3.payments.Payment payment, Challenge challenge) {
    Log.d(TAG, "rejectPayment");
    if (mOrder.getStatus() == Order.STATUS_PARTIAL_PAYMENT) {
      Log.d(TAG, "rejectPayment STATUS_PARTIAL_PAYMENT");
      mBroadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.PARTIAL_AUTH_REJECTED, 0, null, "In rejectPayment: Partial auth rejected by user.  Payment being voided"));
      mBroadcaster.notifyVoidPayment(mPayment, "Partial Auth Rejected");
    } else if (mLastErrorWasDuplicateTransaction) {
      Log.d(TAG, "rejectPayment mLastErrorWasDuplicateTransaction");
      mBroadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.DUPLICATE_TRANSACTION_REJECTED, 0, null, "In rejectPayment: Duplicate transaction rejected by user."));
    } else if (mReaderProgressEvent != null && mReaderProgressEvent.getEventType() == ReaderProgressEvent.EventType.EMV_DATA) {
      Log.d(TAG, "rejectPayment EMV_DATA");
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

  public void sendReceipt(String emailAddress, String phoneNo, String orderId) {

    mSendReceipt.getObservable(emailAddress != null && !emailAddress.isEmpty() ? emailAddress : null, phoneNo != null && !phoneNo.isEmpty() ? phoneNo : null, orderId).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {

      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onComplete() {
        mBroadcaster.notifyOnDisplayMessage("Send receipt successful");
      }

      @Override
      public void onError(Throwable e) {
        mBroadcaster.notifyOnDisplayMessage("Send receipt failed");
      }
    });
  }

  private void notifySignatureRequired() {
    mBroadcaster.notifyOnSignatureRequired(mPayment, new ICloverGoConnectorListener.SignatureCapture() {
      @Override
      public void captureSignature(String paymentId, int[][] xy) {
        if (paymentId == null) {
          throw new NullPointerException("Payment ID cannot be null");
        } else if (xy == null) {
          throw new NullPointerException("Signature cannot be null");
        } else {
          mCaptureSignature.getObservable(paymentId, xy).retry(3).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).onErrorComplete().subscribe();
          notifySendReceipt();
        }
      }
    });
  }

  private void notifySendReceipt() {
    mBroadcaster.notifyOnSendReceipt(mOrder, new ICloverGoConnectorListener.SendReceipt() {
      @Override
      public void sendRequestedReceipt(String email, String phone, String orderId) {
        sendReceipt(email, phone, orderId);
        notifyPaymentResponse();
      }

      @Override
      public void noReceipt() {
        notifyPaymentResponse();
      }
    });
  }

  private void notifyPaymentResponse() {
    Log.d(TAG, "notifyPaymentResponse");
    com.clover.sdk.v3.payments.Payment cloverPayment = new com.clover.sdk.v3.payments.Payment();

    cloverPayment.setId(mPayment.getPaymentId());
    cloverPayment.setAmount((long) (mPayment.getAmountCharged() * 100) - (long) (mPayment.getTipCharged() * 100));
    cloverPayment.setTaxAmount((long) mPayment.getTaxCharged() * 100);
    cloverPayment.setTipAmount((long) (mPayment.getTipCharged() * 100));
    cloverPayment.setExternalPaymentId(mPayment.getExternalPaymentId());

    Reference cloverOrderRef = new Reference();
    cloverOrderRef.setId(mPayment.getOrderId());
    cloverPayment.setOrder(cloverOrderRef);

    Payment.Card card = mPayment.getCard();
    CardTransaction cardTransaction = new CardTransaction();
    cardTransaction.setCardholderName(card.getCardholderName());
    cardTransaction.setCardType(CardType.valueOf(card.getCardType()));
    cardTransaction.setEntryType(CardEntryType.valueOf(card.getMode()));
    cardTransaction.setAuthCode(card.getAuthCode());

    String cardNumber = card.getCardNumber();
    cardTransaction.setFirst6(cardNumber.substring(0, 6));
    cardTransaction.setLast4(cardNumber.substring(cardNumber.length() - 4));

    if (mLastTransactionRequest instanceof SaleRequest) {
      Log.d(TAG, "notifyPaymentResponse SaleRequest");
      SaleResponse response = new SaleResponse(true, ResultCode.SUCCESS);
      cloverPayment.setResult(Result.SUCCESS);
      cardTransaction.setType(CardTransactionType.AUTH);

      cloverPayment.setCardTransaction(cardTransaction);
      response.setPayment(cloverPayment);

      mBroadcaster.notifyOnSaleResponse(response);

    } else if (mLastTransactionRequest instanceof AuthRequest) {
      Log.d(TAG, "notifyPaymentResponse AuthRequest");
      AuthResponse response = new AuthResponse(true, ResultCode.SUCCESS);
      cloverPayment.setResult(Result.SUCCESS);
      cardTransaction.setType(CardTransactionType.PREAUTH);

      cloverPayment.setCardTransaction(cardTransaction);
      response.setPayment(cloverPayment);

      mBroadcaster.notifyOnAuthResponse(response);

    } else if (mLastTransactionRequest instanceof PreAuthRequest) {
      Log.d(TAG, "notifyPaymentResponse PreAuthRequest");
      PreAuthResponse response = new PreAuthResponse(true, ResultCode.SUCCESS);
      cloverPayment.setResult(Result.AUTH);
      cardTransaction.setType(CardTransactionType.PREAUTH);

      cloverPayment.setCardTransaction(cardTransaction);
      response.setPayment(cloverPayment);

      mBroadcaster.notifyOnPreAuthResponse(response);
    }
  }

  private void clearReferenceData() {
    mChallenges = null;
    mConfirmPaymentRequest = null;
    mLastErrorWasDuplicateTransaction = false;

    isQuickChipCardRemoved = false;
    mReaderProgressEvent = null;
    mCreditCard = null;
    mLastTransactionReader = null;
  }

  private void notifyErrorResponse(ResultCode resultCode, String reason, String message) {
    if (mLastTransactionRequest instanceof SaleRequest) {
      Log.d(TAG, "notifyErrorResponse SaleRequest" + message);
      SaleResponse response = new SaleResponse(false, resultCode);
      setPaymentResponseReasonMessage(response, reason, message);
      mBroadcaster.notifyOnSaleResponse(response);

    } else if (mLastTransactionRequest instanceof AuthRequest) {
      Log.d(TAG, "notifyErrorResponse AuthRequest" + message);
      AuthResponse response = new AuthResponse(false, resultCode);
      setPaymentResponseReasonMessage(response, reason, message);
      mBroadcaster.notifyOnAuthResponse(response);

    } else if (mLastTransactionRequest instanceof PreAuthRequest) {
      Log.d(TAG, "notifyErrorResponse PreAuthRequest" + message);
      PreAuthResponse response = new PreAuthResponse(false, resultCode);
      setPaymentResponseReasonMessage(response, reason, message);
      mBroadcaster.notifyOnPreAuthResponse(response);

    }
  }

  private void setPaymentResponseReasonMessage(PaymentResponse paymentResponse, String reason, String message) {
    paymentResponse.setReason(reason);
    paymentResponse.setMessage(message);
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