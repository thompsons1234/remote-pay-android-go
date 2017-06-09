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

import com.clover.remote.QueryStatus;
import com.clover.sdk.v3.payments.Payment;

public class RetrievePaymentResponse extends BaseResponse {
  private String externalPaymentId;
  private Payment payment;
  private QueryStatus queryStatus;

  public RetrievePaymentResponse(ResultCode code, String message, String externalPaymentId, QueryStatus queryStatus, Payment payment) {
    super(code == ResultCode.SUCCESS, code);
    this.setMessage(message);
    this.externalPaymentId = externalPaymentId;
    this.payment = payment;
    this.queryStatus = queryStatus;
  }

  public String getExternalPaymentId(){
    return externalPaymentId;
  }

  public void setExternalId(String externalPaymentId) {
    this.externalPaymentId = externalPaymentId;
  }

  public Payment getPayment() {
    return payment;
  }

  public void setPayment(Payment payment) {
    this.payment = payment;
  }

  public QueryStatus getQueryStatus() {
    return queryStatus;
  }

  public void setQueryStatus(QueryStatus queryStatus) {
    this.queryStatus = queryStatus;
  }

}
