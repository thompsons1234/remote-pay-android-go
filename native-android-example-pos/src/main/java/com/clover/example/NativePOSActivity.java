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

package com.clover.example;

import com.clover.connector.sdk.v3.DisplayConnector;
import com.clover.connector.sdk.v3.PaymentConnector;
import com.clover.example.model.POSCard;
import com.clover.example.model.POSDiscount;
import com.clover.example.model.POSExchange;
import com.clover.example.model.POSItem;
import com.clover.example.model.POSNakedRefund;
import com.clover.example.model.POSOrder;
import com.clover.example.model.POSPayment;
import com.clover.example.model.POSRefund;
import com.clover.example.model.POSStore;
import com.clover.example.utils.IdUtils;
import com.clover.sdk.v3.base.CardData;
import com.clover.sdk.v3.base.Challenge;
import com.clover.sdk.v3.connector.IDisplayConnector;
import com.clover.sdk.v3.connector.IDisplayConnectorListener;
import com.clover.sdk.v3.payments.CardTransactionState;
import com.clover.sdk.v3.payments.CardTransactionType;
import com.clover.sdk.v3.remotepay.AuthResponse;
import com.clover.sdk.v3.remotepay.CapturePreAuthResponse;
import com.clover.sdk.v3.remotepay.CloseoutRequest;
import com.clover.sdk.v3.remotepay.CloseoutResponse;
import com.clover.sdk.v3.remotepay.ConfirmPaymentRequest;
import com.clover.sdk.v3.remotepay.ManualRefundRequest;
import com.clover.sdk.v3.remotepay.ManualRefundResponse;
import com.clover.sdk.v3.remotepay.PaymentResponse;
import com.clover.sdk.v3.remotepay.PreAuthRequest;
import com.clover.sdk.v3.remotepay.PreAuthResponse;
import com.clover.sdk.v3.remotepay.ReadCardDataRequest;
import com.clover.sdk.v3.remotepay.ReadCardDataResponse;
import com.clover.sdk.v3.remotepay.RefundPaymentResponse;
import com.clover.sdk.v3.remotepay.ResponseCode;
import com.clover.sdk.v3.remotepay.RetrievePaymentRequest;
import com.clover.sdk.v3.remotepay.RetrievePaymentResponse;
import com.clover.sdk.v3.remotepay.RetrievePendingPaymentsResponse;
import com.clover.sdk.v3.remotepay.SaleResponse;
import com.clover.sdk.v3.remotepay.TipAdjustAuthResponse;
import com.clover.sdk.v3.remotepay.VaultCardResponse;
import com.clover.sdk.v3.remotepay.VerifySignatureRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentResponse;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v3.connector.IPaymentConnector;
import com.clover.sdk.v3.connector.IPaymentConnectorListener;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.remotepay.TipAdded;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;

public class NativePOSActivity extends Activity implements CurrentOrderFragment.OnFragmentInteractionListener,
    AvailableItem.OnFragmentInteractionListener, OrdersFragment.OnFragmentInteractionListener,
    RegisterFragment.OnFragmentInteractionListener, SignatureFragment.OnFragmentInteractionListener,
    CardsFragment.OnFragmentInteractionListener, ManualRefundsFragment.OnFragmentInteractionListener, MiscellaneousFragment.OnFragmentInteractionListener,
    ProcessingFragment.OnFragmentInteractionListener, PreAuthFragment.OnFragmentInteractionListener {

  private static final String TAG = "NativePOSActivity";
  public static final String EXAMPLE_POS_SERVER_KEY = "clover_device_endpoint";
  public static final int WS_ENDPOINT_ACTIVITY = 123;
  public static final int SVR_ACTIVITY = 456;
  public static final String EXTRA_CLOVER_CONNECTOR_CONFIG = "EXTRA_CLOVER_CONNECTOR_CONFIG";
  public static final String EXTRA_WS_ENDPOINT = "WS_ENDPOINT";
  private static final String DEFAULT_EID = "DFLTEMPLYEE";

  // Package name for example custom activities
  public static final String CUSTOM_ACTIVITY_PACKAGE = "com.clover.cfp.examples.";

  private Dialog ratingsDialog;
  private ListView ratingsList;
  private ArrayAdapter<String> ratingsAdapter;

  Payment currentPayment = null;
  ArrayList<Challenge> currentChallenges = null;
  final PaymentConfirmationListener paymentConfirmationListener = new PaymentConfirmationListener() {
    @Override
    public void onRejectClicked(Challenge challenge) { // Reject payment and send the challenge along for logging/reason
      cloverConnector.rejectPayment(currentPayment, challenge);
      currentChallenges = null;
      currentPayment = null;
    }

    @Override
    public void onAcceptClicked(final int challengeIndex) {
      if (challengeIndex == currentChallenges.size() - 1) { // no more challenges, so accept the payment
        cloverConnector.acceptPayment(currentPayment);
        currentChallenges = null;
        currentPayment = null;
      } else { // show the next challenge
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            showPaymentConfirmation(paymentConfirmationListener, currentChallenges.get(challengeIndex + 1), challengeIndex + 1);
          }
        });
      }
    }
  };

  boolean usb = true;


  IPaymentConnector cloverConnector;
  IDisplayConnector displayConnector;

  final IPaymentConnectorListener ccListener = new IPaymentConnectorListener() {

    public void onDeviceDisconnected() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(NativePOSActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
          Log.d(TAG, "disconnected");
          ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText("Disconnected");
        }
      });

    }

    public void onDeviceConnected() {

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          showMessage("Ready!", Toast.LENGTH_SHORT);
          ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText("Connected!");
        }
      });
    }


    @Override
    public void onReadCardDataResponse(final ReadCardDataResponse response) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          AlertDialog.Builder builder = new AlertDialog.Builder(NativePOSActivity.this);
          builder.setTitle("Read Card Data Response");
          if (response.getSuccess()) {

            LayoutInflater inflater = NativePOSActivity.this.getLayoutInflater();

            View view = inflater.inflate(R.layout.card_data_table, null);
            ListView listView = (ListView) view.findViewById(R.id.cardDataListView);


            if (listView != null) {
              class RowData {
                RowData(String label, String value) {
                  this.text1 = label;
                  this.text2 = value;
                }

                String text1;
                String text2;
              }

              ArrayAdapter<RowData> data = new ArrayAdapter<RowData>(getBaseContext(), android.R.layout.simple_list_item_2) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                  View v = convertView;

                  if (v == null) {
                    LayoutInflater vi;
                    vi = LayoutInflater.from(getContext());
                    v = vi.inflate(android.R.layout.simple_list_item_2, null);
                  }

                  RowData rowData = getItem(position);

                  if (rowData != null) {
                    TextView primaryColumn = (TextView) v.findViewById(android.R.id.text1);
                    TextView secondaryColumn = (TextView) v.findViewById(android.R.id.text2);

                    primaryColumn.setText(rowData.text2);
                    secondaryColumn.setText(rowData.text1);
                  }

                  return v;
                }
              };
              listView.setAdapter(data);
              CardData cardData = response.getCardData();
              data.addAll(new RowData("Encrypted", cardData.getEncrypted() + ""));
              data.addAll(new RowData("Cardholder Name", cardData.getCardholderName()));
              data.addAll(new RowData("First Name", cardData.getFirstName()));
              data.addAll(new RowData("Last Name", cardData.getLastName()));
              data.addAll(new RowData("Expiration", cardData.getExp()));
              data.addAll(new RowData("First 6", cardData.getFirst6()));
              data.addAll(new RowData("Last 4", cardData.getLast4()));
              data.addAll(new RowData("Track 1", cardData.getTrack1()));
              data.addAll(new RowData("Track 2", cardData.getTrack2()));
              data.addAll(new RowData("Track 3", cardData.getTrack3()));
              data.addAll(new RowData("Masked Track 1", cardData.getMaskedTrack1()));
              data.addAll(new RowData("Masked Track 2", cardData.getMaskedTrack2()));
              data.addAll(new RowData("Masked Track 3", cardData.getMaskedTrack3()));
              data.addAll(new RowData("Pan", cardData.getPan()));

            }
            builder.setView(view);

          } else if (response.getResult() == ResponseCode.CANCEL) {
            builder.setMessage("Get card data canceled.");
          } else {
            builder.setMessage("Error getting card data. " + response.getReason() + ": " + response.getMessage());
          }

          builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
          });
          AlertDialog dialog = builder.create();
          dialog.show();

        }
      });
    }

    /**
     * Called in response to a closeout being processed
     *
     * @param response
     */
    @Override
    public void onCloseoutResponse(CloseoutResponse response) {
      if (response.getSuccess()) {
        showMessage("Closeout successful for Batch ID: " + response.getBatch().getId(), Toast.LENGTH_LONG);

      } else {
        showMessage("Closeout error: " + response.getResult(), Toast.LENGTH_LONG);
      }
    }

    /**
     * Called in response to a doRetrievePayment(...) request
     *
     * @param response
     */
    @Override
    public void onRetrievePaymentResponse(RetrievePaymentResponse response) {
      if (response.getSuccess()) {
        showMessage("Retrieve Payment successful for Payment ID: " + response.getExternalPaymentId()
                    + " QueryStatus: " + response.getQueryStatus()
                    + " Payment: " + response.getPayment()
                    + " reason: " + response.getReason(), Toast.LENGTH_LONG);
      } else {
        showMessage("Retrieve Payment error: " + response.getResult(), Toast.LENGTH_LONG);
      }
    }

    @Override
    public void onAuthResponse(final AuthResponse response) {
      if (response.getSuccess()) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Payment _payment = response.getPayment();
            long cashback = _payment.getCashbackAmount() == null ? 0 : _payment.getCashbackAmount();
            long tip = _payment.getTipAmount() == null ? 0 : _payment.getTipAmount();
            POSPayment payment = new POSPayment(_payment.getId(), _payment.getExternalPaymentId(), _payment.getOrder().getId(), DEFAULT_EID, _payment.getAmount(), tip, cashback);
            setPaymentStatus(payment, response);
            store.addPaymentToOrder(payment, store.getCurrentOrder());
            showMessage("Auth successfully processed.", Toast.LENGTH_SHORT);

            store.createOrder(false);
            CurrentOrderFragment currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
            currentOrderFragment.setOrder(store.getCurrentOrder());

            showRegister(null);
            displayConnector.showWelcomeScreen();
/*
              SystemClock.sleep(3000);
              cloverConnector.showWelcomeScreen();
*/
          }
        });
      } else {
        showMessage("Auth error:" + response.getResult(), Toast.LENGTH_LONG);
        displayConnector.showMessage("There was a problem processing the transaction");
/*
          cloverConnector.showMessage("There was a problem processing the transaction");
          SystemClock.sleep(3000);
*/
      }
    }

    @Override
    public void onPreAuthResponse(final PreAuthResponse response) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (response.getSuccess()) {
            Payment _payment = response.getPayment();
            long cashback = _payment.getCashbackAmount() == null ? 0 : _payment.getCashbackAmount();
            long tip = _payment.getTipAmount() == null ? 0 : _payment.getTipAmount();
            POSPayment payment = new POSPayment(_payment.getId(), _payment.getExternalPaymentId(), _payment.getOrder().getId(), DEFAULT_EID, _payment.getAmount(), tip, cashback);
            setPaymentStatus(payment, response);
            store.addPreAuth(payment);
            showMessage("PreAuth successfully processed.", Toast.LENGTH_SHORT);
            showPreAuths(null);
          } else {
            showMessage("PreAuth: " + response.getResult(), Toast.LENGTH_LONG);
          }
        }
      });
      displayConnector.showWelcomeScreen();
/*
        SystemClock.sleep(3000);
        cloverConnector.showWelcomeScreen();
*/
    }

    @Override
    public void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse response) {
      if (!response.getSuccess()) {
        store.setPendingPayments(null);
        showMessage("Retrieve Pending Payments: " + response.getResult(), Toast.LENGTH_LONG);
      } else {
        store.setPendingPayments(response.getPendingPaymentEntries());
      }
    }

    @Override
    public void onTipAdjustAuthResponse(TipAdjustAuthResponse response) {
      if (response.getSuccess()) {

        boolean updatedTip = false;
        for (POSOrder order : store.getOrders()) {
          for (POSExchange exchange : order.getPayments()) {
            if (exchange instanceof POSPayment) {
              POSPayment posPayment = (POSPayment) exchange;
              if (exchange.getPaymentID().equals(response.getPaymentId())) {
                posPayment.setTipAmount(response.getTipAmount());
                // TODO: should the stats be updated?
                updatedTip = true;
                break;
              }
            }
          }
          if (updatedTip) {
            showMessage("Tip successfully adjusted", Toast.LENGTH_LONG);
            break;
          }
        }
      } else {
        showMessage("Tip adjust failed", Toast.LENGTH_LONG);
      }
    }

    @Override
    public void onCapturePreAuthResponse(CapturePreAuthResponse response) {

      if (response.getSuccess()) {
        for (final POSPayment payment : store.getPreAuths()) {
          if (payment.getPaymentID().equals(response.getPaymentId())) {
            final long paymentAmount = response.getAmount();
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                store.removePreAuth(payment);
                store.addPaymentToOrder(payment, store.getCurrentOrder());
                payment.setPaymentStatus(POSPayment.Status.AUTHORIZED);
                payment.amount = paymentAmount;
                showMessage("Sale successfully processing using Pre Authorization", Toast.LENGTH_LONG);

                //TODO: if order isn't fully paid, don't create a new order...
                store.createOrder(false);
                CurrentOrderFragment currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
                currentOrderFragment.setOrder(store.getCurrentOrder());
                showOrders(null);
              }
            });
            break;
          } else {
            showMessage("PreAuth Capture: Payment received does not match any of the stored PreAuth records", Toast.LENGTH_LONG);
          }
        }
      } else {
        showMessage("PreAuth Capture Error: Payment failed with response code = " + response.getResult() + " and reason: " + response.getReason(), Toast.LENGTH_LONG);
      }
    }

    @Override
    public void onVerifySignatureRequest(VerifySignatureRequest request) {

      FragmentManager fragmentManager = getFragmentManager();
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

      hideFragments(fragmentManager, fragmentTransaction);

      Fragment fragment = fragmentManager.findFragmentByTag("SIGNATURE");
      if (fragment == null) {
        fragment = SignatureFragment.newInstance(request, cloverConnector);
        fragmentTransaction.add(R.id.contentContainer, fragment, "SIGNATURE");
      } else {
        ((SignatureFragment) fragment).setVerifySignatureRequest(request);
        fragmentTransaction.show(fragment);
      }

      fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void onConfirmPaymentRequest(ConfirmPaymentRequest request) {
      if (request.getPayment() == null || request.getChallenges() == null) {
        showMessage("Error: The ConfirmPaymentRequest was missing the payment and/or challenges.", Toast.LENGTH_LONG);
      } else {
        currentPayment = request.getPayment();
        currentChallenges = new ArrayList<>(request.getChallenges());
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            showPaymentConfirmation(paymentConfirmationListener, currentChallenges.get(0), 0);
          }
        });
      }
    }

    @Override
    public void onSaleResponse(final SaleResponse response) {
      if (response != null) {
        if (response.getSuccess()) { // Handle cancel response
          if (response.getPayment() != null) {
            Payment _payment = response.getPayment();
            long cashback = _payment.getCashbackAmount() == null ? 0 : _payment.getCashbackAmount();
            long tip = _payment.getTipAmount() == null ? 0 : _payment.getTipAmount();
            POSPayment payment = new POSPayment(_payment.getId(), _payment.getExternalPaymentId(), _payment.getOrder().getId(), DEFAULT_EID, _payment.getAmount(), tip, cashback);
            setPaymentStatus(payment, response);

            store.addPaymentToOrder(payment, store.getCurrentOrder());
            showMessage("Sale successfully processed", Toast.LENGTH_SHORT);
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                store.createOrder(false);
                CurrentOrderFragment currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
                currentOrderFragment.setOrder(store.getCurrentOrder());
                showRegister(null);
              }
            });
          } else { // Handle null payment
            showMessage("Error: Sale response was missing the payment", Toast.LENGTH_LONG);
          }
        } else {
          showMessage(response.getResult().toString() + ":" + response.getReason() + "  " + response.getMessage(), Toast.LENGTH_LONG);
        }
      } else { //Handle null payment response
        showMessage("Error: Null SaleResponse", Toast.LENGTH_LONG);
      }
      displayConnector.showWelcomeScreen();
/*
        SystemClock.sleep(3000);
        cloverConnector.showWelcomeScreen();
*/
    }

    @Override
    public void onManualRefundResponse(final ManualRefundResponse response) {
      if (response.getSuccess()) {
        Credit credit = response.getCredit();
        final POSNakedRefund nakedRefund = new POSNakedRefund(null, credit.getAmount());
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            store.addRefund(nakedRefund);
            showMessage("Manual Refund successfully processed", Toast.LENGTH_SHORT);
          }
        });
      } else if (response.getResult() == ResponseCode.CANCEL) {
        showMessage("User canceled the Manual Refund", Toast.LENGTH_SHORT);
      } else {
        showMessage("Manual Refund Failed with code: " + response.getResult() + " - " + response.getMessage(), Toast.LENGTH_LONG);
      }
    }

    @Override
    public void onRefundPaymentResponse(final RefundPaymentResponse response) {
      if (response.getSuccess()) {
        POSRefund refund = new POSRefund(response.getRefund().getId(), response.getPaymentId(), response.getOrderId(), "DEFAULT", response.getRefund().getAmount());
        boolean done = false;
        for (POSOrder order : store.getOrders()) {
          for (POSExchange payment : order.getPayments()) {
            if (payment instanceof POSPayment) {
              if (payment.getPaymentID().equals(response.getRefund().getPayment().getId())) {
                ((POSPayment) payment).setPaymentStatus(POSPayment.Status.REFUNDED);
                store.addRefundToOrder(refund, order);
                showMessage("Payment successfully refunded", Toast.LENGTH_SHORT);
                done = true;
                break;
              }
            }
          }
          if (done) {
            break;
          }
        }
      } else {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(NativePOSActivity.this);
            builder.setTitle("Refund Error").setMessage("There was an error refunding the payment");
            builder.create().show();
            Log.d(getClass().getName(), "Got refund response of " + response.getReason());
          }
        });
      }
    }

    /**
     * Called when a customer selects a tip amount on the Clover device screen
     *
     * @param tipAdded
     */
    @Override
    public void onTipAdded(TipAdded tipAdded) {

    }

    @Override
    public void onVoidPaymentResponse(VoidPaymentResponse response) {
      if (response.getSuccess()) {
        boolean done = false;
        for (POSOrder order : store.getOrders()) {
          for (POSExchange payment : order.getPayments()) {
            if (payment instanceof POSPayment) {
              if (payment.getPaymentID().equals(response.getPaymentId())) {
                ((POSPayment) payment).setPaymentStatus(POSPayment.Status.VOIDED);
                showMessage("Payment was voided", Toast.LENGTH_SHORT);
                done = true;
                break;
              }
            }
          }
          if (done) {
            break;
          }
        }
      } else {
        showMessage(getClass().getName() + ":Got VoidPaymentResponse of " + response.getResult(), Toast.LENGTH_LONG);
      }
    }

    @Override
    public void onVaultCardResponse(final VaultCardResponse response) {
      if (response.getSuccess()) {
        POSCard card = new POSCard();
        card.setFirst6(response.getCard().getFirst6());
        card.setLast4(response.getCard().getLast4());
        card.setName(response.getCard().getCardholderName());
        card.setMonth(response.getCard().getExpirationDate().substring(0, 2));
        card.setYear(response.getCard().getExpirationDate().substring(2, 4));
        card.setToken(response.getCard().getToken());
        store.addCard(card);
        showMessage("Card successfully vaulted", Toast.LENGTH_SHORT);
      } else {
        if (response.getResult() == ResponseCode.CANCEL) {
          showMessage("User canceled the operation", Toast.LENGTH_SHORT);
          //cloverConnector.showWelcomeScreen();
          displayConnector.showWelcomeScreen();
        } else {
          showMessage("Error capturing card: " + response.getResult(), Toast.LENGTH_LONG);
          displayConnector.showMessage("Card was not saved");
          //cloverConnector.showMessage("Card was not saved");
          SystemClock.sleep(4000); //wait 4 seconds
          //cloverConnector.showWelcomeScreen();
          displayConnector.showWelcomeScreen();
        }
      }
    }

  };

  POSStore store = new POSStore();
  private AlertDialog pairingCodeDialog;

  private SharedPreferences sharedPreferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_example_pos);
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    initStore();
  }

  private void initDisplayConnector() {
    disposeDisplayConnector();
    // Retrieve the Clover account
    Account account = null;
    account = CloverAccount.getAccount(this);

    // If an account can't be acquired, exit the app
    if (account == null) {
      Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    Log.d(TAG, String.format("Account is=%s", account));
    /*
     * Just listens for connection events.
     */
    IDisplayConnectorListener listener = new IDisplayConnectorListener() {
      @Override
      public void onDeviceDisconnected() {
        Log.d(TAG, "onDeviceDisconnected");
      }

      @Override
      public void onDeviceConnected() {
        Log.d(TAG, "onDeviceConnected");
      }
    };
    displayConnector = new DisplayConnector(this, account, listener);
  }

  /**
   * Destroy this classes DisplayConnector and dispose of it.
   */
  private void disposeDisplayConnector() {
    if (displayConnector != null) {
      displayConnector.dispose();
      displayConnector = null;
    }
  }

  private void initDisplayConnector() {
    disposeDisplayConnector();
    // Retrieve the Clover account
    Account account = null;
    account = CloverAccount.getAccount(this);

    // If an account can't be acquired, exit the app
    if (account == null) {
      Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    Log.d(TAG, String.format("Account is=%s", account));
    /*
     * Just listens for connection events.
     */
    IDisplayConnectorListener listener = new IDisplayConnectorListener() {
      @Override
      public void onDeviceDisconnected() {
        Log.d(TAG, "onDeviceDisconnected");
      }

      @Override
      public void onDeviceConnected() {
        Log.d(TAG, "onDeviceConnected");
      }
    };
    displayConnector = new DisplayConnector(this, account, listener);
  }

  /**
   * Destroy this classes DisplayConnector and dispose of it.
   */
  private void disposeDisplayConnector() {
    if (displayConnector != null) {
      displayConnector.dispose();
      displayConnector = null;
    }
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    // save state here
    savedInstanceState.putSerializable("POSStore", store);

    //Call the super and let it do its thing
    super.onSaveInstanceState(savedInstanceState);
  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState) {
    // Always call the superclass so it can restore the view hierarchy
    super.onRestoreInstanceState(savedInstanceState);


    // Restore state members from saved instance
    store = (POSStore)savedInstanceState.getSerializable("POSStore");
    initStore();
  }


  @Override
  protected void onPause() {
/*    if (ccListener != null) {
      if (cloverConnector != null) {
        cloverConnector.removeCloverConnectorListener(ccListener);
      } */
    //cloverConnector = null;
//    }
    displayConnector.showWelcomeScreen();
    displayConnector.dispose();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();

    initStore();
    if (cloverConnector == null) {
      cloverConnector = new PaymentConnector(getApplicationContext(), CloverAccount.getAccount(getApplicationContext()), ccListener);
    } else {
      cloverConnector.addCloverConnectorListener(ccListener);
    }

    cloverConnector.initializeConnection();
    initDisplayConnector();
    updateComponentsWithNewConnectors();


    //FrameLayout frameLayout = (FrameLayout) findViewById(R.id.contentContainer);

    //FragmentManager fragmentManager = getFragmentManager();
    //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    //RegisterFragment register = RegisterFragment.newInstance(store, cloverConnector);

    //fragmentTransaction.add(R.id.contentContainer, register, "REGISTER");
    //fragmentTransaction.commit();
  }

  private KeyStore createTrustStore() {
    try {

      String STORETYPE = "PKCS12";
      KeyStore trustStore = KeyStore.getInstance(STORETYPE);
      InputStream trustStoreStream = getClass().getResourceAsStream("/certs/clover_cacerts.p12");
      String TRUST_STORE_PASSWORD = "clover";

      trustStore.load(trustStoreStream, TRUST_STORE_PASSWORD.toCharArray());

      return trustStore;
    } catch (Throwable t) {
      Log.e(getClass().getSimpleName(), "Error loading trust store", t);
      t.printStackTrace();
      return null;
    }

  }

  private void initStore() {
    if (store == null) {
      store = new POSStore();
    }
    if (store.getAvailableItems().size() == 0) {
      // initialize store...
      store.addAvailableItem(new POSItem("0", "Chicken Nuggets", 539, true, true));
      store.addAvailableItem(new POSItem("1", "Hamburger", 699, true, true));
      store.addAvailableItem(new POSItem("2", "Cheeseburger", 759, true, true));
      store.addAvailableItem(new POSItem("3", "Double Hamburger", 819, true, true));
      store.addAvailableItem(new POSItem("4", "Double Cheeseburger", 899, true, true));
      store.addAvailableItem(new POSItem("5", "Bacon Cheeseburger", 999, true, true));
      store.addAvailableItem(new POSItem("6", "Small French Fries", 239, true, true));
      store.addAvailableItem(new POSItem("7", "Medium French Fries", 259, true, true));
      store.addAvailableItem(new POSItem("8", "Large French Fries", 279, true, true));
      store.addAvailableItem(new POSItem("9", "Small Fountain Drink", 169, true, true));
      store.addAvailableItem(new POSItem("10", "Medium Fountain Drink", 189, true, true));
      store.addAvailableItem(new POSItem("11", "Large Fountain Drink", 229, true, true));
      store.addAvailableItem(new POSItem("12", "Chocolate Milkshake", 449, true, true));
      store.addAvailableItem(new POSItem("13", "Vanilla Milkshake", 419, true, true));
      store.addAvailableItem(new POSItem("14", "Strawberry Milkshake", 439, true, true));
      store.addAvailableItem(new POSItem("15", "Ice Cream Cone", 189, true, true));
      store.addAvailableItem(new POSItem("16", "$25 Gift Card", 2500, false, false));
      store.addAvailableItem(new POSItem("17", "$50 Gift Card", 5000, false, false));

      store.addAvailableDiscount(new POSDiscount("10% Off", 0.1f));
      store.addAvailableDiscount(new POSDiscount("$5 Off", 500));
      store.addAvailableDiscount(new POSDiscount("None", 0));
      store.createOrder(false);
    }

    // Per Transaction Settings defaults
    //store.setTipMode(SaleRequest.TipMode.ON_SCREEN_BEFORE_PAYMENT);
    //store.setSignatureEntryLocation(DataEntryLocation.ON_PAPER);
    //store.setDisablePrinting(false);
    //store.setDisableReceiptOptions(false);
    //store.setDisableDuplicateChecking(false);
    //store.setAllowOfflinePayment(false);
    //store.setForceOfflinePayment(false);
    //store.setApproveOfflinePaymentWithoutPrompt(true);
    //store.setAutomaticSignatureConfirmation(true);
    //store.setAutomaticPaymentConfirmation(true);

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == WS_ENDPOINT_ACTIVITY) {
      if (!usb) {
        initialize();
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_parent, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void initialize() {

  }

  private void setPaymentStatus(POSPayment payment, PaymentResponse response) {
    if (response.hasPayment()){
      if (response.getPayment().getCardTransaction().hasType()) {
        CardTransactionType type = response.getPayment().getCardTransaction().getType();
        if (type.equals(CardTransactionType.AUTH)) {
          payment.setPaymentStatus(POSPayment.Status.PAID);
        } else if (type.equals(CardTransactionType.PREAUTH) && response.getPayment().getCardTransaction().getState().equals(CardTransactionState.PENDING)) {
          payment.setPaymentStatus(POSPayment.Status.AUTHORIZED);
        } else if (response.getIsPreAuth()) {
          payment.setPaymentStatus(POSPayment.Status.PREAUTHORIZED);
        }
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (cloverConnector != null) {
      cloverConnector.dispose();
    }
  }

  @Override
  public void onFragmentInteraction(Uri uri) {

  }

  private void showPaymentConfirmation(PaymentConfirmationListener listenerIn, Challenge challengeIn, int challengeIndexIn) {
    final int challengeIndex = challengeIndexIn;
    final Challenge challenge = challengeIn;
    final PaymentConfirmationListener listener = listenerIn;
    AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(this);
    confirmationDialog.setTitle("Payment Confirmation");
    confirmationDialog.setCancelable(false);
    confirmationDialog.setMessage(challenge.getMessage());
    confirmationDialog.setNegativeButton("Reject", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        listener.onRejectClicked(challenge);
        dialog.dismiss();
      }
    });
    confirmationDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        listener.onAcceptClicked(challengeIndex);
        dialog.dismiss();
      }
    });
    confirmationDialog.show();
  }

  private void showMessage(final String msg, final int duration) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(NativePOSActivity.this, msg, duration).show();
      }
    });
  }

  public void showSettings(MenuItem item) {
    if (!usb) {
      Intent intent = new Intent(this, ExamplePOSSettingsActivity.class);
      startActivityForResult(intent, WS_ENDPOINT_ACTIVITY);
    }
  }

  private void updateComponentsWithNewConnectors() {
    FragmentManager fragmentManager = getFragmentManager();

    RegisterFragment refFragment = (RegisterFragment) fragmentManager.findFragmentByTag("REGISTER");
    if (refFragment != null) {
      refFragment.setCloverConnector(cloverConnector);
      refFragment.setDisplayConnector(displayConnector);
    }
    OrdersFragment ordersFragment = (OrdersFragment) fragmentManager.findFragmentByTag("ORDERS");
    if (ordersFragment != null) {
      ordersFragment.setCloverConnector(cloverConnector);
    }
    ManualRefundsFragment manualRefundsFragment = (ManualRefundsFragment) fragmentManager.findFragmentByTag("REFUNDS");
    if (manualRefundsFragment != null) {
      manualRefundsFragment.setCloverConnector(cloverConnector);
    }
    CardsFragment cardsFragment = (CardsFragment) fragmentManager.findFragmentByTag("CARDS");
    if (cardsFragment != null) {
      cardsFragment.setCloverConnector(cloverConnector);
    }
    MiscellaneousFragment miscFragment = (MiscellaneousFragment) fragmentManager.findFragmentByTag("MISC");
    if (miscFragment != null) {
      miscFragment.setCloverConnector(cloverConnector);
    }
    PendingPaymentsFragment ppFragment = (PendingPaymentsFragment) fragmentManager.findFragmentByTag("PENDING");
    if (ppFragment != null) {
      ppFragment.setCloverConnector(cloverConnector);
    }
  }

  public void showPending(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("PENDING");

    if (fragment == null) {
      fragment = PendingPaymentsFragment.newInstance(store, cloverConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "PENDING");
    } else {
      ((PendingPaymentsFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }

    fragmentTransaction.commitAllowingStateLoss();
  }

  public void showOrders(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("ORDERS");
    if (fragment == null) {
      fragment = OrdersFragment.newInstance(store, cloverConnector);
      ((OrdersFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "ORDERS");
    } else {
      ((OrdersFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }

    fragmentTransaction.commitAllowingStateLoss();
  }

  public void showRegister(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("REGISTER");
    if (fragment == null) {
      fragment = RegisterFragment.newInstance(store, cloverConnector, displayConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "REGISTER");
    } else {
      ((RegisterFragment) fragment).setCloverConnector(cloverConnector);

      fragmentTransaction.show(fragment);
    }

    fragmentTransaction.commitAllowingStateLoss();
  }

  public void showRefunds(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("REFUNDS");
    if (fragment == null) {
      fragment = ManualRefundsFragment.newInstance(store, cloverConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "REFUNDS");
    } else {
      ((ManualRefundsFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }

    fragmentTransaction.commitAllowingStateLoss();
  }

  public void showCards(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("CARDS");
    if (fragment == null) {
      fragment = CardsFragment.newInstance(store, cloverConnector);
      ((CardsFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "CARDS");
    } else {
      ((CardsFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.commitAllowingStateLoss();
  }

  public void showMisc(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("MISC");

    if (fragment == null) {
      fragment = MiscellaneousFragment.newInstance(store, cloverConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "MISC");
    } else {
      ((MiscellaneousFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }

    fragmentTransaction.commitAllowingStateLoss();
  }

  public void showPreAuths(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("PRE_AUTHS");

    if (fragment == null) {
      fragment = PreAuthFragment.newInstance(store, cloverConnector);
      ((PreAuthFragment) fragment).setStore(store);
      fragmentTransaction.add(R.id.contentContainer, fragment, "PRE_AUTHS");
    } else {
      ((PreAuthFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }

    fragmentTransaction.commitAllowingStateLoss();
  }

  private void hideFragments(FragmentManager fragmentManager, FragmentTransaction fragmentTransaction) {
    Fragment fragment = fragmentManager.findFragmentByTag("ORDERS");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("REGISTER");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("SIGNATURE");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("CARDS");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("MISC");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("REFUNDS");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("PRE_AUTHS");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("PENDING");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
  }

  public void captureCardClick(View view) {
    cloverConnector.vaultCard(store.getCardEntryMethods());
  }

  public void onClickCloseout(View view) {
    CloseoutRequest request = new CloseoutRequest();
    request.setAllowOpenTabs(false);
    request.setBatchId(null);
    cloverConnector.closeout(request);
  }

  public void onManualRefundClick(View view) {
    CharSequence val = ((TextView) findViewById(R.id.ManualRefundTextView)).getText();
    try {
      long refundAmount = Long.parseLong(val.toString());
      ManualRefundRequest request = new ManualRefundRequest();
      request.setExternalId(IdUtils.getNextId());
      request.setAmount(refundAmount);
      request.setCardEntryMethods(store.getCardEntryMethods());
      request.setDisablePrinting(store.getDisablePrinting());
      request.setDisableReceiptSelection(store.getDisableReceiptOptions());
      cloverConnector.manualRefund(request);
    } catch (NumberFormatException nfe) {
      showMessage("Invalid value. Must be an integer.", Toast.LENGTH_LONG);
    }
  }

  public void queryPaymentClick(View view) {
    String externalPaymentId = ((TextView) findViewById(R.id.QueryPaymentText)).getText().toString();
    cloverConnector.retrievePayment(new RetrievePaymentRequest().setExternalPaymentId(externalPaymentId));
  }

  public void preauthCardClick(View view) {
    PreAuthRequest request = new PreAuthRequest();
    request.setAmount(5000L);
    request.setExternalId(IdUtils.getNextId());
    request.setCardEntryMethods(store.getCardEntryMethods());
    request.setDisablePrinting(store.getDisablePrinting());
    request.setSignatureEntryLocation(store.getSignatureEntryLocation());
    request.setSignatureThreshold(store.getSignatureThreshold());
    request.setDisableReceiptSelection(store.getDisableReceiptOptions());
    request.setDisableDuplicateChecking(store.getDisableDuplicateChecking());
    cloverConnector.preAuth(request);
  }


  public void onReadCardDataClick(View view) {
    ReadCardDataRequest readCardDataRequest = new ReadCardDataRequest();
    readCardDataRequest.setCardEntryMethods(store.getCardEntryMethods());
    cloverConnector.readCardData(readCardDataRequest);
  }

  public void refreshPendingPayments(View view) {
    cloverConnector.retrievePendingPayments();
  }

}
