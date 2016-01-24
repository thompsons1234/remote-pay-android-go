package com.clover.remote.terminal;

import android.os.Parcel;
import android.os.Parcelable;

public enum TxState implements Parcelable {
  START, SUCCESS, FAIL;

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(final Parcel dest, final int flags) {
    dest.writeString(name());
  }

  public static final Creator<TxState> CREATOR = new Creator<TxState>() {
    @Override
    public TxState createFromParcel(final Parcel source) {
      return TxState.valueOf(source.readString());
    }

    @Override
    public TxState[] newArray(final int size) {
      return new TxState[size];
    }
  };

}
