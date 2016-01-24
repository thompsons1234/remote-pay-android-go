package com.clover.remote.terminal;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by michaelhampton on 12/2/15.
 */
public enum ErrorCode implements Parcelable {
  ORDER_NOT_FOUND,
  PAYMENT_NOT_FOUND,
  FAIL;

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(final Parcel dest, final int flags) {
    dest.writeString(name());
  }

  public static final Creator<ErrorCode> CREATOR = new Creator<ErrorCode>() {
    @Override
    public ErrorCode createFromParcel(final Parcel source) {
      return ErrorCode.valueOf(source.readString());
    }

    @Override
    public ErrorCode[] newArray(final int size) {
      return new ErrorCode[size];
    }
  };
}


