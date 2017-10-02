package com.clover.remote.client.messages;


import com.clover.sdk.v3.printer.Printer;

/**
 * Request object to open cash drawer
 */
public class OpenCashDrawerRequest extends BaseRequest {

  private String reason = null;
  private Printer printer = null;

  /**
   * Constructor
   *
   * @param reason String describing the reason to open the drawer
   */
  public OpenCashDrawerRequest(String reason){
    this.reason = reason;
  }

  /**
   * Constructor
   *
   * @param reason String describing the reason to open the drawer
   */
  public OpenCashDrawerRequest(String reason, Printer printer){
    this.reason = reason;
    this.printer = printer;
  }



  /**
   * Get the field value
   *
   * @return String describing the reason to open the drawer
   */
  public String getReason() {
    return reason;
  }

  /**
   * Get the field value
   *
   * @return Printer to use
   */
  public Printer getPrinter() {
    return printer;
  }

  /**
   * Set the field value
   *
   * @param reason string describing reason to open the cash drawer
   */
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * Set the field value
   *
   * @param printer printer to use
   */
  public void setPrinter(Printer printer) {
    this.printer = printer;
  }
}
