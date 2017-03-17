package com.clover.remote.client.messages;

import java.util.HashMap;

public class CustomActivityResponse extends BaseResponse {
  public final String action;
  public final String payload;
  public final String failReason;

  public CustomActivityResponse(boolean success, ResultCode code, String action, String payload, String failReason) {
    super(success, code);
    this.action = action;
    this.payload = payload;
    this.failReason = failReason;
  }
}
