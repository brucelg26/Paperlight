package dev.paperlight.reader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {
    private static final int PICK_PDF_REQUEST = 402;
    private static final String LOCAL_HOST = "appassets.androidplatform.net";
    private WebView webView;
    private ValueCallback<Uri[]> pendingFileResult;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setDefaultTextEncodingName("UTF-8");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (!"https".equals(uri.getScheme()) || !LOCAL_HOST.equals(uri.getHost())) {
                    return null;
                }
                String path = uri.getPath();
                if (path == null || !path.startsWith("/assets/")) {
                    return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found",
                            java.util.Collections.emptyMap(), new java.io.ByteArrayInputStream(new byte[0]));
                }
                String assetName = path.substring("/assets/".length());
                if (assetName.contains("..") || assetName.startsWith("/") || assetName.isEmpty()) {
                    return new WebResourceResponse("text/plain", "UTF-8", 403, "Forbidden",
                            java.util.Collections.emptyMap(), new java.io.ByteArrayInputStream(new byte[0]));
                }
                try {
                    InputStream stream = getAssets().open(assetName);
                    String mime = assetName.endsWith(".js") ? "text/javascript" :
                            assetName.endsWith(".html") ? "text/html" :
                            assetName.endsWith(".png") ? "image/png" :
                            assetName.endsWith(".css") ? "text/css" : "application/octet-stream";
                    return new WebResourceResponse(mime, assetName.endsWith(".js") || assetName.endsWith(".html") ? "UTF-8" : null, stream);
                } catch (IOException ex) {
                    return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found",
                            java.util.Collections.emptyMap(), new java.io.ByteArrayInputStream(new byte[0]));
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> result, FileChooserParams params) {
                if (pendingFileResult != null) pendingFileResult.onReceiveValue(null);
                pendingFileResult = result;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf");
                try {
                    startActivityForResult(intent, PICK_PDF_REQUEST);
                } catch (Exception ex) {
                    pendingFileResult.onReceiveValue(null);
                    pendingFileResult = null;
                }
                return true;
            }
        });

        webView.loadUrl("https://" + LOCAL_HOST + "/assets/index.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && pendingFileResult != null) {
            Uri uri = (resultCode == RESULT_OK && data != null) ? data.getData() : null;
            pendingFileResult.onReceiveValue(uri == null ? null : new Uri[]{uri});
            pendingFileResult = null;
        }
    }

    @Override
    public void onBackPressed() {
        webView.evaluateJavascript(
            "document.getElementById('reader')?.classList.contains('active') ? 'reading' : 'home'",
            result -> {
                if (result.contains("reading")) {
                    webView.evaluateJavascript("document.getElementById('homeBtn')?.click()", null);
                } else {
                    MainActivity.super.onBackPressed();
                }
            }
        );
    }

    @Override
    protected void onDestroy() {
        if (pendingFileResult != null) {
            pendingFileResult.onReceiveValue(null);
            pendingFileResult = null;
        }
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
