package com.clover.remote.client.clovergo.event;



/**
 * Created by Avdhesh Akhani on 12/22/16.
 */

public final class CardReaderEvent {


    private CardReaderEventType cardReaderEventType;
    private String eventInfo;

    public CardReaderEventType getCardReaderEventType() {
        return cardReaderEventType;
    }

    public void setCardReaderEventType(CardReaderEventType cardReaderEventType) {
        this.cardReaderEventType = cardReaderEventType;
    }

    public String getEventInfo() {
        return eventInfo;
    }

    public void setEventInfo(String eventInfo) {
        this.eventInfo = eventInfo;
    }
}
