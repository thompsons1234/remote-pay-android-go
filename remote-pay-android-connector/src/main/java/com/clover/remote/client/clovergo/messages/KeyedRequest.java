package com.clover.remote.client.clovergo.messages;

/**
 * Created by jerry.destremps on 11/2/17.
 */

public interface KeyedRequest {
  BillingAddress getBillingAddress();
  String getCardNumber();
  String getExpDate();
  String getCvv();
  boolean isCardPresent();
}
