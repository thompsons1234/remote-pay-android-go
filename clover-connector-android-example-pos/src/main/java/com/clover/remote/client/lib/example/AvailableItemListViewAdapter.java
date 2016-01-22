package com.clover.remote.client.lib.example;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.clover.common.util.CurrencyUtils;
import com.clover.remote.client.lib.example.R;
import com.clover.remote.client.lib.example.model.POSLineItem;

import java.util.Locale;

/**
 * Created by blakewilliams on 12/21/15.
 */
public class AvailableItemListViewAdapter extends ArrayAdapter<POSLineItem> {

        Context context;
        int layoutResourceId;
        POSLineItem[] data = null;

        public AvailableItemListViewAdapter(Context context, int layoutResourceId, POSLineItem[] data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            POSLineItemHolder holder = null;

            if(row == null)
            {
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new POSLineItemHolder();
                holder.itemQuantity = (TextView) row.findViewById(R.id.OrderItemQuantityLabel);
                holder.itemName = (TextView)row.findViewById(R.id.OrderItemNameLabel);
                holder.itemPrice = (TextView) row.findViewById(R.id.OrderItemPriceLabel);
                holder.itemDiscount = (TextView) row.findViewById(R.id.OrderItemDiscountLabel);
                holder.itemDiscountPrice = (TextView) row.findViewById(R.id.OrderItemDiscountPriceLabel);

                row.setTag(holder);
            }
            else
            {
                holder = (POSLineItemHolder) row.getTag();
            }

            POSLineItem lineItem = data[position];
            holder.itemName.setText(lineItem.getItem().getName());
            holder.itemQuantity.setText(lineItem.getQuantity()+"");
            holder.itemPrice.setText(CurrencyUtils.longToAmountString(java.util.Currency.getInstance(Locale.getDefault()), lineItem.getPrice()));
            if(lineItem.getDiscount() != null) {
                holder.itemDiscount.setText(lineItem.getDiscount().getName());
                holder.itemDiscountPrice.setText(CurrencyUtils.longToAmountString(java.util.Currency.getInstance(Locale.getDefault()), lineItem.getDiscount().getValue(lineItem.getItem().getPrice())));
            } else {
                holder.itemDiscount.setText("");
                holder.itemDiscountPrice.setText("");
            }

            return row;
        }

        static class POSLineItemHolder
        {
            TextView itemQuantity;
            TextView itemName;
            TextView itemPrice;
            TextView itemDiscount;
            TextView itemDiscountPrice;
        }
    }





