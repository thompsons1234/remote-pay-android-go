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

import com.clover.sdk.v3.base.PendingPaymentEntry;
import com.clover.sdk.v3.connector.IPaymentConnector;
import com.clover.example.adapter.RefundsListViewAdapter;
import com.clover.example.model.POSCard;
import com.clover.example.model.POSNakedRefund;
import com.clover.example.model.POSOrder;
import com.clover.example.model.POSPayment;
import com.clover.example.model.POSStore;
import com.clover.example.model.StoreObserver;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.List;

public class ManualRefundsFragment extends Fragment {
    private static final String ARG_STORE = "store";

    private POSStore store;

    private OnFragmentInteractionListener mListener;

    private WeakReference<IPaymentConnector> cloverConnectorWeakReference;


    public static ManualRefundsFragment newInstance(POSStore store, IPaymentConnector cloverConnector) {
        ManualRefundsFragment fragment = new ManualRefundsFragment();
        fragment.setStore(store);
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public ManualRefundsFragment() {
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
        final View view = inflater.inflate(R.layout.fragment_refunds, container, false);



        final ListView refundsListView = (ListView)view.findViewById(R.id.RefundsListView);
        final RefundsListViewAdapter itemsListViewAdapter = new RefundsListViewAdapter(view.getContext(), R.id.RefundsListView, store.getRefunds());
        refundsListView.setAdapter(itemsListViewAdapter);

        store.addStoreObserver(new StoreObserver() {
            @Override public void newOrderCreated(POSOrder order, boolean userInitiated) {

            }

            @Override public void cardAdded(POSCard card) {

            }

            @Override public void refundAdded(POSNakedRefund refund) {
                final RefundsListViewAdapter itemsListViewAdapter = new RefundsListViewAdapter(view.getContext(), R.id.RefundsListView, store.getRefunds());
                refundsListView.setAdapter(itemsListViewAdapter);
            }


            @Override public void preAuthAdded(POSPayment payment) {

            }

            @Override public void preAuthRemoved(POSPayment payment) {

            }

            @Override public void pendingPaymentsRetrieved(List<PendingPaymentEntry> pendingPayments) {

            }
        });


        return view;
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
