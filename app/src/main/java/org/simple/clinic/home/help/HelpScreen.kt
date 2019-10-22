package org.simple.clinic.home.help

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.main.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.help.HelpScreenTryAgainClicked
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.visibleOrGone
import java.net.URI
import javax.inject.Inject

class HelpScreen(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

  @Inject
  lateinit var controller: HelpScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val webView by bindView<WebView>(R.id.help_webview)
  private val noContentView by bindView<View>(R.id.help_no_content)
  private val toolbar by bindView<Toolbar>(R.id.help_toolbar)
  private val errorMessageTextView by bindView<TextView>(R.id.help_error_message)
  private val tryAgainButton by bindView<Button>(R.id.help_try_again)
  private val progresBar by bindView<ProgressBar>(R.id.help_progress)

  @SuppressLint("SetJavaScriptEnabled")
  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }

    toolbar.setNavigationOnClickListener { screenRouter.pop() }

    webView.settings.javaScriptEnabled = true

    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = Observable.merge(screenCreates(), tryAgainClicks()),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun tryAgainClicks() = RxView
      .clicks(tryAgainButton)
      .map { HelpScreenTryAgainClicked }

  fun showHelp(uri: URI) {
    webView.visibleOrGone(true)
    progresBar.visibleOrGone(false)
    noContentView.visibleOrGone(false)
    webView.loadUrl(uri.toString())
  }

  fun showNoHelpAvailable() {
    webView.visibleOrGone(false)
    progresBar.visibleOrGone(false)
    noContentView.visibleOrGone(true)
    webView.loadUrl("about:blank")
  }

  fun showLoadingView() {
    progresBar.visibleOrGone(true)
    noContentView.visibleOrGone(false)
  }

  fun showNetworkErrorMessage() {
    errorMessageTextView.setText(R.string.help_no_connection)
  }

  fun showUnexpectedErrorMessage() {
    errorMessageTextView.setText(R.string.help_something_went_wrong)
  }
}
