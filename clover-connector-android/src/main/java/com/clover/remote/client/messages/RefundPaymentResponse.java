package com.clover.remote.client.messages;

import com.clover.sdk.v3.payments.Refund;

public class RefundPaymentResponse extends BaseResponse
{
    private String orderId;
    private String paymentId;
    private Refund refundObj;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Refund getRefundObj() {
        return refundObj;
    }

    public void setRefundObj(Refund refundObj) {
        this.refundObj = refundObj;
    }

    //public TxState Code { get; set; }// BaseResponse.Case is a string, so won't serialize
}