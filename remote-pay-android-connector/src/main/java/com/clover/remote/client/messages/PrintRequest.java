package com.clover.remote.client.messages;

import android.media.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Request object for requesting a print job.
 */
public class PrintRequest {

  public List<Image> image = new ArrayList<>();
  public List<URL> imageURL = new ArrayList<>();
  public List<String> text = new ArrayList<>();
  public String printRequestId = null;
  public String printDeviceId = null;

  /**
   * Constructor
   *
   * Create a PrintRequest to print a given image
   * @param image Image to print
   * @param printRequestId identifier to give to the print job, so it can be later queried
   * @param printDeviceId identifier to specify printer to use
   */
  public PrintRequest(Image image, String printRequestId, String printDeviceId){
    this.image.add(image);
    this.printRequestId = printRequestId;
    this.printDeviceId = printDeviceId;
  }

  /**
   * Constructor
   *
   * Create a PrintRequest to print an image at a given URL
   * @param imageUrl URL to the image to print
   * @param printRequestId identifier to give to the print job, so it can be later queried
   * @param printDeviceId identifier to specify printer to use
   */
  public PrintRequest(URL imageUrl, String printRequestId, String printDeviceId){
    this.imageURL.add(imageUrl);
    this.printRequestId = printRequestId;
    this.printDeviceId = printDeviceId;
  }

  /**
   * Constructor
   *
   * Create a PrintRequest to print an array of strings to print
   * @param text Array of strings to be printed
   * @param printRequestId identifier to give to the print job, so it can be later queried
   * @param printDeviceId identifier to specify printer to use
   */
  public PrintRequest(String[] text, String printRequestId, String printDeviceId){
    for (String line: text) {
      this.text.add(line);
    }
    this.printRequestId = printRequestId;
    this.printDeviceId = printDeviceId;
  }
  /**
   * Constructor
   *
   * Create a PrintRequest to print a given image
   * @param image Image to print
   */
  public PrintRequest(Image image){
    this(image, null, null);
  }

  /**
   * Constructor
   *
   * Create a PrintRequest to print a given image
   * @param imageUrl URL to the image to print
   */
  public PrintRequest(URL imageUrl){
    this(imageUrl, null, null);
  }

  /**
   * Constructor
   *
   * Create a PrintRequest to print a given image
   * @param text Array of strings to be printed
   */
  public PrintRequest(String[] text){
    this(text, null, null);
  }

  /**
   * Get the field value
   *
   * @return Image to print
   */
  public Image getImage(){
    return this.image.get(0);
  }

  /**
   * Get the field value
   *
   * @return URL of image to print
   */
  public URL getImageURL() {
    return this.imageURL.get(0);
  }

  /**
   * Get the field value
   *
   * @return list of strings to be printed
   */
  public String[] getText() {
    return text.toArray(new String[0]);
  }

  /**
   * Get the field value
   *
   * @return identifier to give to the print job, so it can later be queried
   */
  public String getPrintRequestId() {
    return printRequestId;
  }

  /**
   * Get the field value
   *
   * @return identifier to specify printer to use
   */
  public String getPrintDeviceId() {
    return printDeviceId;
  }

}
