package com.clover.remote.client.clovergo.messages;

import com.clover.remote.client.messages.SaleRequest;
import com.firstdata.clovergo.domain.utils.CreditCardUtil;
import com.firstdata.clovergo.domain.utils.TextUtil;

/**
 * Created by Akhani, Avdhesh on 6/19/17.
 */

public class KeyedSaleRequest extends SaleRequest implements KeyedRequest {

  private String cardNumber;
  private String expDate;
  private String cvv;
  private BillingAddress billingAddress;
  private boolean cardPresent;

  public KeyedSaleRequest(long amount, String externalId, String cardNumber, String expDate, String cvv) {
    super(amount, externalId);
    this.cardNumber = TextUtil.returnAllNumeric(cardNumber);
    this.expDate = TextUtil.returnAllNumeric(expDate);
    this.cvv = TextUtil.returnAllNumeric(cvv);
    this.cardPresent = true;
  }

  @Override
  public BillingAddress getBillingAddress() {
    return billingAddress;
  }

  public void setBillingAddress(BillingAddress billingAddress) {
    this.billingAddress = billingAddress;
  }

  public void setCardPresent(boolean cardPresent) {
    this.cardPresent = cardPresent;
  }

  @Override
  public String getCardNumber() {
    return this.cardNumber;
  }

  @Override
  public String getExpDate() {
    return this.expDate;
  }

  @Override
  public String getCvv() {
    return this.cvv;
  }

  @Override
  public boolean isCardPresent() {
    return this.cardPresent;
  }

  public boolean validateCard() {
    return CreditCardUtil.validateCard(this.cardNumber);
  }

  public boolean validateCardExpiry() {
    return CreditCardUtil.validateCardExpiry(this.expDate);
  }

}
