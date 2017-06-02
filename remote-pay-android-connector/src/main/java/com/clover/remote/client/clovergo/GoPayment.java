package com.clover.remote.client.clovergo;


import com.clover.sdk.v3.payments.Payment;

/**
 * Created by Akhani, Avdhesh on 5/30/17.
 */

public class GoPayment extends Payment {

    private boolean signatureRequired;


    public GoPayment() {
    }

    public boolean isSignatureRequired() {
        return signatureRequired;
    }

    public void setSignatureRequired(boolean signatureRequired) {
        this.signatureRequired = signatureRequired;
    }
}
