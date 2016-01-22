package com.clover.remote.client.messages;

import com.clover.common2.Signature2;
import com.clover.sdk.v3.payments.Payment;

    public class AuthResponse extends BaseResponse
    {
        private Payment payment;
        private Signature2 signature;

        public Payment getPayment() {
            return payment;
        }

        public void setPayment(Payment payment) {
            this.payment = payment;
        }

        public Signature2 getSignature() {
            return signature;
        }

        public void setSignature(Signature2 signature) {
            this.signature = signature;
        }
    }

