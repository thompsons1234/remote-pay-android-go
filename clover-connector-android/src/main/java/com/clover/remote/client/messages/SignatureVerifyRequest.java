package com.clover.remote.client.messages;

import com.clover.common2.Signature2;
import com.clover.sdk.v3.payments.Payment;

import java.io.Serializable;

/**
 * Created by blakewilliams on 12/15/15.
 */
public class SignatureVerifyRequest implements Serializable {
    private Signature2 signature;
    private Payment payment;

    public Signature2 getSignature() {
        return signature;
    }

    public void setSignature(Signature2 signature) {
        this.signature = signature;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
