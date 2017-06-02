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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.gesture.GestureOverlayView;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.clover.common2.Signature2;
import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.clovergo.ICloverGoConnector;
import com.clover.remote.client.messages.VerifySignatureRequest;

import java.util.ArrayList;
import java.util.List;


public class GoSignatureFragment extends Fragment {


  private Signature2 signature;
  Button acceptButton;
  GestureOverlayView signatureView;

  private OnFragmentInteractionListener mListener;
  private ICloverConnector cloverConnector;
  private String paymentID;

  public static GoSignatureFragment newInstance(String paymentID, ICloverConnector cloverConnector) {
    GoSignatureFragment fragment = new GoSignatureFragment();
    fragment.setCloverConnector(cloverConnector);
    fragment.setPaymentID(paymentID);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public GoSignatureFragment() {
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
    final View view = inflater.inflate(R.layout.fragment_go_signature, container, false);

    if (view != null) {
      signatureView = (GestureOverlayView) view.findViewById(R.id.SignatureView);
      signatureView.setKeepScreenOn(true);

      acceptButton = (Button) view.findViewById(R.id.AcceptButton);

      acceptButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {


          if (signatureView.getGesture() != null) {
            int gestureCount = signatureView.getGesture().getStrokesCount();
            int[][] xy = null;
            float[] points;
            for (int i = 0; i < gestureCount; i++) {
              points = signatureView.getGesture().getStrokes().get(i).points;
              int count = 0;
              xy = new int[points.length / 2][2];
              for (int j = 0; j < points.length; j += 2) {
                xy[count][0] = (int) points[j];
                xy[count][1] = (int) points[j + 1];
                count++;
              }
            }

            ((ICloverGoConnector)cloverConnector).captureSignature(paymentID,xy);
          }

          FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
          fragmentTransaction.hide(GoSignatureFragment.this);
          fragmentTransaction.commit();


        }
      });

    }

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      //TODO: check Go Example
      mListener = (GoSignatureFragment.OnFragmentInteractionListener) activity;
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

  public void setCloverConnector(ICloverConnector cloverConnector) {
    this.cloverConnector = cloverConnector;
  }

  public void setPaymentID(String paymentID) {
    this.paymentID = paymentID;
  }

  public interface OnFragmentInteractionListener {
    public void onFragmentInteraction(Uri uri);
  }

}
