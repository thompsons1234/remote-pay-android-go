package com.clover.remote.client.messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rachel.antion on 9/12/17.
 */

public class PrintJobStatusResponse {

  private List<String> printRequestId = new ArrayList<>();
  private PrintJobStatus status = null;

  public PrintJobStatusResponse(String printRequestId, PrintJobStatus status){
    this.printRequestId.add(printRequestId);
    this.status = status;
  }

  public PrintJobStatusResponse(PrintJobStatus status){
    this(null, status);
  }


}
