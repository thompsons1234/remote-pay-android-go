package com.clover.remote.client.messages;

import com.clover.remote.PendingPaymentEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by blakewilliams on 7/21/16.
 */
public class RetrievePendingPaymentsResponse extends BaseResponse {
  List<PendingPaymentEntry> pendingPayemnts;

  public RetrievePendingPaymentsResponse(ResultCode code, String message, List<PendingPaymentEntry> payments) {
    super(code == ResultCode.SUCCESS, code);
    pendingPayemnts = payments == null ? Collections.EMPTY_LIST : new ArrayList<PendingPaymentEntry>(payments);
  }

  public List<PendingPaymentEntry> getPendingPayemnts() {
    return pendingPayemnts;
  }
}
