package com.clover.remote.client.messages;


/**
 * Request to retrieve all available printers
 */
public class RetrievePrintersRequest {
  private PrintCategory category = null;

  /**
   * Constructor
   *
   * @param printCategory category of printers to retrieve
   */
  public RetrievePrintersRequest(PrintCategory printCategory){
    this.category = printCategory;
  }

  /**
   * Get the field value
   *
   * @return category of printers to retrieve
   */
  public PrintCategory getCategory() {
    return category;
  }
}
