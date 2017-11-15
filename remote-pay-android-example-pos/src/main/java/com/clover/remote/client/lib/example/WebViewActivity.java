package com.clover.remote.client.lib.example;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {
  public static final String EXTRA_CLOVER_GO_BASE_URL = "EXTRA_CLOVER_GO_BASE_URL";
  private static final String SCHEME = "clovergooauth";
  private static final String HOST = "oauthresult";

  private WebView webView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_webview);

    String baseUrl = getIntent().getStringExtra(EXTRA_CLOVER_GO_BASE_URL);
    String url = baseUrl + "&redirect_uri=" + SCHEME + "://" + HOST;

    webView = (WebView) findViewById(R.id.webView);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.setWebViewClient(new WebViewClient() {
      public void onPageStarted(WebView view, String url, Bitmap favicon) {
        String accessTokenFragment = "#access_token=";

        int accessTokenStart = url.indexOf(accessTokenFragment);
        if (accessTokenStart > -1) {
          String accessToken = url.substring(accessTokenStart + accessTokenFragment.length(), url.length());

          Intent output = new Intent();
          output.putExtra(StartupActivity.EXTRA_CLOVER_GO_ACCESS_TOKEN, accessToken);
          setResult(RESULT_OK, output);
          finish();
        }
      }
    });
    webView.loadUrl(url);
  }
}