package com.clover.remote.client.messages;

/**
 * Created by rachel.antion on 9/12/17.
 */

/**
 * Request for status of print job
 */
public class PrintJobStatusRequest {
private String printRequestId = null;

  /**
   * Constructor
   *
   * @param printRequestId id of the print job to be retrieved
   */
  public PrintJobStatusRequest(String printRequestId){
    this.printRequestId = printRequestId;
  }

  /**
   * Get the field value
   *
   * @return id of the print job to be retrieved
   */
  public String getPrintRequestId() {
    return printRequestId;
  }
}
