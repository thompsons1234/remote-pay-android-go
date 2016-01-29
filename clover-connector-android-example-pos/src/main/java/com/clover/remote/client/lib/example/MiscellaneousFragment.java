package com.clover.remote.client.lib.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.clover.remote.client.CloverConnector;
import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.lib.example.adapter.ItemsListViewAdapter;
import com.clover.remote.client.lib.example.adapter.OrdersListViewAdapter;
import com.clover.remote.client.lib.example.adapter.PaymentsListViewAdapter;
import com.clover.remote.client.lib.example.model.OrderObserver;
import com.clover.remote.client.lib.example.model.POSDiscount;
import com.clover.remote.client.lib.example.model.POSExchange;
import com.clover.remote.client.lib.example.model.POSLineItem;
import com.clover.remote.client.lib.example.model.POSOrder;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.model.POSRefund;
import com.clover.remote.client.lib.example.model.POSStore;
import com.clover.remote.client.lib.example.model.StoreObserver;
import com.clover.remote.client.messages.RefundPaymentRequest;
import com.clover.remote.client.messages.TipAdjustAuthRequest;
import com.clover.remote.client.messages.VoidPaymentRequest;
import com.clover.sdk.v3.order.VoidReason;

import java.lang.ref.WeakReference;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MiscellaneousFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MiscellaneousFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MiscellaneousFragment extends Fragment {
    private static final String ARG_STORE = "store";

    private POSStore store;

    private OnFragmentInteractionListener mListener;

    private WeakReference<ICloverConnector> cloverConnectorWeakReference;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param store Parameter 1.
     * @param cloverConnector Parameter 2.
     * @return A new instance of fragment OrdersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MiscellaneousFragment newInstance(POSStore store, CloverConnector cloverConnector) {
        MiscellaneousFragment fragment = new MiscellaneousFragment();
        fragment.setStore(store);
        Bundle args = new Bundle();
        fragment.setArguments(args);

        store.addCurrentOrderObserver(new OrderObserver() {
            @Override public void lineItemAdded(POSOrder posOrder, POSLineItem lineItem) {

            }

            @Override public void lineItemRemoved(POSOrder posOrder, POSLineItem lineItem) {

            }

            @Override public void lineItemChanged(POSOrder posOrder, POSLineItem lineItem) {

            }

            @Override public void paymentAdded(POSOrder posOrder, POSPayment payment) {

            }

            @Override public void refundAdded(POSOrder posOrder, POSRefund refund) {

            }

            @Override public void paymentChanged(POSOrder posOrder, POSExchange pay) {

            }

            @Override public void discountAdded(POSOrder posOrder, POSDiscount discount) {

            }

            @Override public void discountChanged(POSOrder posOrder, POSDiscount discount) {

            }
        });


        return fragment;
    }

    public MiscellaneousFragment() {
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
        View view = inflater.inflate(R.layout.fragment_miscellaneous, container, false);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
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

    public void setStore(POSStore store) {
        this.store = store;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public void setCloverConnector(ICloverConnector cloverConnector) {
        cloverConnectorWeakReference = new WeakReference<ICloverConnector>(cloverConnector);
    }

}
