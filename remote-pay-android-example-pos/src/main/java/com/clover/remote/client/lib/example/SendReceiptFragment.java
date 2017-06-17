package com.clover.remote.client.lib.example;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.clovergo.ICloverGoConnector;
import com.clover.remote.client.lib.example.utils.Validator;

/**
 * Created by Akhani, Avdhesh on 6/13/17.
 */

public class SendReceiptFragment extends Fragment {


    private ICloverConnector cloverConnector;
    private String orderID;

    public static SendReceiptFragment newInstance(String orderID, ICloverConnector cloverConnector) {
        SendReceiptFragment fragment = new SendReceiptFragment();
        fragment.setCloverConnector(cloverConnector);
        fragment.setOrderID(orderID);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SendReceiptFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
                        ((ICloverGoConnector)cloverConnector).sendReceipt(email,phoneNumber,orderID);

                        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                        fragmentTransaction.hide(SendReceiptFragment.this);
                        fragmentTransaction.commit();

                    } else {
                        Toast.makeText(getActivity(), "Please enter a valid phone number or email", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            mNoReceiptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                    fragmentTransaction.hide(SendReceiptFragment.this);
                    fragmentTransaction.commit();
                }
            });

        }

        return view;
    }


    public void setCloverConnector(ICloverConnector cloverConnector) {
        this.cloverConnector = cloverConnector;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }


}
