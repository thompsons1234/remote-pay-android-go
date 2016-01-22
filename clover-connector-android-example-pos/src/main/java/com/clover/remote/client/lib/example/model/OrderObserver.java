package com.clover.remote.client.lib.example.model;

/**
 * Created by blakewilliams on 12/22/15.
 */
public interface OrderObserver {
    public void lineItemAdded(POSOrder posOrder, POSLineItem lineItem);
    public void lineItemRemoved(POSOrder posOrder, POSLineItem lineItem);
    public void lineItemChanged(POSOrder posOrder, POSLineItem lineItem);
    public void paymentAdded(POSOrder posOrder, POSPayment payment);
    public void refundAdded(POSOrder posOrder, POSRefund refund);
    public void paymentChanged(POSOrder posOrder, POSExchange pay);
    public void discountAdded(POSOrder posOrder, POSDiscount discount);
    public void discountChanged(POSOrder posOrder, POSDiscount discount);
}
