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

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onReceivedError(
      view: WebView?,
      request: WebResourceRequest?,
      error: WebResourceError?
  ) {
    super.onReceivedError(view, request, error)
    handleError(webView = view, errorCode = error?.errorCode)
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
