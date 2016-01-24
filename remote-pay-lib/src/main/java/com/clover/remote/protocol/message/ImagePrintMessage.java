package com.clover.remote.protocol.message;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class ImagePrintMessage extends Message {
  public final byte[] png;

  public ImagePrintMessage(byte[] png) {
    super(Method.PRINT_IMAGE);
    this.png = png;
  }

  public ImagePrintMessage(Bitmap bitmap) {
    super(Method.PRINT_IMAGE);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);

    this.png = os.toByteArray();
  }

  public Bitmap getBitmap() {
    return BitmapFactory.decodeByteArray(png, 0, png.length);
  }
}
