/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */

package com.clover.remote.client.messages;

import com.clover.common2.payments.PayIntent;

@SuppressWarnings(value="unused")
public class PreAuthRequest extends TransactionRequest {

  public PreAuthRequest(long amount, String externalId){
    super(amount, externalId);
  }

  @Override public PayIntent.TransactionType getType() {
    return PayIntent.TransactionType.AUTH;
  }
}
