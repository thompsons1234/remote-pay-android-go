package com.clover.remote.client;

import android.graphics.Bitmap;

import java.util.List;

public interface IPrintConnector {
  /**
   * Print simple lines of text to the Clover Mini printer
   *
   * @param messages -
   **/
  void printText(List<String> messages);

  /**
   * Print an image on the Clover Mini printer
   *
   * @param image -
   **/
  void printImage(Bitmap image);

  /**
   * Print an image on the Clover Mini printer
   * @param url
   */
  void printImageFromURL(String url);


}
