package com.clover.remote.client.lib.example.qrCode.barcode;

import com.clover.remote.client.lib.example.R;
import com.clover.remote.client.lib.example.qrCode.camera.CameraSource;
import com.clover.remote.client.lib.example.qrCode.camera.CameraSourcePreview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public final class BarcodeCaptureActivity extends Activity
    implements BarcodeTracker.BarcodeGraphicTrackerCallback {

  public static final String BarcodeObject = "Barcode";
  private static final String TAG = "Barcode-reader";
  private static final int RC_HANDLE_GMS = 9001;
  private CameraSource mCameraSource;
  private CameraSourcePreview mPreview;

  /**
   * Initializes the UI and creates the detector pipeline.
   */
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.barcode_capture);

    mPreview = (CameraSourcePreview) findViewById(R.id.preview);

    boolean autoFocus = true;
    boolean useFlash = false;

    int rc = PackageManager.PERMISSION_GRANTED;
    if (rc == PackageManager.PERMISSION_GRANTED) {
      createCameraSource(autoFocus, useFlash);
    } else {
      finish();
    }
  }

  @Override
  public void onDetectedQrCode(Barcode barcode) {
    if (barcode != null) {
      Intent intent = new Intent();
      intent.putExtra(BarcodeObject, barcode);
      setResult(CommonStatusCodes.SUCCESS, intent);
      finish();
    }
  }

  /**
   * Creates and starts the camera.
   * <p/>
   * Suppressing InlinedApi since there is a check that the minimum version is met before using
   * the constant.
   */
  @SuppressLint("InlinedApi")
  private void createCameraSource(boolean autoFocus, boolean useFlash) {
    Context context = getApplicationContext();

    BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context)
        .setBarcodeFormats(Barcode.ALL_FORMATS)
        .build();
    BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(this);
    barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

    if (!barcodeDetector.isOperational()) {
      Log.w(TAG, "Detector dependencies are not yet available.");

      IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
      boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

      if (hasLowStorage) {
        Toast.makeText(this, R.string.low_storage_error,
            Toast.LENGTH_LONG).show();
        Log.w(TAG, getString(R.string.low_storage_error));
      }
    }

    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);

    CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
        .setFacing(CameraSource.CAMERA_FACING_BACK)
        .setRequestedPreviewSize(metrics.widthPixels / 2, metrics.heightPixels / 2)
        .setRequestedFps(24.0f);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      builder = builder.setFocusMode(
          autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);
    }

    mCameraSource = builder
        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
        .build();
  }

  @Override
  protected void onResume() {
    super.onResume();
    startCameraSource();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mPreview != null) {
      mPreview.stop();
    }
  }

  /**
   * Releases the resources associated with the camera source, the associated detectors, and the
   * rest of the processing pipeline.
   */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mPreview != null) {
      mPreview.release();
    }
  }

  /**
   * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() throws SecurityException {
    int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
        getApplicationContext());
    if (code != ConnectionResult.SUCCESS) {
      Dialog dlg =
          GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
      dlg.show();
    }

    if (mCameraSource != null) {
      try {
        mPreview.start(mCameraSource);
      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        mCameraSource.release();
        mCameraSource = null;
      }
    }
  }
}
