package com.clover.remote.client.messages;

import com.clover.common2.payments.PayIntent;
import com.clover.sdk.v3.payments.VaultedCard;

/**
 * Created by blakewilliams on 12/14/15.
 */
public class SaleRequest {
    private long amount;
    private Long tipAmount;
    private Long tippableAmount;
    private Long taxAmount;
    private Integer cardEntryMethods;
    private boolean cardNotPresent;
    private VaultedCard VaultedCard;

    /*
        public bool DisableCashback { get; set; } //
        public bool DisableTip { get; set; } // if the merchant account is
        public bool DisablePrinting { get; set; }
        public bool DisableRestartTransactionOnFail { get; set; }
     */

    public PayIntent.TransactionType getType(){
        return PayIntent.TransactionType.PAYMENT;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public Long getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(Long tipAmount) {
        this.tipAmount = tipAmount;
    }

    public Long getTippableAmount() {
        return tippableAmount;
    }

    public void setTippableAmount(Long tippableAmount) {
        this.tippableAmount = tippableAmount;
    }

    public Long getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Long taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Integer getCardEntryMethods() {
        return cardEntryMethods;
    }

    public void setCardEntryMethods(Integer cardEntryMethods) {
        this.cardEntryMethods = cardEntryMethods;
    }

    public boolean isCardNotPresent() {
        return cardNotPresent;
    }

    public void setCardNotPresent(boolean cardNotPresent) {
        this.cardNotPresent = cardNotPresent;
    }

    public com.clover.sdk.v3.payments.VaultedCard getVaultedCard() {
        return VaultedCard;
    }

    public void setVaultedCard(com.clover.sdk.v3.payments.VaultedCard vaultedCard) {
        VaultedCard = vaultedCard;
    }
}
