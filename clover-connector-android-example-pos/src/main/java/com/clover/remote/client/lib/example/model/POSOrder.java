package com.clover.remote.client.lib.example.model;

import java.util.*;

public class POSOrder
    {

        public enum OrderStatus
        {
            OPEN, CLOSED, LOCKED, AUTHORIZED
        }

        private List<POSLineItem> items;
        private List<POSExchange> payments;
        private POSDiscount discount;
        public String id;
        public Date date;
        public OrderStatus status;

        private transient List<OrderObserver> observers = new ArrayList<OrderObserver>();

        public POSOrder()
        {
            status = OrderStatus.OPEN;
            items = new ObservableList<POSLineItem>();
            payments = new ObservableList<POSExchange>();
            discount = new POSDiscount("None", 0);
            date = new Date();
        }

        public void addOrderObserver(OrderObserver observer) {
            this.observers.add(observer);
        }

        public void removeObserver(OrderObserver observer) {
            this.observers.remove(observer);
        }

        public long getPreDiscountSubTotal() {
            long sub = 0;
            for (POSLineItem li : items)
            {
                sub += li.getPrice() * li.getQuantity();
            }
            return sub;
        }
        public long getPreTaxSubTotal() {
            long sub = 0;
            for (POSLineItem li : items)
            {
                sub += li.getPrice() * li.getQuantity() ;
            }
            if(discount != null)
            {
                sub = discount.appliedTo(sub);
            }
            return sub;
        }
        public long getTippableAmount() {
            long tippableAmount = 0;
            for(POSLineItem li : items)
            {
                if (li.getItem().isTippable())
                {
                    tippableAmount += li.getPrice() * li.getQuantity();
                }
            }
            if (discount != null)
            {
                tippableAmount = discount.appliedTo(tippableAmount);
            }
            return tippableAmount + getTaxAmount(); // shuold match Total if there aren't any "non-tippable" items

        }
        public long getTaxableSubtotal() {
            long sub = 0;
            for (POSLineItem li : items)
            {
                if(li.getItem().isTaxable())
                {
                    sub += li.getPrice() * li.getQuantity();
                }
            }
            if (discount != null)
            {
                sub = discount.appliedTo(sub);
            }
            return sub;
        }
        public long getTaxAmount() {
            return (int)(getTaxableSubtotal() * 0.07);
        }
        public long getTotal()
        {
            return getPreTaxSubTotal() + getTaxAmount();
        }
        public long getTips()
        {
            long tips = 0;
            for(POSExchange posPayment : payments)
            {
                if(posPayment instanceof POSPayment) {
                    tips += ((POSPayment)posPayment).getTipAmount();
                }
            }
            return tips;
        }


        /// <summary>
        /// manages adding a POSItem to an order. If the POSItem already exists, the quantity is just incremented
        /// </summary>
        /// <param name="i"></param>
        /// <param name="quantity"></param>
        /// <returns>The POSLineItem for the POSItem. Will either return a new one, or an exising with its quantity incremented</returns>
        public POSLineItem addItem(POSItem i, int quantity)
        {
            boolean exists = false;
            POSLineItem targetItem = null;
            for (POSLineItem lineI : items)
            {
                if(lineI.getItem().getId() == i.getId())
                {
                    exists = true;
                    lineI.incrementQuantity(quantity);
                    targetItem = lineI;
                    notifyObserverItemChanged(targetItem);
                    break;
                }
            }
            if(!exists)
            {
                POSLineItem li = new POSLineItem(this, i, quantity);
                targetItem = li;
                items.add(targetItem);
                notifyObserverItemAdded(targetItem);
            }
            return targetItem;
        }



        void addPayment(POSPayment payment)
        {
            payments.add(payment);
            payment.setOrder(this);
            notifyObserverPaymentAdded(payment);
        }


        void addRefund(POSRefund refund)
        {
            for(POSExchange pay : payments)
            {
                if(pay instanceof POSPayment)
                {
                    if (pay.paymentID == refund.getPaymentID())
                    {
                        ((POSPayment)pay).setPaymentStatus(POSPayment.Status.REFUNDED);
                        notifyObserverPaymentChanged(pay);
                    }

                }
            }
            payments.add(refund);
            notifyObserverRefundAdded(refund);
        }



        protected void removeItem(POSLineItem selectedLineItem)
        {
            items.remove(selectedLineItem);
            notifyObserverItemRemoved(selectedLineItem);
        }


        public List<POSLineItem> getItems() {
            return items;
        }

        public List<POSExchange> getPayments() {
            return Collections.unmodifiableList(payments);
        }

        public void setDiscount(POSDiscount discount) {
            this.discount = discount;
            notifyObserverDiscountChanged(discount);
        }


        public POSDiscount getDiscount() {
            return discount;
        }

        void notifyObserverItemAdded(POSLineItem targetItem) {
            for(OrderObserver observer : observers) {
                observer.lineItemAdded(this, targetItem);
            }
        }

        void notifyObserverItemChanged(POSLineItem targetItem) {
            for(OrderObserver observer : observers) {
                observer.lineItemChanged(this, targetItem);
            }
        }
        void notifyObserverPaymentAdded(POSPayment payment) {
            for(OrderObserver observer : observers) {
                observer.paymentAdded(this, payment);
            }
        }
        void notifyObserverRefundAdded(POSRefund refund) {
            for(OrderObserver observer : observers) {
                observer.refundAdded(this, refund);
            }
        }
        void notifyObserverPaymentChanged(POSExchange pay) {
            for(OrderObserver observer : observers) {
                observer.paymentChanged(this, pay);
            }
        }
        void notifyObserverItemRemoved(POSLineItem lineItem) {
            for(OrderObserver observer : observers) {
                observer.lineItemRemoved(this, lineItem);
            }
        }
        void notifyObserverDiscountChanged(POSDiscount discount) {
            for(OrderObserver observer : observers) {
                observer.discountChanged(this, discount);
            }
        }
    }
