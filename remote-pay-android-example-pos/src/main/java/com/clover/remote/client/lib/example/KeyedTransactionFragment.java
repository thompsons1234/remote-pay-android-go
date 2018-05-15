package com.clover.remote.client.lib.example;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.clover.remote.client.clovergo.CloverGoConstants.TransactionType;
import com.clover.remote.client.clovergo.messages.KeyedAuthRequest;
import com.clover.remote.client.clovergo.messages.KeyedPreAuthRequest;
import com.clover.remote.client.clovergo.messages.KeyedSaleRequest;
import com.clover.remote.client.lib.example.model.POSStore;
import com.clover.remote.client.lib.example.utils.IdUtils;
import com.clover.remote.client.messages.TransactionRequest;
import com.firstdata.clovergo.domain.utils.CreditCardUtil;

/**
 * Created by Akhani, Avdhesh on 6/20/17.
 */

public class KeyedTransactionFragment extends DialogFragment {
  private final String CARD_NUM = "4111111111111111";
  private final String CARD_EXP = "1220";
  private final String CARD_CVV = "333";

  private POSStore store;
  private TransactionType txType;

  public static KeyedTransactionFragment newInstance(POSStore store, TransactionType txType) {
    KeyedTransactionFragment fragment = new KeyedTransactionFragment();
    fragment.store = store;
    fragment.txType = txType;
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.keyed_dialog_layout, container, false);

    final EditText mCardNumber = (EditText) view.findViewById(R.id.cardNumberEditText);
    final EditText mExpiration = (EditText) view.findViewById(R.id.expirationEditText);
    final EditText mCvv = (EditText) view.findViewById(R.id.cvvEditText);
    view.findViewById(R.id.quickFill).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mCardNumber.setText(CARD_NUM);
        mExpiration.setText(CARD_EXP);
        mCvv.setText(CARD_CVV);
      }
    });
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
        ((ExamplePOSActivity) getActivity()).showProgressDialog("Keyed Transaction", "Processing Transaction", false);
        getDialog().dismiss();

        if (txType == TransactionType.SALE) {

          KeyedSaleRequest request = new KeyedSaleRequest(store.getCurrentOrder().getTotal(), IdUtils.getNextId(), cardNumber, expiration, cvv);
          request.setTaxAmount(store.getCurrentOrder().getTaxAmount());
          request.setTipAmount(store.getTipAmount());
          request.setNote(store.getPaymentNote());
          doneKeyEntry(txType, request);

        } else if (txType == TransactionType.AUTH) {

          KeyedAuthRequest request = new KeyedAuthRequest(store.getCurrentOrder().getTotal(), IdUtils.getNextId(), cardNumber, expiration, cvv);
          request.setTaxAmount(store.getCurrentOrder().getTaxAmount());
          request.setNote(store.getPaymentNote());
          doneKeyEntry(txType, request);

        } else if (txType == TransactionType.PRE_AUTH) {

          KeyedPreAuthRequest request = new KeyedPreAuthRequest(((ExamplePOSActivity) getActivity()).getPreAuthAmount(), IdUtils.getNextId(), cardNumber, expiration, cvv);
          request.setNote(store.getPaymentNote());
          doneKeyEntry(txType, request);
        }
      }
    });

    return view;
  }

  private void doneKeyEntry(TransactionType transactionType, TransactionRequest saleRequest) {
    ((ExamplePOSActivity) getActivity()).keyEntryDone(saleRequest, transactionType);
  }
}