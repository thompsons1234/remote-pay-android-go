package com.clover.remote.client.lib.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.clover.remote.client.lib.example.R;
import com.clover.remote.client.lib.example.model.POSOrder;

import java.util.List;

/**
 * Created by blakewilliams on 1/17/16.
 */
public class OrdersListViewAdapter extends ArrayAdapter<POSOrder> {

    public OrdersListViewAdapter(Context context, int resource) {
        super(context, resource);
    }

    public OrdersListViewAdapter(Context context, int resource, List<POSOrder> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.orders_row, null);
        }

        POSOrder posOrder = getItem(position);

        if (posOrder != null) {
            TextView idColumn = (TextView) v.findViewById(R.id.OrdersRowIdColumn);
            TextView dateColumn = (TextView) v.findViewById(R.id.OrdersRowDateColumn);
            TextView statusColumn = (TextView) v.findViewById(R.id.OrdersRowStatusColumn);
            TextView subtotalColumn = (TextView) v.findViewById(R.id.OrdersRowSubtotalColumn);
            TextView taxColumn = (TextView) v.findViewById(R.id.OrdersRowTaxColumn);
            TextView totalColumn = (TextView) v.findViewById(R.id.OrdersRowTotalColumn);

            idColumn.setText(posOrder.id);
            dateColumn.setText(posOrder.date.toString());
            statusColumn.setText(posOrder.status.toString());
            subtotalColumn.setText("" + posOrder.getPreTaxSubTotal());
            taxColumn.setText("" + posOrder.getTaxAmount());
            totalColumn.setText("" + posOrder.getTotal());
        }

        return v;
    }
}
