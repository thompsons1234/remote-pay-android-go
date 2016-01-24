package com.clover.remote.terminal;

import android.os.Parcel;
import android.os.Parcelable;

public enum KeyPress implements Parcelable {
  NONE((byte)0x00),
  ENTER((byte)0x28),
  ESC((byte)0x29),
  BACKSPACE((byte)0x2a),
  TAB((byte)0x2b),
  STAR((byte)0x55),

  BUTTON_1((byte)0x3a),
  BUTTON_2((byte)0x3b),
  BUTTON_3((byte)0x3c),
  BUTTON_4((byte)0x3d),
  BUTTON_5((byte)0x3e),
  BUTTON_6((byte)0x3f),
  BUTTON_7((byte)0x40),
  BUTTON_8((byte)0x41),

  DIGIT_1((byte)0x59),
  DIGIT_2((byte)0x5a),
  DIGIT_3((byte)0x5b),
  DIGIT_4((byte)0x5c),
  DIGIT_5((byte)0x5d),
  DIGIT_6((byte)0x5e),
  DIGIT_7((byte)0x5f),
  DIGIT_8((byte)0x60),
  DIGIT_9((byte)0x61),
  DIGIT_0((byte)0x62);

  public final byte data;

  KeyPress(byte data) {
    this.data = data;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(final Parcel dest, final int flags) {
    dest.writeByte(data);
  }

  public static final Creator<KeyPress> CREATOR = new Creator<KeyPress>() {
    @Override
    public KeyPress createFromParcel(final Parcel source) {
      byte parcelKeyData = source.readByte();

      for (KeyPress keyPress : KeyPress.values()) {
        if (keyPress.data == parcelKeyData) {
          return keyPress;
        }
      }

      return null;
    }

    @Override
    public KeyPress[] newArray(final int size) {
      return new KeyPress[size];
    }
  };

}
