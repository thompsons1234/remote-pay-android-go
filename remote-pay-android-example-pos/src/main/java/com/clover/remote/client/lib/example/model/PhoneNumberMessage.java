package com.clover.remote.client.lib.example.model;

/**
 * Created by glennbedwell on 5/4/17.
 */
public class PhoneNumberMessage extends PayloadMessage{
  public final String phoneNumber;
  public PhoneNumberMessage(String phoneNumber) {
    super("PhoneNumberMessage", RatingsMessageType.PHONE_NUMBER);
    this.phoneNumber = phoneNumber;
  }
}