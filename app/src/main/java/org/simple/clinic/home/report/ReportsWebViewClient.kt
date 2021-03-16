package org.simple.clinic.home.report

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class ReportsWebViewClient : WebViewClient() {

  override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
    val url = request?.url?.toString() ?: return super.shouldOverrideUrlLoading(view, request)

    view?.loadUrl(url)
    return false
  }
}
