package com.clover.remote.client.lib.example;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.clovergo.messages.KeyedAuthRequest;
import com.clover.remote.client.clovergo.messages.KeyedPreAuthRequest;
import com.clover.remote.client.clovergo.messages.KeyedSaleRequest;
import com.clover.remote.client.lib.example.model.POSStore;
import com.clover.remote.client.lib.example.utils.IdUtils;
import com.firstdata.clovergo.domain.utils.CreditCardUtil;

/**
 * Created by Akhani, Avdhesh on 6/20/17.
 */

public class KeyedTransactionFragment extends DialogFragment {


    private POSStore store;
    private ICloverConnector cloverConnector;
    private String txType;

    public static KeyedTransactionFragment newInstance(POSStore store, ICloverConnector cloverConnector, String txType) {

        KeyedTransactionFragment fragment = new KeyedTransactionFragment();

        fragment.setStore(store);
        fragment.setCloverConnector(cloverConnector);
        fragment.setTransactionType(txType);

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.keyed_dialog_layout, container, false);


        final EditText mCardNumber = (EditText) view.findViewById(R.id.cardNumberEditText);
        final EditText mExpiration = (EditText) view.findViewById(R.id.expirationEditText);
        final EditText mCvv = (EditText) view.findViewById(R.id.cvvEditText);
        view.findViewById(R.id.startTransactionBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cardNumber = mCardNumber.getText().toString();
                if (!CreditCardUtil.validateCard(cardNumber)) {
                    Toast.makeText(getActivity(), "Please enter a valid card number", Toast.LENGTH_SHORT).show();
                    return;
                }
                String expiration = (mExpiration.getText().toString().replace("/", ""));
                if (!CreditCardUtil.validateCardExpiry(expiration)) {
                    Toast.makeText(getActivity(), "Please enter a valid card expiration", Toast.LENGTH_SHORT).show();
                    return;
                }
                String cvv = mCvv.getText().toString();
                int defaultCvvLength = CreditCardUtil.getCardType(cardNumber) == CreditCardUtil.AMEX ? 4 : 3;

                if (cvv.length() != defaultCvvLength) {
                    Toast.makeText(getActivity(), "Please enter a valid cvv number", Toast.LENGTH_SHORT).show();
                    return;
                }
                ((ExamplePOSActivity)getActivity()).showProgressDialog("Keyed Transaction","Processing Transaction",false);
                getDialog().dismiss();
                if (txType.equals("sale")){
                    KeyedSaleRequest request = new KeyedSaleRequest(store.getCurrentOrder().getTotal(), IdUtils.getNextId(),cardNumber,expiration,cvv);
                    request.setTaxAmount(store.getCurrentOrder().getTaxAmount());
                    request.setTipAmount(store.getTipAmount());
                    cloverConnector.sale(request);
                }else if (txType.equals("auth")){
                    KeyedAuthRequest request = new KeyedAuthRequest(store.getCurrentOrder().getTotal(), IdUtils.getNextId(),cardNumber,expiration,cvv);
                    request.setTaxAmount(store.getCurrentOrder().getTaxAmount());
                    cloverConnector.auth(request);
                }else if (txType.equals("preAuth")){
                    KeyedPreAuthRequest request = new KeyedPreAuthRequest(5000L, IdUtils.getNextId(),cardNumber,expiration,cvv);
                    cloverConnector.preAuth(request);
                }
            }
        });


        return view;
    }

    public void setStore(POSStore store) {
        this.store = store;
    }

    public void setCloverConnector(ICloverConnector cloverConnector) {
        this.cloverConnector = cloverConnector;
    }

    public void setTransactionType(String txType) {
        this.txType = txType;
    }
}
