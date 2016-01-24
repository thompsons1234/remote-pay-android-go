package com.clover.remote.client.lib.example.model;

import java.io.Serializable;

public class POSExchange
    {
        public String paymentID;
        public String orderID;
        public String employeeID;
        public long amount;

        public POSExchange(String paymentID, String orderID, String employeeID, long amount)
        {
            this.paymentID = paymentID;
            this.orderID = orderID;
            this.employeeID = employeeID;
            this.amount = amount;
        }

        public String getPaymentID() {
            return paymentID;
        }

        public long getAmount() {
            return amount;
        }

        public String getOrderId() {
            return orderID;
        }
    }