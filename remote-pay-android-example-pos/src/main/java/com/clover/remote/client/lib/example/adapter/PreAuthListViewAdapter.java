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

package com.clover.remote.client.lib.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.clover.remote.client.lib.example.R;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.utils.CurrencyUtils;

import java.util.List;
import java.util.Locale;

public class PreAuthListViewAdapter extends ArrayAdapter<POSPayment> {

  public PreAuthListViewAdapter(Context context, int resource) {
    super(context, resource);
  }

  public PreAuthListViewAdapter(Context context, int resource, List<POSPayment> items) {
    super(context, resource, items);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = convertView;

    if (view == null) {
      view = LayoutInflater.from(getContext()).inflate(R.layout.preauth_row, null);
    }

    POSPayment posPayment = getItem(position);

    if (posPayment != null) {
      TextView nameColumn = (TextView) view.findViewById(R.id.PreAuthNameColumn);
      TextView amountColumn = (TextView) view.findViewById(R.id.PreAuthAmountColumn);
      TextView orderTxtVw = (TextView) view.findViewById(R.id.preAuthOrderIdTxtVw);
      TextView paymentTxtVw = (TextView) view.findViewById(R.id.preAuthPayIdTxtVw);
      TextView externalPayTxtVw = (TextView) view.findViewById(R.id.preAuthExternalPayIdTxtVw);

      nameColumn.setText("Pre-Authorized");
      amountColumn.setText(CurrencyUtils.format(posPayment.getAmount(), Locale.getDefault()));
      orderTxtVw.setText("Order ID: " + posPayment.getOrderId());
      paymentTxtVw.setText("Payment ID: " + posPayment.getPaymentID());
      externalPayTxtVw.setText("External Payment ID: " + posPayment.getExternalPaymentId());
    }

    return view;
  }
}