package com.clover.remote.client.messages;

/**
 * Created by blakewilliams on 1/17/16.
 */
public class CaptureCardRequest extends BaseRequest {
    int cardEntryMethods;

    public CaptureCardRequest(int cardEntryMethods) {
        this.cardEntryMethods = cardEntryMethods;
    }
}
