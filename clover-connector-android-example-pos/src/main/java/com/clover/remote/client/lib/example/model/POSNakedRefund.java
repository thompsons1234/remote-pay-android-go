package com.clover.remote.client.lib.example.model;

public class POSNakedRefund
    {
        public String EmployeeID;
        public long Amount;

        public POSNakedRefund(String employeeID, long amount)
        {
            EmployeeID = employeeID;
            Amount = amount;
        }
    }