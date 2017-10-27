package com.clover.remote.client.clovergo.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class DeviceUtil {

  public static void hideKeyboard(View view) {
    if (view == null)
      return;

    ((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
  }
}