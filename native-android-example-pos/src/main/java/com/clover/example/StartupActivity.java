/*
 * Copyright (C) 2016 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;

public class StartupActivity extends Activity {

  public static final String TAG = StartupActivity.class.getSimpleName();
  public static final String EXAMPLE_APP_NAME = "EXAMPLE_NATIVE_APP";
  public static final String CONNECTION_MODE = "CONNECTION_MODE";
  public static final String NATIVE = "NATIVE";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_startup);

    getActionBar().hide();

    Intent intent = new Intent();
    intent.setClass(this, NativePOSActivity.class);
    intent.putExtra(NativePOSActivity.EXTRA_CLOVER_CONNECTOR_CONFIG, NATIVE);
    startActivity(intent);
  }

}
