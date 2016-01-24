package com.clover.remote.client.lib.example.model;

import java.io.Serializable;
import java.util.UUID;

public class POSLineItem
    {
        private transient POSOrder order;
        private POSLineItemDiscount discount;
        public String id;
        private POSItem item;

        public POSLineItem(POSOrder order, POSItem item)
        {
            this.item = item;
            this.order = order;
            this.quantity = 1;
            id = UUID.randomUUID().toString();
        }
        int quantity = 1;


        public POSLineItem(POSOrder order, POSItem item, int quantity)
        {
            this.item = item;
            this.order = order;
            this.quantity = quantity;
            id = UUID.randomUUID().toString();
        }


        public long getPrice() {
            if(discount != null)
            {
                return item.getPrice() - discount.getValue(item);
            }
            else
            {
                return item.getPrice();
            }
        }

        public void setDiscount(POSLineItemDiscount discount) {
            this.discount = discount;
            order.notifyObserverItemChanged(this);
        }

        public POSDiscount getDiscount() {
            return discount;
        }

        public int getQuantity()
        {
            return quantity;
        }

        public void setQuantity(int newQuantity) {
            quantity = newQuantity;
            order.notifyObserverItemChanged(this);
        }

        public void incrementQuantity(int quantity) {
            this.quantity += quantity;
            this.quantity = Math.max(0, this.quantity);
            order.notifyObserverItemChanged(this);
        }

        public String getId() {
            return id;
        }

        public POSItem getItem() {
            return item;
        }
    }
