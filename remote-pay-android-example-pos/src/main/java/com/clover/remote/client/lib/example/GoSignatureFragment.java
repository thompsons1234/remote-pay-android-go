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
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.clover.remote.client.clovergo.ICloverGoConnectorListener;


public class GoSignatureFragment extends Fragment {

  private ICloverGoConnectorListener.SignatureCapture signatureCapture;
  private String paymentID;

  private GestureOverlayView signatureView;

  public static GoSignatureFragment newInstance(String paymentID, ICloverGoConnectorListener.SignatureCapture signatureCapture) {
    GoSignatureFragment fragment = new GoSignatureFragment();
    fragment.signatureCapture = signatureCapture;
    fragment.paymentID = paymentID;
    return fragment;
  }

  public GoSignatureFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.fragment_go_signature, container, false);

    if (view != null) {
      signatureView = (GestureOverlayView) view.findViewById(R.id.SignatureView);
      signatureView.setKeepScreenOn(true);

      view.findViewById(R.id.AcceptButton).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (signatureView.getGesture() != null) {
            int gestureCount = signatureView.getGesture().getStrokesCount();
            int[][] xy = null;
            float[] points;

            for (int i = 0; i < gestureCount; i++) {
              points = signatureView.getGesture().getStrokes().get(i).points;
              xy = new int[points.length / 2][2];
              int count = 0;

              for (int j = 0; j < points.length; j += 2) {
                xy[count][0] = (int) points[j];
                xy[count][1] = (int) points[j + 1];
                count++;
              }
            }

            signatureCapture.captureSignature(paymentID, xy);
            getActivity().getFragmentManager().beginTransaction().remove(GoSignatureFragment.this).commit();

          } else {
            Toast.makeText(getActivity(), "Please Sign...", Toast.LENGTH_LONG).show();
          }
        }
      });
    }

    return view;
  }
}