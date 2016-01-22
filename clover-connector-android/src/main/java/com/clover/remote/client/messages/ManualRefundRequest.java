package com.clover.remote.client.messages;

public class ManualRefundRequest extends BaseRequest
{
    private long amount;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}

