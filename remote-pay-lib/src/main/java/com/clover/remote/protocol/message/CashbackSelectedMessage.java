package com.clover.remote.protocol.message;

public class CashbackSelectedMessage extends Message {
  public final long cashbackAmount;

  public CashbackSelectedMessage(long cashbackAmount) {
    super(Method.CASHBACK_SELECTED);
    this.cashbackAmount = cashbackAmount;
  }
}
