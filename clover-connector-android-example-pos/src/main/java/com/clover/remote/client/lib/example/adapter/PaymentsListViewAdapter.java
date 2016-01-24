package com.clover.remote.client.lib.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.clover.remote.client.lib.example.R;
import com.clover.remote.client.lib.example.model.POSExchange;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.model.POSRefund;

import java.util.List;

/**
 * Created by blakewilliams on 1/17/16.
 */
public class PaymentsListViewAdapter extends ArrayAdapter<POSExchange>{

    public PaymentsListViewAdapter(Context context, int resource) {
        super(context, resource);
    }

    public PaymentsListViewAdapter(Context context, int resource, List<POSExchange> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.payments_row, null);
        }

        POSExchange posPayment = getItem(position);

        if (posPayment != null) {
            TextView statusColumn = (TextView) v.findViewById(R.id.PaymentsRowStatusColumn);
            TextView itemAmountColumn = (TextView) v.findViewById(R.id.PaymentsRowAmountColumn);
            TextView tipColumn = (TextView) v.findViewById(R.id.PaymentsRowTipColumn);
            TextView totalColumn = (TextView) v.findViewById(R.id.PaymentsRowTotalColumn);


            if(posPayment instanceof POSPayment) {

                statusColumn.setText(((POSPayment)posPayment).getPaymentStatus() == null ? "" : "" + ((POSPayment)posPayment).getPaymentStatus());
                itemAmountColumn.setText(""+((POSPayment)posPayment).getOrder().getTotal());
                tipColumn.setText(""+((POSPayment)posPayment).getTipAmount());
                totalColumn.setText("" + ((POSPayment)posPayment).getAmount());
            } else if (posPayment instanceof POSRefund){
                statusColumn.setText("REFUND");
                itemAmountColumn.setText(""+((POSRefund)posPayment).getAmount());
                tipColumn.setText("");
                totalColumn.setText("");
            }
        }

        return v;
    }
}
