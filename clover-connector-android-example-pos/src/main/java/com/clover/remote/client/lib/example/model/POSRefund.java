package com.clover.remote.client.lib.example.model;

public class POSRefund extends POSExchange
    {
        public POSRefund(String paymentID, String orderID, String employeeID, long amount)
        {
            super(paymentID, orderID, employeeID, amount);
        }
    }