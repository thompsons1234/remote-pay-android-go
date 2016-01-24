package com.clover.remote.client.messages;


import com.clover.sdk.v3.customers.Card;

/**
 * Created by blakewilliams on 1/17/16.
 */
public class CaptureCardResponse extends BaseResponse {
    private Card card;
    public CaptureCardResponse(Card card) {
        this.card = card;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}
