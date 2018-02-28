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

import com.clover.remote.client.clovergo.ICloverGoConnector;

public class PaymentTypeFragment extends Fragment {

  public PaymentTypeFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
      boolean showRP350 = args.getBoolean(AppConstants.PAYMENT_TYPE_RP350, false);
      boolean showRP450 = args.getBoolean(AppConstants.PAYMENT_TYPE_RP450, false);
      boolean showKeyEnter = args.getBoolean(AppConstants.PAYMENT_TYPE_KEYED, false);

      Button rp450Button = (Button) view.findViewById(R.id.rp450_button);

      if (showRP450) {
        rp450Button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            continueTransaction(ICloverGoConnector.GoPaymentType.RP450);
          }
        });

        rp450Button.setVisibility(View.VISIBLE);

      } else {
        rp450Button.setVisibility(View.GONE);
      }

      Button rp350Button = (Button) view.findViewById(R.id.rp350_button);
      if (showRP350) {
        rp350Button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            continueTransaction(ICloverGoConnector.GoPaymentType.RP350);
          }
        });

        rp350Button.setVisibility(View.VISIBLE);

      } else {
        rp350Button.setVisibility(View.GONE);
      }

      Button keyEnterButton = (Button) view.findViewById(R.id.key_enter_button);
      if (showKeyEnter) {
        keyEnterButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            continueTransaction(ICloverGoConnector.GoPaymentType.KEYED);
          }
        });

        keyEnterButton.setVisibility(View.VISIBLE);

      } else {
        keyEnterButton.setVisibility(View.GONE);
      }

      Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
      cancelButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          removeMe();
          ((ExamplePOSActivity) getActivity()).goPaymentTypeCanceled();
        }
      });
    }
  }

  private void continueTransaction(ICloverGoConnector.GoPaymentType paymentType) {
    removeMe();
    ((ExamplePOSActivity) getActivity()).goPaymentTypeSelected(paymentType);
  }

  private void removeMe() {
    FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
    fragmentTransaction.remove(this);
    fragmentTransaction.commit();
  }
}

