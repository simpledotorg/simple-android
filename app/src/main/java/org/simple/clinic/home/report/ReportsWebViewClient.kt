package org.simple.clinic.home.report

import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi

class ReportsWebViewClient(
    private val backClicked: () -> Unit
) : WebViewClient() {

  override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
    val url = request?.url?.toString() ?: return super.shouldOverrideUrlLoading(view, request)

    if (url == "simple://progress-tab") {
      backClicked.invoke()
    }
    view?.loadUrl(url)
    return false
  }

  override fun onPageFinished(view: WebView?, url: String?) {
    /**
     * Since we are loading the progress page from database,
     * it doesn't have a proper URL. `about:blank` url here indicates
     * that the progress tab is loaded.
     *
     * Once progress tab is loaded, we are clearing the WebView history,
     * so that the system back navigation can be properly handled.
     */
    if (url == "about:blank") {
      view?.clearHistory()
    }
    super.onPageFinished(view, url)
  }

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onReceivedError(
      view: WebView?,
      request: WebResourceRequest?,
      error: WebResourceError?
  ) {
    super.onReceivedError(view, request, error)
    handleError(webView = view, errorCode = error?.errorCode)
  }

  @Suppress("OVERRIDE_DEPRECATION")
  override fun onReceivedError(
      view: WebView?,
      errorCode: Int,
      description: String?,
      failingUrl: String?
  ) {
    super.onReceivedError(view, errorCode, description, failingUrl)
    handleError(webView = view, errorCode = errorCode)
  }

  private fun handleError(webView: WebView?, errorCode: Int?) {
    if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT) {
      webView?.loadUrl("file:///android_asset/drug_stock_no_connection.html")
    }
  }
}
