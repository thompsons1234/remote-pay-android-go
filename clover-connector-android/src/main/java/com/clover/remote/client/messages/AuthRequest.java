package com.clover.remote.client.messages;

import com.clover.common2.payments.PayIntent;
import com.clover.remote.protocol.message.Method;
import org.apache.http.MethodNotSupportedException;

/**
 * Created by blakewilliams on 12/15/15.
 */
public class AuthRequest extends SaleRequest {
    private boolean isPreAuth = false;

    public AuthRequest() {
        this(false);
    }
    public AuthRequest(boolean preAuth){
        isPreAuth = preAuth;
    }

    public boolean isPreAuth() {
        return isPreAuth;
    }

    public PayIntent.TransactionType getType(){
        return isPreAuth() ? PayIntent.TransactionType.AUTH : PayIntent.TransactionType.PAYMENT;
    }
}
