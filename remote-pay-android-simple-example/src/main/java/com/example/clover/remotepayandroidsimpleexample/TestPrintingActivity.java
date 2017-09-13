package com.example.clover.remotepayandroidsimpleexample;

import com.clover.remote.client.ICloverConnector;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by rachel.antion on 9/13/17.
 */

public class TestPrintingActivity extends Activity {
  private static ICloverConnector cloverConnector;
  private final String TAG = TestPrintingActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test_printing);
  }

  private void exit(){
    Log.d(TAG,"exiting");
    cloverConnector.showWelcomeScreen();
    cloverConnector.dispose();
    synchronized (this) {
      notifyAll();
    }
  }
}
