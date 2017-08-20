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
import com.clover.example.adapter.CardsListViewAdapter;
import com.clover.example.adapter.PendingListViewAdapter;
import com.clover.example.model.POSCard;
import com.clover.example.model.POSNakedRefund;
import com.clover.example.model.POSOrder;
import com.clover.example.model.POSPayment;
import com.clover.example.model.POSStore;
import com.clover.example.model.StoreObserver;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class PendingPaymentsFragment extends Fragment {

  private WeakReference<IPaymentConnector> cloverConnectorWeakReference;

  ListView pendingPaymentsListView;
  List<PendingPaymentEntry> pendingPayments;
  private POSStore store;

  public static PendingPaymentsFragment newInstance(POSStore store, IPaymentConnector cloverConnector) {
    PendingPaymentsFragment fragment = new PendingPaymentsFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);

    fragment.setCloverConnector(cloverConnector);

    fragment.store = store;

    return fragment;
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.fragment_pending, container, false);

    pendingPaymentsListView = (ListView) view.findViewById(R.id.PendingPaymentsListView);
    final CardsListViewAdapter cardsListViewAdapter = new CardsListViewAdapter(view.getContext(), R.id.PreAuthListView, pendingPayments == null ? Collections.EMPTY_LIST : pendingPayments);
    pendingPaymentsListView.setAdapter(cardsListViewAdapter);

    store.addStoreObserver(new StoreObserver() {
      @Override public void newOrderCreated(POSOrder order, boolean userInitiated) {

      }

      @Override public void cardAdded(POSCard card) {

      }

      @Override public void refundAdded(POSNakedRefund refund) {

      }

      @Override public void preAuthAdded(POSPayment payment) {

      }

      @Override public void preAuthRemoved(POSPayment payment) {

      }

      @Override public void pendingPaymentsRetrieved(final List<PendingPaymentEntry> pendingPayments) {
        getActivity().runOnUiThread(new Runnable(){
          @Override public void run() {
            if(pendingPayments == null) {
              Toast.makeText(view.getContext(), "Get pending payments failed.", Toast.LENGTH_SHORT).show();
            } else {
              Toast.makeText(view.getContext(), "Got back " + pendingPayments.size() + " pending payment(s)", Toast.LENGTH_SHORT).show();
            }
            pendingPaymentsListView.setAdapter(new PendingListViewAdapter(view.getContext(), R.id.PendingPaymentsListView, pendingPayments == null ? Collections.EMPTY_LIST : pendingPayments));
          }
        });
      }
    });


    return view;
  }

  public void setPendingPayments(List<PendingPaymentEntry> pendingPayments) {
    this.pendingPayments = pendingPayments;
  }

  public void setCloverConnector(IPaymentConnector cloverConnector) {
    this.cloverConnectorWeakReference = new WeakReference<IPaymentConnector>(cloverConnector);
  }
}
