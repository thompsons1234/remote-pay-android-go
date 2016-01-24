package com.clover.remote.client.messages;

public class VoidPaymentResponse extends TransactionResponse
    {
        private String PaymentId;
        private String TransactionNumber; //optional?
        private String ResponseCode; //optional?
        private String ResponseText; //optional?

        public String getPaymentId() {
            return PaymentId;
        }

        public void setPaymentId(String paymentId) {
            PaymentId = paymentId;
        }

        public String getTransactionNumber() {
            return TransactionNumber;
        }

        public void setTransactionNumber(String transactionNumber) {
            TransactionNumber = transactionNumber;
        }

        public String getResponseCode() {
            return ResponseCode;
        }

        public void setResponseCode(String responseCode) {
            ResponseCode = responseCode;
        }

        public String getResponseText() {
            return ResponseText;
        }

        public void setResponseText(String responseText) {
            ResponseText = responseText;
        }
    }