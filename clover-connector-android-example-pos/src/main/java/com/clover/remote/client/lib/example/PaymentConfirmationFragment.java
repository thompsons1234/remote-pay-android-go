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

import com.clover.remote.Challenge;
import com.clover.remote.client.ICloverConnector;
import com.clover.sdk.v3.payments.Payment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class PaymentConfirmationFragment extends DialogFragment {

  private int challengeIndex = 0;
  private Payment payment;
  private Challenge[] challenges;
  private ICloverConnector cloverConnector;
  Context _context;
  private AlertDialog.Builder confirmationDialog;
  private TextView view;
  private LayoutInflater inflater;

  public static PaymentConfirmationFragment newInstance(Payment payment, Challenge[] challenges, ICloverConnector cloverConnector) {
    PaymentConfirmationFragment fragment = new PaymentConfirmationFragment();
    fragment.setPayment(payment);
    fragment.setChallenges(challenges);
    fragment.setCloverConnector(cloverConnector);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public PaymentConfirmationFragment() {
    // Required empty public constructor
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    confirmationDialog = new AlertDialog.Builder(_context);

    view = inflater.inflate()
    view = (TextView)confirmationDialog.findViewById(android.R.id.message);

    confirmationDialog.setMessage(challenges[challengeIndex].message);
    confirmationDialog.setNegativeButton("Reject", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        cloverConnector.rejectPayment(payment, challenges[challengeIndex]);
        dialog.dismiss();
      }
    });
    confirmationDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (challengeIndex == challenges.length - 1) {
          cloverConnector.acceptPayment(payment);
          dialog.dismiss();
        } else {
          challengeIndex++;
          confirmationDialog.setMessage(challenges[challengeIndex].message);
          confirmationDialog.
        }
      }
    });
    return confirmationDialog.create();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    _context = activity;
  }

  @Override
  public void onResume() {
    super.onResume();

  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  public void setPayment(Payment payment) {
    this.payment = payment;
  }

  public void setChallenges(Challenge[] challenges) {
    this.challenges = challenges;
  }

  public void setCloverConnector(ICloverConnector cloverConnector) {
    this.cloverConnector = cloverConnector;
  }

}
