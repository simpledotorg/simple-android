package org.simple.clinic.home.report

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.visibleOrGone
import java.net.URI
import javax.inject.Inject

class ReportsScreen(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

  @Inject
  lateinit var controller: ReportsScreenController

  var webView: WebView? = null

  private val webViewContainer by bindView<ViewGroup>(R.id.reportsscreen_webview_container)
  private val noReportView by bindView<View>(R.id.reportsscreen_no_report)

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    val screenDestroys = RxView
        .detaches(this)
        .map { ScreenDestroyed() }

    Observable.merge(screenCreates(), screenDestroys)
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(screenDestroys)
        .subscribe { uiChange -> uiChange(this) }
  }

  @SuppressLint("SetJavaScriptEnabled")
  private fun screenCreates() = Observable.create<ScreenCreated> { emitter ->
    val asyncLayoutInflater = AsyncLayoutInflater(context)

    asyncLayoutInflater.inflate(R.layout.reports_webview, webViewContainer) { view, _, _ ->
      val webView = view as WebView
      webView.settings.javaScriptEnabled = true

      this.webView = webView
      showWebview(false)

      this.webViewContainer.addView(this.webView)
      emitter.onNext(ScreenCreated())
    }
  }

  fun showReport(uri: URI) {
    showWebview(true)
    webView?.loadUrl(uri.toString())
  }

  fun showNoReportsAvailable() {
    showWebview(false)
    webView?.loadUrl("about:blank")
  }

  private fun showWebview(isVisible: Boolean) {
    webView?.visibleOrGone(isVisible)
    noReportView.visibleOrGone(isVisible.not())
  }
}
