package com.clover.remote.client.messages;

public class TipAdjustAuthRequest extends BaseRequest
    {
        private String paymentID;
        private String orderID;
        private long tipAmount;

        public String getOrderID() {
            return orderID;
        }

        public void setOrderID(String orderID) {
            this.orderID = orderID;
        }

        public String getPaymentID() {
            return paymentID;
        }

        public void setPaymentID(String paymentID) {
            this.paymentID = paymentID;
        }

        public long getTipAmount() {
            return tipAmount;
        }

        public void setTipAmount(long tipAmount) {
            this.tipAmount = tipAmount;
        }
    }



