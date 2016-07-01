/*
 * Copyright (C) 2016 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.remote.client.messages;

public class TipAdjustAuthResponse extends BaseResponse {
  private String paymentId;
  private Long tipAmount;
  private boolean success;

  public TipAdjustAuthResponse(boolean success, ResultCode result) {
    super(success, result);
  }

  public String getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  public Long getTipAmount() {
    return tipAmount;
  }

  public void setTipAmount(long tipAmount) {
    this.tipAmount = tipAmount;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }
}