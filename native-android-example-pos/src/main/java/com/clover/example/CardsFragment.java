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

import com.clover.example.adapter.CardsListViewAdapter;
import com.clover.example.model.POSCard;
import com.clover.example.model.POSNakedRefund;
import com.clover.example.model.POSOrder;
import com.clover.example.model.POSPayment;
import com.clover.example.model.POSStore;
import com.clover.example.model.StoreObserver;
import com.clover.example.utils.IdUtils;
import com.clover.sdk.v3.base.PendingPaymentEntry;
import com.clover.sdk.v3.connector.IPaymentConnector;
import com.clover.sdk.v3.payments.TipMode;
import com.clover.sdk.v3.payments.VaultedCard;
import com.clover.sdk.v3.remotepay.AuthRequest;
import com.clover.sdk.v3.remotepay.SaleRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

public class CardsFragment extends Fragment {
    private static final String ARG_STORE = "store";

    private POSStore store;

    private OnFragmentInteractionListener mListener;

    private WeakReference<IPaymentConnector> cloverConnectorWeakReference;
    private ListView cardsListView;

    public static CardsFragment newInstance(POSStore store, IPaymentConnector cloverConnector) {
        CardsFragment fragment = new CardsFragment();
        fragment.setStore(store);
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public CardsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_cards, container, false);


        store.addStoreObserver(new StoreObserver() {
            @Override public void newOrderCreated(POSOrder order, boolean userInitiated) {

            }

            @Override public void cardAdded(POSCard card) {
                final CardsListViewAdapter cardsListViewAdapter = new CardsListViewAdapter(view.getContext(), R.id.CardsListView, store.getCards());
                new AsyncTask(){
                    @Override protected Object doInBackground(Object[] params) {
                        return null;
                    }

                    @Override protected void onPostExecute(Object o) {
                        cardsListView.setAdapter(cardsListViewAdapter);
                    }
                }.execute();
            }

            @Override public void refundAdded(POSNakedRefund refund) {

            }

            @Override public void preAuthAdded(POSPayment payment) {

            }

            @Override public void preAuthRemoved(POSPayment payment) {

            }

            @Override public void pendingPaymentsRetrieved(List<PendingPaymentEntry> pendingPayments) {

            }
        });

        cardsListView = (ListView)view.findViewById(R.id.CardsListView);
        final CardsListViewAdapter cardsListViewAdapter = new CardsListViewAdapter(view.getContext(), R.id.CardsListView, store.getCards());
        cardsListView.setAdapter(cardsListViewAdapter);


        cardsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final POSCard posCard = (POSCard) cardsListView.getItemAtPosition(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                String[] paymentOptions = null;

                String[] payOptions = new String[]{"Sale for current order", "Auth for current order"/*, "Pre-Auth"*/};

                builder.setTitle("Pay With Card").
                    setItems(payOptions, new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int index) {
                            final IPaymentConnector cloverConnector = cloverConnectorWeakReference.get();
                            if(cloverConnector != null) {

                                VaultedCard vaultedCard = new VaultedCard();
                                vaultedCard.setCardholderName(posCard.getName());
                                vaultedCard.setFirst6(posCard.getFirst6());
                                vaultedCard.setLast4(posCard.getLast4());
                                vaultedCard.setExpirationDate(posCard.getMonth() + posCard.getYear());
                                vaultedCard.setToken(posCard.getToken());

                                switch(index) {
                                    case 0: {
                                        SaleRequest request = new SaleRequest();
                                        request.setAmount(store.getCurrentOrder().getTotal());
                                        request.setExternalId(IdUtils.getNextId());
                                        request.setCardEntryMethods(store.getCardEntryMethods());
                                        request.setAllowOfflinePayment(store.getAllowOfflinePayment());
                                        request.setForceOfflinePayment(store.getForceOfflinePayment());
                                        request.setApproveOfflinePaymentWithoutPrompt(store.getApproveOfflinePaymentWithoutPrompt());
                                        request.setTippableAmount(store.getCurrentOrder().getTippableAmount());
                                        request.setTaxAmount(store.getCurrentOrder().getTaxAmount());
                                        request.setDisablePrinting(store.getDisablePrinting());
                                        TipMode tipMode = store.getTipMode() != null ? TipMode.valueOf(store.getTipMode().toString()) : null;
                                        request.setTipMode(tipMode != null ? tipMode : null);
                                        request.setSignatureEntryLocation(store.getSignatureEntryLocation());
                                        request.setSignatureThreshold(store.getSignatureThreshold());
                                        request.setDisableReceiptSelection(store.getDisableReceiptOptions());
                                        request.setDisableDuplicateChecking(store.getDisableDuplicateChecking());
                                        request.setTipAmount(store.getTipAmount());
                                        request.setAutoAcceptPaymentConfirmations(store.getAutomaticPaymentConfirmation());
                                        request.setAutoAcceptSignature(store.getAutomaticSignatureConfirmation());
                                        request.setVaultedCard(vaultedCard);
                                        cloverConnector.sale(request);
                                        dialog.dismiss();
                                        break;
                                    }
                                    case 1: {
                                        AuthRequest request = new AuthRequest();
                                        request.setAmount(store.getCurrentOrder().getTotal());
                                        request.setExternalId(IdUtils.getNextId());
                                        request.setCardEntryMethods(store.getCardEntryMethods());
                                        request.setAllowOfflinePayment(store.getAllowOfflinePayment());
                                        request.setForceOfflinePayment(store.getForceOfflinePayment());
                                        request.setApproveOfflinePaymentWithoutPrompt(store.getApproveOfflinePaymentWithoutPrompt());
                                        request.setTippableAmount(store.getCurrentOrder().getTippableAmount());
                                        request.setTaxAmount(store.getCurrentOrder().getTaxAmount());
                                        request.setDisablePrinting(store.getDisablePrinting());
                                        request.setSignatureEntryLocation(store.getSignatureEntryLocation());
                                        request.setSignatureThreshold(store.getSignatureThreshold());
                                        request.setDisableReceiptSelection(store.getDisableReceiptOptions());
                                        request.setDisableDuplicateChecking(store.getDisableDuplicateChecking());
                                        request.setAutoAcceptPaymentConfirmations(store.getAutomaticPaymentConfirmation());
                                        request.setAutoAcceptSignature(store.getAutomaticSignatureConfirmation());
                                        request.setVaultedCard(vaultedCard);
                                        cloverConnector.auth(request);
                                        dialog.dismiss();
                                        break;
                                    }
                                }
                            } else {
                                Toast.makeText(getActivity().getBaseContext(), "Clover Connector is null", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                final Dialog dlg = builder.create();
                dlg.show();
            }
        });

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                                         + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setStore(POSStore store) {
        this.store = store;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    public void setCloverConnector(IPaymentConnector cloverConnector) {
        cloverConnectorWeakReference = new WeakReference<IPaymentConnector>(cloverConnector);
    }

}