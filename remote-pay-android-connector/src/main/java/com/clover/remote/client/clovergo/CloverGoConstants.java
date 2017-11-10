package com.clover.remote.client.clovergo;

/**
 * Created by jerry.destremps on 10/10/17.
 */

public interface CloverGoConstants {

  String CARD_MODE_EMV_CONTACT = "EMV_CONTACT";

  enum TransactionType {
    PRE_AUTH, AUTH, SALE
  }
}
