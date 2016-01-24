package com.clover.remote.client.lib.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.clover.common.util.CurrencyUtils;
import com.clover.remote.client.lib.example.R;
import com.clover.remote.client.lib.example.model.POSLineItem;
import com.clover.remote.client.lib.example.model.POSOrder;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Created by blakewilliams on 1/17/16.
 */
public class ItemsListViewAdapter extends ArrayAdapter<POSLineItem> {

  public ItemsListViewAdapter(Context context, int resource) {
    super(context, resource);
  }

  public ItemsListViewAdapter(Context context, int resource, List<POSLineItem> items) {
    super(context, resource, items);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View v = convertView;

    if (v == null) {
      LayoutInflater vi;
      vi = LayoutInflater.from(getContext());
      v = vi.inflate(R.layout.items_row, null);
    }

    POSLineItem posLI = getItem(position);

    if (posLI != null) {
      TextView quantityColumn = (TextView) v.findViewById(R.id.ItemsRowQuantityColumn);
      TextView descriptionColumn = (TextView) v.findViewById(R.id.ItemsRowDescriptionColumn);
      TextView priceColumn = (TextView) v.findViewById(R.id.ItemsRowPriceColumn);

      quantityColumn.setText(""+posLI.getQuantity());
      descriptionColumn.setText(posLI.getItem().getName());
      priceColumn.setText(CurrencyUtils.longToAmountString(Currency.getInstance(Locale.getDefault()), posLI.getItem().getPrice()));
    }

    return v;
  }
}
