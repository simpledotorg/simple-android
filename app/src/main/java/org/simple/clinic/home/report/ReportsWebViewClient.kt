package org.simple.clinic.home.report

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

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
}
