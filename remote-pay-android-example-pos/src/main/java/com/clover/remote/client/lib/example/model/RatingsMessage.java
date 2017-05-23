package com.clover.remote.client.lib.example.model;

/**
 * Created by glennbedwell on 5/4/17.
 */
public class RatingsMessage extends PayloadMessage {
  public final Rating[] ratings;
  public RatingsMessage(Rating[] ratings) {
    super("RatingsMessage", MessageType.RATINGS);
    this.ratings = ratings;
  }
}