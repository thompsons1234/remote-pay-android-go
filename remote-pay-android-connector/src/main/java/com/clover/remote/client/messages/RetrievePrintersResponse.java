package com.clover.remote.client.messages;

import com.clover.sdk.v3.printer.Printer;

import java.util.ArrayList;
import java.util.List;

/**
 * Response object for retrieving the printers
 */
public class RetrievePrintersResponse {

  private List<Printer> printers = new ArrayList<>();

  /**
   * Constructor
   *
   * @param printers a list of printers being passed back
   */
  public RetrievePrintersResponse(Printer[] printers){
    for( Printer printer : printers){
      this.printers.add(printer);
    }
  }

  /**
   * Get the field value
   *
   * @return a list of printers
   */
  public List<Printer> getPrinters() {
    return printers;
  }
}
