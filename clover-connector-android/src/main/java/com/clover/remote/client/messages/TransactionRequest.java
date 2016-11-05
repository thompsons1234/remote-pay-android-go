/**
 */

package com.clover.remote.client.messages;

import com.clover.common2.payments.PayIntent;
import com.clover.sdk.v3.payments.DataEntryLocation;
import com.clover.sdk.v3.payments.TipMode;
import com.clover.sdk.v3.payments.TransactionSettings;

/**
 * base class used by all transaction requests
 * SaleRequest, AuthRequest, PreAuthRequest and ManualRefundRequest
 */
@SuppressWarnings(value="unused")
public abstract class TransactionRequest extends BaseRequest {

  private java.lang.Boolean cloverShouldHandleReceipts = null;
  private java.lang.Boolean cardNotPresent = null;
  private java.lang.Boolean disableRestartTransactionOnFail = null;
  private long amount;
  private java.lang.Integer cardEntryMethods = null;
  private com.clover.sdk.v3.payments.VaultedCard vaultedCard = null;
  private java.lang.String externalId = null;
  private PayIntent.TransactionType type = null;
  private java.lang.Long signatureThreshold = null;
  private DataEntryLocation signatureEntryLocation = null;
  private java.lang.Boolean disableReceiptSelection = null;
  private java.lang.Boolean disableDuplicateChecking = null;

  public TransactionRequest(long amount, String externalId) {
    if(externalId == null || externalId.length() > 32) {
      throw new IllegalArgumentException("The externalId must be provided.  The maximum length is 32 characters.");
    }
    this.amount = amount;
    this.externalId = externalId;
  }
    
  /**
  * Set the field value
  * Do not print
  *
  */
  public void setCloverShouldHandleReceipts(java.lang.Boolean cloverShouldHandleReceipts) {
    this.cloverShouldHandleReceipts = cloverShouldHandleReceipts;
  }

  /**
  * Get the field value
  * Do not print
  */
  public java.lang.Boolean getCloverShouldHandleReceipts() {
    return this.cloverShouldHandleReceipts;
  }  
  /**
  * Set the field value
  * If true then card not present is accepted
  *
  */
  public void setCardNotPresent(java.lang.Boolean cardNotPresent) {
    this.cardNotPresent = cardNotPresent;
  }

  /**
  * Get the field value
  * If true then card not present is accepted
  */
  public java.lang.Boolean getCardNotPresent() {
    return this.cardNotPresent;
  }  
  /**
  * Set the field value
  * If the transaction times out or fails because of decline, do not restart it
  *
  */
  public void setDisableRestartTransactionOnFail(java.lang.Boolean disableRestartTransactionOnFail) {
    this.disableRestartTransactionOnFail = disableRestartTransactionOnFail;
  }

  /**
  * Get the field value
  * If the transaction times out or fails because of decline, do not restart it
  */
  public java.lang.Boolean getDisableRestartTransactionOnFail() {
    return this.disableRestartTransactionOnFail;
  }  
  /**
  * Set the field value
  * Total amount paid
  *
  */
  public void setAmount(long amount) {
    this.amount = amount;
  }

  /**
  * Get the field value
  * Total amount paid
  */
  public long getAmount() {
    return this.amount;
  }  
  /**
  * Set the field value
  * Allowed entry methods
  *
  */
  public void setCardEntryMethods(java.lang.Integer cardEntryMethods) {
    this.cardEntryMethods = cardEntryMethods;
  }

  /**
  * Get the field value
  * Allowed entry methods
  */
  public java.lang.Integer getCardEntryMethods() {
    return this.cardEntryMethods;
  }  
  /**
  * Set the field value
  * A saved card
  *
  */
  public void setVaultedCard(com.clover.sdk.v3.payments.VaultedCard vaultedCard) {
    this.vaultedCard = vaultedCard;
  }

  /**
  * Get the field value
  * A saved card
  */
  public com.clover.sdk.v3.payments.VaultedCard getVaultedCard() {
    return this.vaultedCard;
  }  
  /**
  * Set the field value
  * An id that will be persisted with transactions.
  *
  */
  public void setExternalId(java.lang.String externalId) {
    this.externalId = externalId;
  }

  /**
  * Get the field value
  * An id that will be persisted with transactions.
  */
  public java.lang.String getExternalId() {
    return this.externalId;
  }  


  /**
  * Get the field value
  * The type of the transaction.
  */
  abstract public PayIntent.TransactionType getType();

  /**
   * Set the field value
   * The signature threshold settings overrides
   *
   */
  public void setSignatureThreshold(Long signatureThreshold) {
    this.signatureThreshold = signatureThreshold;
  }

  /**
   * Get the field value
   * The transaction level settings overrides
   */
  public Long getSignatureThreshold() {
    return this.signatureThreshold;
  }

  /**
   * Set the field value
   * The signature entry location settings overrides
   *
  */
  public void setSignatureEntryLocation(DataEntryLocation signatureEntryLocation) {
    this.signatureEntryLocation = signatureEntryLocation;
  }

  /**
   * Get the field value
   * The transaction level settings overrides
   */
  public DataEntryLocation getSignatureEntryLocation() {
    return this.signatureEntryLocation;
  }

  /**
   * Set the field value
   * The disable receipt options screen settings overrides
   *
   */
  public void setDisableReceiptSelection(Boolean disableReceiptSelection) {
    this.disableReceiptSelection = disableReceiptSelection;
  }

  /**
   * Get the field value
   * The disable receipt options screen settings overrides
   */
  public Boolean getDisableReceiptSelection() {
    return this.disableReceiptSelection;
  }

  /**
   * Set the field value
   * The disable duplicate transaction validation settings overrides
   *
   */
  public void setDisableDuplicateChecking(Boolean disableDuplicateChecking) {
    this.disableDuplicateChecking = disableDuplicateChecking;
  }

  /**
   * Get the field value
   * The disable duplicate transaction validation settings overrides
   */
  public Boolean getDisableDuplicateChecking() {
    return this.disableDuplicateChecking;
  }


}
