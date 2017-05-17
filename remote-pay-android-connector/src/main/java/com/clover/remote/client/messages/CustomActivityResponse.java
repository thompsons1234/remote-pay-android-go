package com.clover.remote.client.messages;

import java.util.HashMap;

public class CustomActivityResponse extends BaseResponse {
  public final String payload;
  public final String failReason;
  public final String action;

  public CustomActivityResponse(boolean success, ResultCode code, String payload, String failReason, String action) {
    super(success, code);
    this.payload = payload;
    this.failReason = failReason;
    this.action = action;
  }
}
