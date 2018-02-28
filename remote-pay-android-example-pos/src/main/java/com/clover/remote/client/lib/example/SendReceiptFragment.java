package com.clover.remote.client.lib.example;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.clover.remote.client.clovergo.ICloverGoConnector;
import com.clover.remote.client.clovergo.ICloverGoConnectorListener;
import com.clover.remote.client.lib.example.utils.Validator;

/**
 * Created by Akhani, Avdhesh on 6/13/17.
 */

public class SendReceiptFragment extends Fragment {

  private ICloverGoConnector cGoConnector;
  private String orderID;

  private ICloverGoConnectorListener.SendReceipt sendReceipt;

  public static SendReceiptFragment newInstance(String orderID, ICloverGoConnector cGoConnector) {
    SendReceiptFragment fragment = new SendReceiptFragment();
    fragment.cGoConnector = cGoConnector;
    fragment.orderID = orderID;
    return fragment;
  }

  public static SendReceiptFragment newInstance(String orderID, ICloverGoConnectorListener.SendReceipt sendReceipt) {
    SendReceiptFragment fragment = new SendReceiptFragment();
    fragment.sendReceipt = sendReceipt;
    fragment.orderID = orderID;
    return fragment;
  }

  public SendReceiptFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.fragment_send_receipt, container, false);

    if (view != null) {
      final EditText mEmailEditText = (EditText) view.findViewById(R.id.sendReceiptEmailEdit);
      final EditText mPhoneEditText = (EditText) view.findViewById(R.id.sendReceiptPhoneEdit);
      Button mSendReceiptBtn = (Button) view.findViewById(R.id.sendReceiptBtn);
      Button mNoReceiptBtn = (Button) view.findViewById(R.id.noReceiptBtn);

      mSendReceiptBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

          String phoneNumber = mPhoneEditText.getText().toString().replaceAll("\\D", "");
          String email = mEmailEditText.getText().toString();

          if (Validator.validateEmailInput(email) || Validator.validatePhoneNumberInput(phoneNumber)) {
            if (sendReceipt != null) {
              sendReceipt.sendRequestedReceipt(email, phoneNumber, orderID);
            } else {
              cGoConnector.sendReceipt(email, phoneNumber, orderID);
            }

            ((ExamplePOSActivity) getActivity()).hideKeyboard();
            getActivity().getFragmentManager().beginTransaction().hide(SendReceiptFragment.this).commit();

          } else {
            Toast.makeText(getActivity(), "Please enter a valid phone number or email", Toast.LENGTH_SHORT).show();
          }

        }
      });

      mNoReceiptBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (sendReceipt != null)
            sendReceipt.noReceipt();

          ((ExamplePOSActivity) getActivity()).hideKeyboard();
          getActivity().getFragmentManager().beginTransaction().remove(SendReceiptFragment.this).commit();
        }
      });
    }

    return view;
  }
}