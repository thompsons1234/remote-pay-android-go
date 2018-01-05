package com.clover.remote.client.lib.example;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.lib.example.model.POSExchange;
import com.clover.remote.client.lib.example.model.POSStore;
import com.clover.remote.client.messages.RefundPaymentRequest;

import java.lang.ref.WeakReference;

public class PartialRefundFragment extends DialogFragment {
  private WeakReference<ICloverConnector> cloverConnectorWeakReference;

  private POSStore store;
  private POSExchange posExchange;

  public static PartialRefundFragment newInstance(POSStore store, POSExchange posExchange, WeakReference<ICloverConnector> cloverConnector) {
    PartialRefundFragment fragment = new PartialRefundFragment();
    fragment.store = store;
    fragment.posExchange = posExchange;
    fragment.cloverConnectorWeakReference = cloverConnector;
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
    View view = inflater.inflate(R.layout.dialog_partial_refund, container, false);

    final EditText refundAmountEdtTxt = (EditText) view.findViewById(R.id.partialRefundAmountEditTxt);

    view.findViewById(R.id.partialRefundBtn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ICloverConnector cloverConnector = cloverConnectorWeakReference.get();

        String amount = refundAmountEdtTxt.getText().toString();
        if (TextUtils.isEmpty(amount) || Long.valueOf(amount) <= 0) {
          Toast.makeText(getActivity(), "Please enter a valid refund amount", Toast.LENGTH_SHORT).show();
          return;
        }
        ((ExamplePOSActivity) getActivity()).showProgressDialog("Partial Refund", "Processing refund", false);
        getDialog().dismiss();

        RefundPaymentRequest refundRequest = new RefundPaymentRequest();
        refundRequest.setPaymentId(posExchange.getPaymentID());
        refundRequest.setOrderId(posExchange.orderID);
        refundRequest.setFullRefund(false);
        refundRequest.setAmount(Long.valueOf(amount));
        refundRequest.setDisablePrinting(store.getDisablePrinting() != null ? store.getDisablePrinting() : false);
        refundRequest.setDisableReceiptSelection(store.getDisableReceiptOptions() != null ? store.getDisableReceiptOptions() : false);
        cloverConnector.refundPayment(refundRequest);
      }
    });

    view.findViewById(R.id.partialRefundCancelBtn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getDialog().dismiss();
      }
    });

    return view;
  }
}