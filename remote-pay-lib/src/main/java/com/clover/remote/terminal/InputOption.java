package com.clover.remote.terminal;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class InputOption implements Parcelable, Comparable<InputOption> {
  private static final String TAG = InputOption.class.getSimpleName();

  private static final Map<KeyPress, Integer> CONTROL_BUTTON_ORDER = new HashMap<KeyPress,Integer>() {{
    put(KeyPress.ESC, 0);
    put(KeyPress.BACKSPACE, 1);
    put(KeyPress.ENTER, 2);
  }};

  public static final Comparator<InputOption> COMPARATOR = new Comparator<InputOption>() {
    @Override
    public int compare(InputOption io1, InputOption io2) {
      KeyPress kp1 = io1.keyPress;
      //ALog.i(this, "key press 1: %s", kp1);
      KeyPress kp2 = io2.keyPress;
      //ALog.i(this, "key press 2: %s", kp2);

      Integer i1 = CONTROL_BUTTON_ORDER.get(kp1);
      //ALog.i(this, "index 1: %s", i1);
      Integer i2 = CONTROL_BUTTON_ORDER.get(kp2);
      //ALog.i(this, "index 2: %s", i2);


      if (i1 == null) {
        return 1;
      }
      if (i2 == null) {
        return -1;
      }
      if (i1 < i2) {
        //ALog.i(this, "%s < %s", kp1, kp2);
        return -1;
      }
      if (i1 > i2) {
        //ALog.i(this, "%s > %s", kp1, kp2);
        return 1;
      }
      //ALog.i(this, "%s = %s", kp1, kp2);
      return 0;
    }
  };

  public final KeyPress keyPress;
  public final String description;

  public InputOption(KeyPress keyPress, String description) {
    this.keyPress = keyPress;
    this.description = description;
  }

  public InputOption(Parcel in) {
    this.keyPress = in.readParcelable(this.getClass().getClassLoader());
    this.description = in.readString();
  }

  @Override
  public int compareTo(InputOption another) {
    return keyPress.compareTo(another.keyPress);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(final Parcel dest, final int flags) {
    dest.writeParcelable(keyPress, 0);
    dest.writeString(description);
  }

  public static final Creator<InputOption> CREATOR = new Creator<InputOption>() {
    @Override
    public InputOption createFromParcel(final Parcel source) {
      return new InputOption(source);
    }

    @Override
    public InputOption[] newArray(final int size) {
      return new InputOption[size];
    }
  };
}
