package com.clover.remote.client.messages;

/**
 * Created by glennbedwell on 3/8/16.
 */
public class ConfigErrorResponse extends BaseRequest {
  private String message;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
