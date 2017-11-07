package com.clover.remote.client.clovergo;

/**
 * Created by jerry.destremps on 10/10/17.
 */

public interface CloverGoConstants {

  String CARD_MODE_EMV_CONTACT = "EMV_CONTACT";

  String TRANSACTION_TYPE_ARG = "TRANSACTION_TYPE_ARG";

  enum TransactionType {
    PRE_AUTH, AUTH, SALE
  }
}
