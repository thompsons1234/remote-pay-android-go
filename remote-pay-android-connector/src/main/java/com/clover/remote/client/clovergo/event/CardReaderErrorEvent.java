package com.clover.remote.client.clovergo.event;



/**
 * Card Reader event class encapsulating event information and associated data about card reader errors
 */
public final class CardReaderErrorEvent {

    private CardReaderErrorEventType cardReaderErrorEventType;
    private String eventInfo;

    /**
         @param - return information about error
     */
    public String getEventInfo() {
        return eventInfo;
    }

    public void setEventInfo(String eventInfo) {
        this.eventInfo = eventInfo;
    }

    /**
        @return - Return reader error event type
     */
    public CardReaderErrorEventType getCardReaderErrorEventType() {
        return cardReaderErrorEventType;
    }

    public void setCardReaderEventType(CardReaderErrorEventType cardReaderErrorEventType) {
        this.cardReaderErrorEventType = cardReaderErrorEventType;
    }

}
