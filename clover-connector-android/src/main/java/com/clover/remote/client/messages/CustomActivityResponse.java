package com.clover.remote.client.messages;

import java.util.HashMap;

public class CustomActivityResponse extends BaseResponse {
  public final String payload;
  public final String failReason;

  public CustomActivityResponse(boolean success, ResultCode code, String payload, String failReason) {
    super(success, code);
    this.payload = payload;
    this.failReason = failReason;
  }
}
