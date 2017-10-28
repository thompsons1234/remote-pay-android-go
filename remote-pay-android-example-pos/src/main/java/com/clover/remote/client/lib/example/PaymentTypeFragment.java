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

package com.clover.remote.client.lib.example;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PaymentTypeFragment extends Fragment {

  public PaymentTypeFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    final View view = inflater.inflate(R.layout.fragment_go_payment_type, container, false);
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    showMe();
  }

  private void showMe() {

    View view = getView();

    if (view != null) {

      Bundle args = getArguments();
      final String transactionType = args.getString(AppConstants.TRANSACTION_TYPE_ARG);
      boolean showR450 = args.getBoolean(AppConstants.PAYMENT_TYPE_RP450);
      boolean showR350 = args.getBoolean(AppConstants.PAYMENT_TYPE_RP350);

      Button rp450Button = (Button) view.findViewById(R.id.rp450_button);

      if (showR450) {
        rp450Button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            continueTransaction(transactionType, AppConstants.PAYMENT_TYPE_RP450);
          }
        });
      } else {
        rp450Button.setVisibility(View.GONE);
      }

      Button rp350Button = (Button) view.findViewById(R.id.rp350_button);
      if (showR350) {
        rp350Button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            continueTransaction(transactionType, AppConstants.PAYMENT_TYPE_RP350);
          }
        });
      } else {
        rp350Button.setVisibility(View.GONE);
      }

      Button keyEnterButton = (Button) view.findViewById(R.id.key_enter_button);
      keyEnterButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          continueTransaction(transactionType, AppConstants.PAYMENT_TYPE_KEYED);
        }
      });

      Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
      cancelButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          hideMe();
          ((ExamplePOSActivity) getActivity()).paymentTypeCanceled();
        }
      });
    }
  }

  private void continueTransaction(String transactionType, String paymentType) {
    hideMe();
    ((ExamplePOSActivity) getActivity()).paymentTypeSelected(transactionType, paymentType);
  }

  private void hideMe() {
    FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
    fragmentTransaction.hide(this);
    fragmentTransaction.commit();
  }
}

