package org.simple.clinic.home.report

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import io.reactivex.Observable
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class ReportsScreen(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs), ReportsUi {

  @Inject
  lateinit var effectHandler: ReportsEffectHandler

  private val delegate by unsafeLazy {
    val uiRenderer = ReportsUiRenderer(this)

    MobiusDelegate.forView(
        events = Observable.never(),
        defaultModel = ReportsModel.create(),
        init = ReportsInit(),
        update = ReportsUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  private lateinit var webView: WebView
  private lateinit var noReportView: LinearLayout

  @SuppressLint("SetJavaScriptEnabled")
  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }

    webView.settings.javaScriptEnabled = true

    context.injector<Injector>().inject(this)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun showReport(html: String) {
    showWebview(true)
    webView.loadDataWithBaseURL(null, html, "text/html", Charsets.UTF_8.name(), null)
  }

  override fun showNoReportsAvailable() {
    showWebview(false)
    webView.loadUrl("about:blank")
  }

  private fun showWebview(isVisible: Boolean) {
    webView.visibleOrGone(isVisible)
    noReportView.visibleOrGone(isVisible.not())
  }

  interface Injector {
    fun inject(target: ReportsScreen)
  }
}
