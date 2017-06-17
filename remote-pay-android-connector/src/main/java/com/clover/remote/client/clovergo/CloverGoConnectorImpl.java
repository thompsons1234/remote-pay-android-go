package com.clover.remote.client.clovergo;

import android.text.TextUtils;
import android.util.Log;

import com.clover.remote.Challenge;
import com.clover.remote.client.MerchantInfo;
import com.clover.remote.client.clovergo.di.DaggerApplicationComponent;
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
import com.firstdata.clovergo.domain.model.EmployeeMerchant;
import com.firstdata.clovergo.domain.model.EmvCard;
import com.firstdata.clovergo.domain.model.Error;
import com.firstdata.clovergo.domain.model.Order;
import com.firstdata.clovergo.domain.model.Payment;
import com.firstdata.clovergo.domain.model.ReaderError;
import com.firstdata.clovergo.domain.model.ReaderInfo;
import com.firstdata.clovergo.domain.model.ReaderProgressEvent;
import com.firstdata.clovergo.domain.model.Refund;
import com.firstdata.clovergo.domain.model.TaxRate;
import com.firstdata.clovergo.domain.model.TransactionError;
import com.firstdata.clovergo.domain.rx.EventBus;
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

import static com.firstdata.clovergo.domain.model.ReaderProgressEvent.EventType.CHIP_DATA;
import static com.firstdata.clovergo.domain.model.ReaderProgressEvent.EventType.CONTACT_LESS_DATA;
import static com.firstdata.clovergo.domain.model.ReaderProgressEvent.EventType.SWIPE_DATA;

/**
 * Created by Akhani, Avdhesh on 5/22/17.
 */
public class CloverGoConnectorImpl {
    private final Observer<ReaderProgressEvent> progressObserver;
    private final CloverGoConnectorBroadcaster broadcaster;
    private final Observer<Payment> mPaymentResponseObserver;
    private ReaderProgressEvent readerProgressEvent;
    private static String TAG = "CloverGO";
    private Object mLastTransactionRequest;
    private ReaderInfo mLastTransactionReader;

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
    CaptureSignature captureSignature;
    @Inject
    SendReceipt sendReceipt;
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

    private Payment payment;
    private TransactionError transactionError;
    private Order mOrder;
    private HashMap<ReaderInfo.ReaderType, MerchantInfo> merchantInfoMap = new HashMap<>();
    private Disposable scanDisposable;
    private EmployeeMerchant mEmployeeMerchant;


    public CloverGoConnectorImpl(final CloverGoConnectorBroadcaster broadcaster, CloverGoDeviceConfiguration mCloverGoConfiguration) throws InitializationFailedException{
        this.broadcaster = broadcaster;

        String environment = mCloverGoConfiguration.getEnv().name();

        String url;
        switch (environment){
            case "LIVE":
                url = "https://api.payeezy.com/clovergosdk/v1/";
                break;
            case "DEMO":
                url = "https://api-int.payeezy.com/clovergosdk/v1/";
                break;
            case "SANDBOX":
                url = "https://api-cert.payeezy.com/clovergosdk/v1/";
                break;
            case "SANDBOX_DEV":
                url = "https://api-cert.payeezy.com/clovergosdk/v1/";
                break;
            default:
                url = "https://api-cat.payeezy.com/clovergosdk/v1/";
        }

        DaggerApplicationComponent.builder().sDKDataComponent(DaggerSDKDataComponent.builder().readerModule(new ReaderModule(mCloverGoConfiguration.getContext())).
                networkModule(new NetworkModule(url, mCloverGoConfiguration.getApiKey(), mCloverGoConfiguration.getSecret(), mCloverGoConfiguration.getAccessToken(), InstanceID.getInstance(mCloverGoConfiguration.getContext()).getId(), BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME)).
                utilityModule(new UtilityModule(mCloverGoConfiguration.getContext())).build()).build().inject(this);

        mGetMerchantsInfoOAuth.getCachedObservable().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<EmployeeMerchant>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(EmployeeMerchant employeeMerchant) {
                mEmployeeMerchant = employeeMerchant;
            }

            @Override
            public void onError(Throwable e) {
                throw new InitializationFailedException(e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });


        mPaymentResponseObserver =  new Observer<Payment>() {

            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG,"Sale onSubscribe");
            }

            @Override
            public void onNext(final Payment _payment) {
                payment = _payment;

                if ("EMV_CONTACT".equals(payment.getCard().getMode())) {

                    Map<String, String> data = new HashMap<>();
                    if (payment.getCard().getExtra() != null) {
                        data.put("Authorization_Response", payment.getCard().getExtra().getAuthResponseCode().equals("3130") ? "3030" : payment.getCard().getExtra().getAuthResponseCode());
                        data.put("Issuer_Auth_Data", payment.getCard().getExtra().getIssuerAuthData());
                        data.put("Auth_Code", HexUtils.convertASCII2HexaDecimal(payment.getCard().getExtra().getAuthCode()));
                        data.put("Result", "01");
                        data.put("issuer_script_template1", payment.getCard().getExtra().getIssuerScriptTemplate1());
                        data.put("issuer_script_template2", payment.getCard().getExtra().getIssuerScriptTemplate2());
                    } else {
                        data.put("Result", "01");
                        data.put("Auth_Code", HexUtils.convertASCII2HexaDecimal(payment.getAuthCode()));
                        data.put("Authorization_Response", "3030");
                    }
                    mWriteToCard.getObservable(mLastTransactionReader, data).subscribe();

                }else {
                    notifyPaymentResponse();
                }
            }

            @Override
            public void onError(Throwable e) {
                transactionError = TransactionError.convertToError(e);

                if ("duplicate_transaction".equals(transactionError.getCode())){
                    ConfirmPaymentRequest confirmPaymentRequest = new ConfirmPaymentRequest();
                    Challenge[] challenge = {new Challenge(transactionError.getMessage(), Challenge.ChallengeType.DUPLICATE_CHALLENGE, VoidReason.REJECT_DUPLICATE)};
                    confirmPaymentRequest.setChallenges(challenge);
                    broadcaster.notifyOnConfirmPaymentRequest(confirmPaymentRequest);
                    return;
                }else if (("charge_declined".equals(transactionError.getCode()) || "charge_declined_referral".equals(transactionError.getCode())) && readerProgressEvent != null && readerProgressEvent.getEventType() == ReaderProgressEvent.EventType.CHIP_DATA) {
                    mGetConnectedReaders.getBlockingObservable().subscribe(new Consumer<ReaderInfo>() {
                        @Override
                        public void accept(@NonNull ReaderInfo readerInfo) throws Exception {
                            final Map<String, String> completeEmvData = new HashMap<>();
                            if (transactionError.getClientData() != null && transactionError.getClientData().getAuthResponseCode() != null && !transactionError.getClientData().getAuthResponseCode().isEmpty()) {
                                completeEmvData.put("Result", "01");
                                completeEmvData.put("Authorization_Response", transactionError.getClientData().getAuthResponseCode());
                                completeEmvData.put("Issuer_Auth_Data", transactionError.getClientData().getIssuerAuthData());
                                completeEmvData.put("Auth_Code", HexUtils.convertASCII2HexaDecimal(TextUtils.isEmpty(transactionError.getClientData().getAuthCode()) ? "123456" : transactionError.getClientData().getAuthCode()));
                                completeEmvData.put("issuer_script_template1", transactionError.getClientData().getIssuerScriptTemplate1());
                                completeEmvData.put("issuer_script_template2", transactionError.getClientData().getIssuerScriptTemplate2());
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

            @Override
            public void onComplete() {
                Log.e(TAG,"Sale onComplete");
            }
        };

        EventBus.getObservable().ofType(ReaderInfo.class).subscribe(new Observer<ReaderInfo>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG,"ReaderInfo-Event onSubscribe");
            }

            @Override
            public void onNext(final ReaderInfo readerInfo) {
                Log.e(TAG,"ReaderInfo-Event onNext");
                if (readerInfo.isConnected()){

                    boolean supportsSales= false; boolean supportAuths= false; boolean supportsPreAuths= false; boolean supportsVaultCards= false; boolean supportsManualRefunds= false; boolean supportsVoids= false; boolean supportsTipAdjust = false;
                    for (String feature: mEmployeeMerchant.getMerchant().getFeatures()){
                        switch (feature){
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

                    MerchantInfo merchantInfo = new MerchantInfo(mEmployeeMerchant.getMerchant().getId(), mEmployeeMerchant.getMerchant().getName(), supportsSales, supportAuths, supportsPreAuths, supportsVaultCards, supportsManualRefunds, supportsVoids, supportsTipAdjust, readerInfo.getBluetoothName(), readerInfo.getSerialNo(), readerInfo.getReaderType().name());
                    Log.e(TAG,"Get Merchant Info");
                    merchantInfoMap.put(readerInfo.getReaderType(),merchantInfo);
                    broadcaster.notifyOnReady(merchantInfo);

                }else {
                    merchantInfoMap.put(readerInfo.getReaderType(), null);
                    broadcaster.notifyOnDisconnect(readerInfo);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG,"ReaderInfo-Event onError");
            }

            @Override
            public void onComplete() {
                Log.e(TAG,"ReaderInfo-Event onComplete");
            }
        });

        EventBus.getObservable().ofType(ReaderError.class).subscribe(new Observer<ReaderError>() {

            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG,"ReaderError-Event onSubscribe");
            }

            @Override
            public void onNext(ReaderError readerError) {
                readerProgressEvent = null;
                CloverDeviceErrorEvent deviceErrorEvent = null;

                switch (readerError.getErrorType()) {
                    case EMV_CARD_SWIPED_ERROR:
                        Log.e(TAG,"Chip Card "+ "Please insert card");
                        deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.EMV_CARD_SWIPED_ERROR,0,"Please insert card");
                        broadcaster.notifyOnDeviceError(deviceErrorEvent);
                        break;
                    case CONTACT_LESS_FAILED_TRY_CONTACT_ERROR:
                        Log.e(TAG,"Contactless failed "+ "Please insert/swipe card");
                        deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.CONTACT_LESS_FAILED_TRY_CONTACT_ERROR,0,"Contactless failed "+ "Please insert/swipe card");
                        broadcaster.notifyOnDeviceError(deviceErrorEvent);
                        break;
                    case MULTIPLE_CONTACT_LESS_CARD_DETECTED_ERROR:
                        deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.MULTIPLE_CONTACT_LESS_CARD_DETECTED_ERROR,0,"Multiple Contactless card Detected");
                        broadcaster.notifyOnDeviceError(deviceErrorEvent);
                        Log.e("Multiple Contactless", "Multiple Contactless card Detected");
                        break;
                    case DIP_FAILED_ALL_ATTEMPTS_ERROR:
                        Log.e(TAG,"Chip read failed" + "Please swipe card");
                        deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.DIP_FAILED_ALL_ATTEMPTS_ERROR,0,readerError.getMessage());
                        broadcaster.notifyOnDeviceError(deviceErrorEvent);
                        break;
                    case DIP_FAILED_ERROR:
                        Log.e(TAG,"Chip read failed "+"Please remove and insert card");
                        deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.DIP_FAILED_ERROR,0,readerError.getMessage());
                        broadcaster.notifyOnDeviceError(deviceErrorEvent);
                        break;
                    case SWIPE_FAILED_ERROR:
                        Log.e(TAG,"Swipe failed "+ "Please swipe again");
                        deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.SWIPE_FAILED_ERROR,0,"Please swipe again");
                        broadcaster.notifyOnDeviceError(deviceErrorEvent);
                        break;
                    case READER_ERROR:
                        Log.e(TAG,"Reader error "+"Reader error");
                        deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.READER_ERROR,0,readerError.getMessage());
                        broadcaster.notifyOnDeviceError(deviceErrorEvent);
                        break;
                    case CARD_ERROR:
                        Log.e("Card error", "Card error");
                        deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.CARD_ERROR,0,readerError.getMessage());
                        broadcaster.notifyOnDeviceError(deviceErrorEvent);
                        break;
                    case READER_TIMEOUT:
                        deviceErrorEvent = new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.READER_TIMEOUT,0,"Reader Transaction Time Out, Start Transaction Again");
                        broadcaster.notifyOnDeviceError(deviceErrorEvent);
                        Log.e("Reader Time OUT","Reader Time OUT" );
                        break;
                }

            }
            @Override
            public void onError(Throwable e) {
                Log.e(TAG,"ReaderError-Event onError");
            }
            @Override
            public void onComplete() {
                Log.e(TAG,"ReaderError-Event onComplete");
            }
        });


        progressObserver = new Observer<ReaderProgressEvent>() {

            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG,"ReaderProgressEvent-Event onSubscribe");
            }

            @Override
            public void onNext(ReaderProgressEvent readerProgressEvent) {

                EmvCard emvCard = null;
                CloverDeviceEvent deviceEvent;

                switch (readerProgressEvent.getEventType()){
                    case CANCEL_CARD_READ:
                        Log.e(TAG,"ReaderProgressEvent-Event onNext CANCEL_CARD_READ");
                        deviceEvent = new CloverDeviceEvent();
                        deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.CANCEL_CARD_READ);
                        deviceEvent.setMessage("Card Reader Transaction Cancelled");
                        broadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);
                        break;
                    case EMV_COMPLETE_DATA:
                        Log.e(TAG,"Please remove card");
                        deviceEvent = new CloverDeviceEvent();
                        deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.EMV_COMPLETE_DATA);
                        deviceEvent.setMessage("Please remove card");
                        broadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);

                        if (!(readerProgressEvent.getData().get("status").equals("Success") && "TC".equals(readerProgressEvent.getData().get("cryptogram_information_data")))) {

                            if (payment != null){
                                mVoidTransaction.getObservable(payment.getOrderId(), payment.getId()).subscribeOn(Schedulers.io()).subscribe();
                                payment = null;
                                transactionError = new TransactionError("chip_decline", "Transaction declined - Chip Decline");
                            }
                        }

                        break;
                    case CARD_INSERTED_MSG:
                        Log.e(TAG,"Leave card inserted");
                        deviceEvent = new CloverDeviceEvent();
                        deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.CARD_INSERTED_MSG);
                        deviceEvent.setMessage("Processing Transaction, Leave card inserted");
                        broadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);
                        break;
                    case LOW_BATTERY:
                        Log.e(TAG,"ReaderProgressEvent-Event onNext LOW_BATTERY");
                        deviceEvent = new CloverDeviceEvent();
                        deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.LOW_BATTERY);
                        deviceEvent.setMessage("Reader Battery low");
                        broadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);

                        break;
                    case CARD_REMOVED_MSG:
                        Log.e(TAG,"ReaderProgressEvent-Event onNext CARD_REMOVED_MSG");
                        deviceEvent = new CloverDeviceEvent();
                        deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.CARD_REMOVED_MSG);
                        deviceEvent.setMessage("Card Removed");
                        broadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);

                        if (payment != null)
                            notifyPaymentResponse();
                        else if (transactionError !=null)
                            notifyErrorResponse();
                        else {
                            transactionError = new TransactionError("unknown_error", "unknown_error");
                            notifyErrorResponse();
                        }
                        break;
                    case PLEASE_SEE_PHONE_MSG:
                        Log.e(TAG,"ReaderProgressEvent-Event onNext PLEASE_SEE_PHONE_MSG");
                        break;
                    case READER_READY:
                        Log.e(TAG,"ReaderProgressEvent-Event onNext READER_READY");
                        break;
                    case SWIPE_DATA:
                        Map<String, String> swipeData = readerProgressEvent.getData();
                        emvCard = new EmvCard(swipeData.get("ksn"),swipeData.get("encryptedTrack"),swipeData.get("track1"),swipeData.get("track2"),swipeData.get("pan"));
                        deviceEvent = new CloverDeviceEvent();
                        deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.CARD_SWIPED);
                        deviceEvent.setMessage("Processing transaction");
                        broadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);
                        break;
                    case CHIP_DATA:
                        Map<String, String> chipData = readerProgressEvent.getData();
                        emvCard = new EmvCard(chipData);
                        break;
                    case CONTACT_LESS_DATA:
                        Map<String, String> contactLessData = readerProgressEvent.getData();
                        emvCard = new EmvCard(contactLessData);
                        Log.e(TAG,"ReaderProgressEvent-Event onNext CONTACT_LESS_DATA");
                        Log.e(TAG,"Processing transaction");
                        deviceEvent = new CloverDeviceEvent();
                        deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.CARD_TAPPED);
                        deviceEvent.setMessage("Processing transaction");
                        broadcaster.notifyOnCloverGoDeviceActivity(deviceEvent);
                        break;
                    case APPLICATION_IDENTIFIERS:
                        Log.e(TAG,"APPLICATION_IDENTIFIERS "+readerProgressEvent.getData());
                        Set<String> keySet = readerProgressEvent.getData().keySet();
                        List<CardApplicationIdentifier> applicationIdentifiers = new ArrayList<CardApplicationIdentifier>();
                        for (String key : keySet) {
                            CardApplicationIdentifier cardApplicationIdentifier  = new CardApplicationIdentifier();
                            cardApplicationIdentifier.setApplicationLabel(key);
                            cardApplicationIdentifier.setApplicationIdentifier(readerProgressEvent.getData().get(key));
                            applicationIdentifiers.add(cardApplicationIdentifier);
                        }
                        broadcaster.notifyOnAidMatch(applicationIdentifiers, new ICloverGoConnectorListener.AidSelection(){
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
                                        return mReadCard.getObservable(readerInfo, selectedCardApplicationIdentifier.getApplicationIdentifier());
                                    }
                                }).onErrorComplete().subscribe();
                            }
                        });
                        break;
                }

                if (readerProgressEvent.getEventType() == SWIPE_DATA ||readerProgressEvent.getEventType() == CHIP_DATA || readerProgressEvent.getEventType() == CONTACT_LESS_DATA){
                    payment = null;
                    transactionError = null;
                    CloverGoConnectorImpl.this.readerProgressEvent = readerProgressEvent;
                    if (mLastTransactionRequest instanceof SaleRequest){
                        mAuthOrSaleTransaction.getObservable(mOrder,emvCard).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentResponseObserver);
                    }else if (mLastTransactionRequest instanceof AuthRequest){
                        mAuthOrSaleTransaction.getObservable(mOrder,emvCard).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentResponseObserver);
                    }else if (mLastTransactionRequest instanceof PreAuthRequest){
                        mPreAuthTransaction.getObservable(mOrder,emvCard).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mPaymentResponseObserver);
                    }
                }

            }
            @Override
            public void onError(Throwable t) {
                Log.e(TAG,"ReaderProgressEvent-Event onError");
            }
            @Override
            public void onComplete() {
                Log.e(TAG,"ReaderProgressEvent-Event onComplete");
            }
        };
        EventBus.getObservable().ofType(ReaderProgressEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(progressObserver);


    }

    public void initializeConnection(ReaderInfo.ReaderType readerType) {
        if (readerType == ReaderInfo.ReaderType.RP450){
            mScanForReaders.getObservable(null,15000).subscribe(new Observer<ReaderInfo>() {
                @Override
                public void onSubscribe(Disposable d) {
                    scanDisposable = d;
                }

                @Override
                public void onNext(ReaderInfo readerInfo) {
                    Log.e(TAG,"Scan-Reader onNext");
                    broadcaster.notifyOnDiscovered(readerInfo);
                }

                @Override
                public void onError(Throwable e) {
                }

                @Override
                public void onComplete() {
                }
            });
        }else if (readerType == ReaderInfo.ReaderType.RP350){
            ReaderInfo readerInfo =  ReaderInfo.createRP350ReaderType();
            mConnectToReader.getObservable(readerInfo.getReaderType(),readerInfo.getBluetoothName(),readerInfo.getBluetoothIdentifier()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
        }
    }

    public void connectToDevice(ReaderInfo readerInfo) {
        stopDeviceScan();
        mConnectToReader.getObservable(readerInfo.getReaderType(),readerInfo.getBluetoothName(),readerInfo.getBluetoothIdentifier()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    public void disconnectDevice(ReaderInfo.ReaderType readerType) {
        disconnectReader.getObservable(readerType).onErrorComplete().subscribe();
    }

    public void stopDeviceScan() {
        if (!scanDisposable.isDisposed())
            scanDisposable.dispose();
    }

    public void sale(SaleRequest saleRequest, final ReaderInfo.ReaderType readerType, boolean allowDuplicate) {
        if (merchantInfoMap.get(readerType) == null ) {
            SaleResponse saleResponse = new SaleResponse(false,ResultCode.CANCEL);
            saleResponse.setMessage("Connect Reader");
            saleResponse.setReason("Reader Not Connected");
            broadcaster.notifyOnSaleResponse(saleResponse);
            return;
        }
        if (!mEmployeeMerchant.getMerchant().getFeatures().contains("sales")){
            broadcaster.notifyOnSaleResponse(new SaleResponse(false, ResultCode.UNSUPPORTED));
            return;
        }

        mLastTransactionRequest = saleRequest;

        mOrder = new Order();
        TaxRate taxRate=null;
        mOrder.addCustomItem(new Order.CustomItem("item",((double)saleRequest.getAmount())/100,1, taxRate));
        if (saleRequest.getTipAmount() != null)
            mOrder.setTip(((double)saleRequest.getTipAmount())/100);
        mOrder.setExternalPaymentId(saleRequest.getExternalId());
        mOrder.allowDuplicates = allowDuplicate;

        mGetConnectedReaders.getBlockingObservable().filter(new Predicate<ReaderInfo>() {
            @Override
            public boolean test(@NonNull ReaderInfo readerInfo) throws Exception {
                return readerInfo.getReaderType() == readerType;
            }
        }).switchIfEmpty(Observable.<ReaderInfo>error(new Error("",""))).subscribe(new Observer<ReaderInfo>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ReaderInfo readerInfo) {
                mLastTransactionReader = readerInfo;
                mReadCard.getObservable(readerInfo, (int)(mOrder.getTotalAmount()*100)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
            }

            @Override
            public void onError(Throwable e) {
                SaleResponse saleResponse = new SaleResponse(false,ResultCode.CANCEL);
                saleResponse.setMessage("Connect Reader");
                saleResponse.setReason("Reader Not Connected");
                broadcaster.notifyOnSaleResponse(saleResponse);
            }

            @Override
            public void onComplete() {
            }
        });

    }

    public void preAuth(PreAuthRequest preAuthRequest, final ReaderInfo.ReaderType readerType, boolean allowDuplicate) {

        if (merchantInfoMap.get(readerType) == null ) {
            PreAuthResponse preAuthResponse = new PreAuthResponse(false,ResultCode.CANCEL);
            preAuthResponse.setMessage("Connect Reader");
            preAuthResponse.setReason("Reader Not Connected");
            broadcaster.notifyOnPreAuthResponse(preAuthResponse);
            return;
        }
        if (!mEmployeeMerchant.getMerchant().getFeatures().contains("preAuths")){
            broadcaster.notifyOnPreAuthResponse(new PreAuthResponse(false,ResultCode.UNSUPPORTED));
            return;
        }


        mLastTransactionRequest = preAuthRequest;
        mOrder = new Order();
        TaxRate taxRate=null;
        mOrder.addCustomItem(new Order.CustomItem("item",((double)preAuthRequest.getAmount())/100,1, taxRate));
        mOrder.setTip(-1);
        mOrder.setExternalPaymentId(preAuthRequest.getExternalId());
        mOrder.allowDuplicates = allowDuplicate;
        //TODO: setTax amount in Order

        mGetConnectedReaders.getBlockingObservable().filter(new Predicate<ReaderInfo>() {
            @Override
            public boolean test(@NonNull ReaderInfo readerInfo) throws Exception {
                return readerInfo.getReaderType() == readerType;
            }
        }).switchIfEmpty(Observable.<ReaderInfo>error(new Error("",""))).subscribe(new Observer<ReaderInfo>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ReaderInfo readerInfo) {
                mLastTransactionReader = readerInfo;
                mReadCard.getObservable(readerInfo, (int)(mOrder.getTotalAmount()*100)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
            }

            @Override
            public void onError(Throwable e) {
                PreAuthResponse preAuthResponse = new PreAuthResponse(false,ResultCode.CANCEL);
                preAuthResponse.setMessage("Connect Reader");
                preAuthResponse.setReason("Reader Not Connected");
                broadcaster.notifyOnPreAuthResponse(preAuthResponse);
            }

            @Override
            public void onComplete() {
            }
        });
    }

    public void auth(AuthRequest authRequest, final ReaderInfo.ReaderType readerType, boolean allowDuplicate) {

        if (merchantInfoMap.get(readerType) == null ) {
            AuthResponse authResponse = new AuthResponse(false,ResultCode.CANCEL);
            authResponse.setMessage("Connect Reader");
            authResponse.setReason("Reader Not Connected");
            broadcaster.notifyOnAuthResponse(authResponse);
            return;
        }
        if (!mEmployeeMerchant.getMerchant().getFeatures().contains("auths")){
            broadcaster.notifyOnAuthResponse(new AuthResponse(false,ResultCode.UNSUPPORTED));
            return;
        }

        mLastTransactionRequest = authRequest;
        mOrder = new Order();
        TaxRate taxRate=null;
        mOrder.addCustomItem(new Order.CustomItem("item",((double)authRequest.getAmount())/100,1, taxRate));
        mOrder.setTip(-1);
        mOrder.setExternalPaymentId(authRequest.getExternalId());
        mOrder.allowDuplicates = allowDuplicate;
        //TODO: setTax amount in Order

        mGetConnectedReaders.getBlockingObservable().filter(new Predicate<ReaderInfo>() {
            @Override
            public boolean test(@NonNull ReaderInfo readerInfo) throws Exception {
                return readerInfo.getReaderType() == readerType;
            }
        }).switchIfEmpty(Observable.<ReaderInfo>error(new Error("",""))).subscribe(new Observer<ReaderInfo>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ReaderInfo readerInfo) {
                mLastTransactionReader = readerInfo;
                mReadCard.getObservable(readerInfo, (int)(mOrder.getTotalAmount()*100)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
            }

            @Override
            public void onError(Throwable e) {
                AuthResponse authResponse = new AuthResponse(false,ResultCode.CANCEL);
                authResponse.setMessage("Connect Reader");
                authResponse.setReason("Reader Not Connected");
                broadcaster.notifyOnAuthResponse(authResponse);
            }

            @Override
            public void onComplete() {
            }
        });

        if (!mGetConnectedReaders.getBlockingObservable().isEmpty().blockingGet()) {
            mReadCard.getObservable(mGetConnectedReaders.getBlockingObservable().blockingFirst(), (int)(mOrder.getTotalAmount()*100)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
        }else {
            AuthResponse authResponse = new AuthResponse(false,ResultCode.CANCEL);
            authResponse.setMessage("Connect Reader");
            authResponse.setReason("Reader Not Connected");
            broadcaster.notifyOnAuthResponse(authResponse);
        }
    }

    public void tipAdjustAuth(final TipAdjustAuthRequest authTipAdjustRequest, ReaderInfo.ReaderType readerType) {
        if (mEmployeeMerchant.getMerchant().getFeatures().contains("tip_adjust")){
            mAddTips.getObservable(authTipAdjustRequest.getPaymentId(),((double)authTipAdjustRequest.getTipAmount()/100)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
                @Override
                public void onSubscribe(Disposable d) {}

                @Override
                public void onComplete() {
                    Log.e(TAG,"TipAdjustAuth onComplete");
                    TipAdjustAuthResponse tipAdjustAuthResponse = new TipAdjustAuthResponse(true,ResultCode.SUCCESS);
                    tipAdjustAuthResponse.setPaymentId(authTipAdjustRequest.getPaymentId());
                    tipAdjustAuthResponse.setTipAmount(authTipAdjustRequest.getTipAmount());
                    broadcaster.notifyOnTipAdjustAuthResponse(tipAdjustAuthResponse);
                }

                @Override
                public void onError(Throwable e) {
                    Error error = Error.convertToError(e);
                    Log.e(TAG,"TipAdjustAuth onError");
                    TipAdjustAuthResponse tipAdjustAuthResponse =  new TipAdjustAuthResponse(false,ResultCode.FAIL);
                    tipAdjustAuthResponse.setTipAmount(authTipAdjustRequest.getTipAmount());
                    tipAdjustAuthResponse.setPaymentId(authTipAdjustRequest.getPaymentId());
                    tipAdjustAuthResponse.setReason(error.getCode());
                    tipAdjustAuthResponse.setMessage(error.getMessage());
                    broadcaster.notifyOnTipAdjustAuthResponse(tipAdjustAuthResponse);
                }
            });
        }else {
            TipAdjustAuthResponse tipAdjustAuthResponse =  new TipAdjustAuthResponse(false,ResultCode.UNSUPPORTED);
            tipAdjustAuthResponse.setTipAmount(authTipAdjustRequest.getTipAmount());
            tipAdjustAuthResponse.setPaymentId(authTipAdjustRequest.getPaymentId());
            broadcaster.notifyOnTipAdjustAuthResponse(tipAdjustAuthResponse);
        }
    }

    public void capturePreAuth(final CapturePreAuthRequest capturePreAuthRequest) {
        mCaptureTransaction.getObservable(capturePreAuthRequest.getPaymentID(),((double)capturePreAuthRequest.getAmount())/100,((double)capturePreAuthRequest.getTipAmount())/100).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Payment>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Payment payment) {
                Log.e(TAG,"Capture Transaction onNext");
                CapturePreAuthResponse response = new CapturePreAuthResponse(true, ResultCode.SUCCESS);
                response.setPaymentID(payment.getId());

                response.setTipAmount((long)(payment.getTipCharged()*100));
                response.setAmount((long) (payment.getAmountCharged()* 100) - (long)(payment.getTipCharged()*100) );
                broadcaster.notifyOnCapturePreAuth(response);
            }

            @Override
            public void onError(Throwable e) {
                Error error = Error.convertToError(e);
                Log.e(TAG,"Capture Transaction onError");
                CapturePreAuthResponse response = new CapturePreAuthResponse(true, ResultCode.FAIL);
                response.setPaymentID(capturePreAuthRequest.getPaymentID());
                response.setAmount(capturePreAuthRequest.getAmount());
                response.setTipAmount(capturePreAuthRequest.getTipAmount());
                response.setReason(error.getCode());
                response.setMessage(error.getMessage());
                broadcaster.notifyOnCapturePreAuth(response);
            }

            @Override
            public void onComplete() {
            }
        });
    }

    public void voidPayment(final VoidPaymentRequest voidPaymentRequest) {
        if (mEmployeeMerchant.getMerchant().getFeatures().contains("voids")){
            mVoidTransaction.getObservable(voidPaymentRequest.getOrderId(),voidPaymentRequest.getPaymentId()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
                @Override
                public void onSubscribe(Disposable d) {}

                @Override
                public void onComplete() {
                    Log.e(TAG,"Void transaction onComplete");
                    VoidPaymentResponse response = new VoidPaymentResponse(true,ResultCode.SUCCESS);
                    response.setPaymentId(voidPaymentRequest.getPaymentId());
                    broadcaster.notifyOnVoidPaymentResponse(response);
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG,"Void transaction onError "+ e.getMessage());
                    Error error = Error.convertToError(e);
                    VoidPaymentResponse response = new VoidPaymentResponse(false,ResultCode.FAIL);
                    response.setPaymentId(voidPaymentRequest.getPaymentId());
                    response.setReason(error.getCode());
                    response.setMessage(error.getMessage());
                    broadcaster.notifyOnVoidPaymentResponse(response);
                }
            });
        }else {
            VoidPaymentResponse voidPaymentResponse = new VoidPaymentResponse(false,ResultCode.FAIL);
            voidPaymentResponse.setPaymentId(voidPaymentRequest.getPaymentId());
            broadcaster.notifyOnVoidPaymentResponse(voidPaymentResponse);
        }
    }

    public void refundPayment(final RefundPaymentRequest refundPaymentRequest) {
        if (refundPaymentRequest.getPaymentId() == null){
            RefundPaymentResponse response =  new RefundPaymentResponse(false,ResultCode.ERROR);
            response.setReason("payment_id_null");
            response.setMessage("Payment ID must not be null");

            broadcaster.notifyOnRefundPaymentResponse(response);
            return;
        }

        String amount = null;
        if (!refundPaymentRequest.isFullRefund()){
            amount = String.valueOf(refundPaymentRequest.getAmount());
        }
        mRefundTransaction.getObservable(refundPaymentRequest.getPaymentId(),amount).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Refund>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Refund refund) {
                RefundPaymentResponse response = new RefundPaymentResponse(true, ResultCode.SUCCESS);
                response.setPaymentId(refund.getPaymentId());
                response.setOrderId(refundPaymentRequest.getOrderId());
                com.clover.sdk.v3.payments.Refund _refund = new com.clover.sdk.v3.payments.Refund();
                _refund.setId(refund.getRefundId());
                _refund.setAmount((long)(refund.getAmount()* 100));
                response.setRefund(_refund);
                broadcaster.notifyOnRefundPaymentResponse(response);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG,"Refund transaction onError "+ e.getMessage());
                Error error = Error.convertToError(e);
                RefundPaymentResponse response = new RefundPaymentResponse(false,ResultCode.FAIL);
                response.setPaymentId(refundPaymentRequest.getPaymentId());
                response.setOrderId(refundPaymentRequest.getOrderId());
                response.setReason(error.getCode());
                response.setMessage(error.getMessage());
                broadcaster.notifyOnRefundPaymentResponse(response);

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
                Log.e(TAG,"CloseOut onComplete");
                CloseoutResponse closeoutResponse = new CloseoutResponse(true,ResultCode.SUCCESS);
                broadcaster.notifyCloseout(closeoutResponse);
            }

            @Override
            public void onError(Throwable e) {
                Error error = Error.convertToError(e);
                CloseoutResponse closeoutResponse = new CloseoutResponse(false,ResultCode.FAIL);
                closeoutResponse.setReason(error.getMessage());

                broadcaster.notifyCloseout(closeoutResponse);
            }
        });
    }

    public void acceptPayment(com.clover.sdk.v3.payments.Payment payment) {
        mOrder.allowDuplicates = true;
        EventBus.post(readerProgressEvent);
    }

    public void rejectPayment(com.clover.sdk.v3.payments.Payment payment, Challenge challenge) {
        if (readerProgressEvent != null && readerProgressEvent.getEventType() == ReaderProgressEvent.EventType.CHIP_DATA) {
            mGetConnectedReaders.getBlockingObservable().subscribe(new Consumer<ReaderInfo>() {
                @Override
                public void accept(@NonNull ReaderInfo readerInfo) throws Exception {
                    final Map<String, String> completeEmvData = new HashMap<>();
                    completeEmvData.put("Result", "02");
                    completeEmvData.put("Authorization_Response", "5A33");
                    mWriteToCard.getObservable(readerInfo, completeEmvData).subscribe();;
                }
            });
        }
    }

    public void captureSignature(String paymentId, int[][] xy){
        if (paymentId == null){
            throw new NullPointerException("Payment ID cannot be null");
        }else if (xy == null){
            throw new NullPointerException("Signature cannot be null");
        }else {
            captureSignature.getObservable(paymentId, xy).retry(3).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).onErrorComplete().subscribe();
        }
    }

    public void sendReceipt(String emailAddress, String phoneNo, String orderId){
        sendReceipt.getObservable(emailAddress != null && !emailAddress.isEmpty() ? emailAddress : null, phoneNo != null && !phoneNo.isEmpty() ? phoneNo : null, orderId).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).onErrorComplete().subscribe();
    }

    private void notifyPaymentResponse() {
        // com.clover.sdk.v3.payments.Payment _payment = new com.clover.sdk.v3.payments.Payment();
        GoPayment _payment = new GoPayment();

        _payment.setId(payment.getId());
        _payment.setAmount((long) (payment.getAmountCharged()* 100) - (long)(payment.getTipCharged()*100));
        _payment.setTipAmount((long)(payment.getTipCharged()*100));
        _payment.setExternalPaymentId(payment.getExternalPaymentId());
        _payment.setSignatureRequired(payment.getCard().isSignatureRequired());

        Reference orderRef = new Reference();
        orderRef.setId(payment.getOrderId());
        _payment.setOrder(orderRef);


        CardTransaction cardTransaction = new CardTransaction();
        cardTransaction.setCardholderName(payment.getCard().getCardholderName());
        cardTransaction.setCardType(CardType.valueOf(payment.getCard().getCardType()));

        if (mLastTransactionRequest instanceof SaleRequest){
            Log.e(TAG,"Sale onNext");
            SaleResponse response = new SaleResponse(true, ResultCode.SUCCESS);
            _payment.setResult(Result.SUCCESS);
            cardTransaction.setType(CardTransactionType.AUTH);

            _payment.setCardTransaction(cardTransaction);
            response.setPayment(_payment);

            broadcaster.notifyOnSaleResponse(response);

        }else if (mLastTransactionRequest instanceof AuthRequest){
            Log.e(TAG,"Auth onNext");
            AuthResponse response = new AuthResponse(true, ResultCode.SUCCESS);
            _payment.setResult(Result.SUCCESS);
            cardTransaction.setType(CardTransactionType.PREAUTH);

            _payment.setCardTransaction(cardTransaction);
            response.setPayment(_payment);

            broadcaster.notifyOnAuthResponse(response);

        }else if (mLastTransactionRequest instanceof PreAuthRequest){
            Log.e(TAG,"Pre-Auth onNext");
            PreAuthResponse response = new PreAuthResponse(true, ResultCode.SUCCESS);
            _payment.setResult(Result.AUTH);;
            cardTransaction.setType(CardTransactionType.PREAUTH);

            _payment.setCardTransaction(cardTransaction);
            response.setPayment(_payment);

            broadcaster.notifyOnPreAuthResponse(response);
        }
    }

    private void notifyErrorResponse() {
        if (mLastTransactionRequest instanceof SaleRequest){
            Log.e(TAG,"Sale onError "+ transactionError.getMessage());
            SaleResponse response = new SaleResponse(false, ResultCode.FAIL);
            response.setReason(transactionError.getCode());
            response.setMessage(transactionError.getMessage());
            broadcaster.notifyOnSaleResponse(response);
        }else if (mLastTransactionRequest instanceof AuthRequest){
            Log.e(TAG,"Auth onError "+ transactionError.getMessage());
            AuthResponse response = new AuthResponse(false, ResultCode.FAIL);
            response.setReason(transactionError.getCode());
            response.setMessage(transactionError.getMessage());
            broadcaster.notifyOnAuthResponse(response);
        }else if (mLastTransactionRequest instanceof PreAuthRequest){
            Log.e(TAG,"Pre-Auth onError "+ transactionError.getMessage());
            PreAuthResponse response = new PreAuthResponse(false, ResultCode.FAIL);
            response.setReason(transactionError.getCode());
            response.setMessage(transactionError.getMessage());
            broadcaster.notifyOnPreAuthResponse(response);
        }
    }

    public void cancel(final ReaderInfo.ReaderType readerType) {
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
