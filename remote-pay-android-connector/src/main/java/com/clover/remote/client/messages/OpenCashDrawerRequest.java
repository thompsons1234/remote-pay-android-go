package com.clover.remote.client.messages;


/**
 * Request object to open cash drawer
 */
public class OpenCashDrawerRequest extends BaseRequest {

  private String reason = null;

  /**
   * Constructor
   *
   * @param reason String describing the reason to open the drawer
   */
  public OpenCashDrawerRequest(String reason){
    this.reason = reason;
  }

  /**
   * Get the field value
   *
   * @return String describing the reason to open the drawer
   */
  public String getReason() {
    return reason;
  }
}
