package com.clover.remote.client.messages;

public class CustomActivityResponse extends BaseResponse {
  public final String payload;
  public final String action;

  public CustomActivityResponse(boolean success, ResultCode code, String payload, String failReason, String action) {
    super(success, code);
    this.setReason(failReason);
    this.payload = payload;
    this.action = action;
  }
}
