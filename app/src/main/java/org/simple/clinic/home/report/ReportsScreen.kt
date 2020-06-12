package org.simple.clinic.home.report

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_report.view.*
import org.simple.clinic.bindUiToController
import org.simple.clinic.main.TheActivity
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import java.net.URI
import javax.inject.Inject

class ReportsScreen(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

  @Inject
  lateinit var controller: ReportsScreenController

  @SuppressLint("SetJavaScriptEnabled")
  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }

    webView.settings.javaScriptEnabled = true

    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = screenCreates(),
        controller = controller,
        screenDestroys = detaches().map { ScreenDestroyed() }
    )
  }

  private fun screenCreates(): Observable<UiEvent> = Observable.just(ScreenCreated())

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
