package com.clover.remote.client.messages;

import com.clover.sdk.v3.payments.Credit;

public class ManualRefundResponse extends TransactionResponse
    {
        private Credit Credit;
        private String TransactionNumber;
        private String ResponseCode;
        private String ResponseText;

        public com.clover.sdk.v3.payments.Credit getCredit() {
            return Credit;
        }

        public void setCredit(com.clover.sdk.v3.payments.Credit credit) {
            Credit = credit;
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