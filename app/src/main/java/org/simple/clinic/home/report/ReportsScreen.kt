package org.simple.clinic.home.report

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.*
import io.reactivex.schedulers.Schedulers.*
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.visibleOrGone
import org.simple.clinic.widgets.ScreenCreated
import java.net.URI
import javax.inject.Inject

class ReportsScreen(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

  companion object {
    val KEY = ReportsScreenKey()
  }

  @Inject
  lateinit var controller: ReportsScreenController

  private val webView by bindView<WebView>(R.id.reportsscreen_webview)
  private val noReportView by bindView<View>(R.id.reportsscreen_no_report)

  @SuppressLint("SetJavaScriptEnabled", "CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }

    webView.settings.javaScriptEnabled = true

    TheActivity.component.inject(this)

    Observable.just(ScreenCreated())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  fun showReport(uri: URI) {
    showWebview(true)
    webView.loadUrl(uri.toString())
  }

  fun showNoReportsAvailable() {
    showWebview(false)
    webView.loadUrl("about:blank")
  }

  private fun showWebview(isVisible: Boolean) {
    webView.visibleOrGone(isVisible)
    noReportView.visibleOrGone(isVisible.not())
  }
}
